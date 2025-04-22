package com.lagosai.entity.ai.goal;

import com.lagosai.Lag0sMod;
import com.lagosai.entity.CapabilityStat;
import com.lagosai.entity.Lag0sEntity;
import com.lagosai.entity.ai.Objective;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleOptions;

import java.util.EnumSet;
import java.util.function.Predicate;

public class GoalAcquireResource extends Goal {
    protected final Lag0sEntity mob;
    protected final double speedModifier;
    protected final int searchRange;
    protected final TagKey<Block> resourceTag; // Tag to search for (e.g., BlockTags.LOGS)
    
    protected BlockPos targetPos = BlockPos.ZERO;
    protected BlockState targetBlockState; // Store the state for effects
    protected int breakingTime;
    protected int nextStartTick;
    protected boolean reachedTarget;
    protected int consecutivePathFailures; // Track consecutive pathing failures
    protected boolean wasInterrupted; // Track if goal was interrupted
    protected boolean resourceAcquired; // Track if we successfully got the resource
    
    private static final int MAX_BREAK_TIME = 60; 
    private static final float CRAFTING_XP_PER_BREAK = 2.0f;
    private static final int COOLDOWN_TICKS = 100; // Cooldown after finishing/failing
    private static final int COOLDOWN_TICKS_FAIL = 100; // Cooldown after failing
    private static final int MAX_PATH_FAILURES = 3; // Max consecutive path failures before longer cooldown
    private int lastBreakProgress = -1;

    public GoalAcquireResource(Lag0sEntity pMob, double pSpeedModifier, int pSearchRange, TagKey<Block> pResourceTag) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.searchRange = pSearchRange;
        this.resourceTag = pResourceTag;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Check 1: Cooldown check (Log this first!)
        if (this.nextStartTick > 0) {
            --this.nextStartTick;
            // Lag0sMod.LOGGER.trace("AcquireResourceGoal: On Cooldown ({})", this.nextStartTick); // Use Trace if too spammy
            return false;
        }
        
        // Check 2: Is the current objective compatible?
        Objective currentObj = this.mob.getCurrentObjective();
        // Lag0sMod.LOGGER.trace("AcquireResourceGoal: Checking canUse. Current Objective: {}", currentObj); // Trace if needed
        if (currentObj != Objective.IMPROVE_COMFORT && currentObj != Objective.PURSUE_PURPOSE) {
            return false; 
        }
        
        // Check 3: Search for a suitable resource block
        boolean foundTarget = findTarget();
        if (!foundTarget) {
             this.nextStartTick = COOLDOWN_TICKS_FAIL; 
             Lag0sMod.LOGGER.debug("AcquireResourceGoal: Failed to find target for resource {} (Cooldown started).", this.resourceTag.location());
            return false;
        }
        
        // If all checks pass:
        Lag0sMod.LOGGER.debug("AcquireResourceGoal: CAN USE. Found target {} for resource {}", this.targetPos, this.resourceTag.location());
        return true;
    }
    
    private boolean findTarget() {
        RandomSource random = this.mob.getRandom();
        BlockPos currentPos = this.mob.blockPosition();
        PathNavigation navigation = this.mob.getNavigation();

        for(int i = 0; i < 15; ++i) { // Increase search attempts
             BlockPos potentialPos = currentPos.offset(
                random.nextInt(this.searchRange * 2 + 1) - this.searchRange, 
                random.nextInt(6) - 3, 
                random.nextInt(this.searchRange * 2 + 1) - this.searchRange
            );

            // Check if the block matches the tag and is reachable
            if (this.mob.level().isLoaded(potentialPos) && 
                this.mob.level().getBlockState(potentialPos).is(this.resourceTag)) 
            {
                 // Check path reachability more robustly
                Path path = navigation.createPath(potentialPos, 0);
                if (path != null && path.canReach()) { 
                    this.targetPos = potentialPos;
                    return true;
                }
            }
        }
        this.targetPos = null;
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.resourceAcquired) {
            return false; // Stop if we got what we wanted
        }
        if (this.breakingTime >= MAX_BREAK_TIME) {
            return false; // Stop if we've been breaking too long
        }
        return this.targetPos != null && 
               breakingTime < MAX_BREAK_TIME && 
               isValidTarget(this.mob.level(), this.targetPos);
    }

    @Override
    public void start() {
        this.breakingTime = 0;
        this.reachedTarget = false;
        this.targetBlockState = null; 
        Lag0sMod.LOGGER.debug("AcquireResourceGoal: Starting. Moving to {}", this.targetPos);
        moveToTarget();
    }

    protected void moveToTarget() {
        if (this.targetPos != null) {
            PathNavigation navigation = this.mob.getNavigation();
            Path path = navigation.createPath(this.targetPos, 0);
            if (path != null && navigation.moveTo(path, this.speedModifier)) {
                Lag0sMod.LOGGER.debug("AcquireResourceGoal: Pathing started to {}", this.targetPos);
                this.consecutivePathFailures = 0; // Reset on successful path
            } else {
                Lag0sMod.LOGGER.warn("AcquireResourceGoal: Cannot path to target {}", this.targetPos);
                this.consecutivePathFailures++;
                if (this.consecutivePathFailures >= MAX_PATH_FAILURES) {
                    Lag0sMod.LOGGER.warn("AcquireResourceGoal: Too many path failures, stopping");
                    this.stop(); // Stop if pathing fails too many times
                }
            }
        }
    }

    @Override
    public void stop() {
        Lag0sMod.LOGGER.debug("AcquireResourceGoal: Stopping. Target: {}, BreakingTime: {}, Interrupted: {}", 
            this.targetPos, this.breakingTime, this.wasInterrupted);
        
        this.mob.getNavigation().stop();
        
        // Apply longer cooldown if we had path failures or interruption
        if (this.consecutivePathFailures >= MAX_PATH_FAILURES || this.wasInterrupted) {
            this.nextStartTick = COOLDOWN_TICKS_FAIL * 2;
            Lag0sMod.LOGGER.debug("AcquireResourceGoal: Applying extended cooldown due to failures/interruption");
        } else {
            this.nextStartTick = COOLDOWN_TICKS;
        }
        
        // Reset all state
        this.targetPos = null;
        this.breakingTime = 0;
        this.targetBlockState = null;
        this.reachedTarget = false;
        this.consecutivePathFailures = 0;
        this.wasInterrupted = false;
        this.resourceAcquired = false;
        this.lastBreakProgress = -1;
    }

    protected boolean isAdjacentToTarget() {
        if (this.targetPos == null) return false;
        return this.mob.blockPosition().distManhattan(this.targetPos) <= 1; 
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            Lag0sMod.LOGGER.warn("AcquireResourceGoal: Tick with null target!");
            this.wasInterrupted = true;
            stop(); 
            return;
        }

        if (!this.reachedTarget) {
            if (this.mob.distanceToSqr(Vec3.atCenterOf(this.targetPos)) > 2.0) {
                moveToTarget();
            } else {
                this.reachedTarget = true;
                Lag0sMod.LOGGER.debug("AcquireResourceGoal: Reached target at {}", this.targetPos);
            }
            return;
        }

        Level level = this.mob.level();
        BlockState state = level.getBlockState(this.targetPos);
        
        // Validate target is still valid
        if (!isValidTarget(state, this.targetPos, level)) {
            Lag0sMod.LOGGER.warn("AcquireResourceGoal: Target no longer valid at {}", this.targetPos);
            this.wasInterrupted = true;
            stop();
            return;
        }

        this.breakingTime++;
        int i = (int)((float)this.breakingTime / MAX_BREAK_TIME * 10.0F);
        
        if (i != this.lastBreakProgress) {
            level.destroyBlockProgress(this.mob.getId(), this.targetPos, i);
            this.lastBreakProgress = i;
        }

        if (this.breakingTime >= MAX_BREAK_TIME) {
            // Success! Resource acquired
            this.resourceAcquired = true;
            level.destroyBlock(this.targetPos, true, this.mob);
            this.mob.gainXp(CapabilityStat.CRAFTING, CRAFTING_XP_PER_BREAK);
            
            // Update entity's wood state if we're gathering wood
            if (this.resourceTag == BlockTags.LOGS) {
                ((Lag0sEntity)this.mob).setHasWood(true);
            }
            
            // Play success effects
            SoundType soundType = state.getSoundType();
            level.playSound(null, this.targetPos, soundType.getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            
            Vec3 pos = Vec3.atCenterOf(this.targetPos);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, 
                    pos.x, pos.y, pos.z, 5, 0.5D, 0.5D, 0.5D, 0.0D);
            }
            
            Lag0sMod.LOGGER.info("AcquireResourceGoal: Successfully acquired resource at {}", this.targetPos);
            stop();
        }
    }

    protected boolean isValidTarget(BlockState state, BlockPos pos, LevelReader level) {
        return state != null && state.is(this.resourceTag);
    }

    protected boolean isValidTarget(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return isValidTarget(state, pos, level);
    }
} 