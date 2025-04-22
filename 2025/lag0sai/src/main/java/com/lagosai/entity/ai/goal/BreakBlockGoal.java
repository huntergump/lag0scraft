package com.lagosai.entity.ai.goal;

import com.lagosai.Lag0sMod;
import com.lagosai.entity.CapabilityStat;
import com.lagosai.entity.Lag0sEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;

// Revert to extending MoveToBlockGoal
public class BreakBlockGoal extends MoveToBlockGoal {
    private final Lag0sEntity lagosEntity;
    private int breakingTime;
    private static final int MAX_BREAK_TIME = 60; 
    private static final float CRAFTING_XP_PER_BREAK = 2.0f;
    private BlockState targetBlockState; 

    public BreakBlockGoal(Lag0sEntity pMob, double pSpeedModifier, int pSearchRange) {
        // Pass vertical search distance 1 to parent
        super(pMob, pSpeedModifier, pSearchRange, 1); 
        this.lagosEntity = pMob;
        // Remove attempt to set vertical search range
        // setVerticalSearchRange(2); 
    }

    @Override
    protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
        BlockState blockstate = pLevel.getBlockState(pPos);
        return blockstate.is(BlockTags.LOGS);
    }
    
    @Override
    public void start() {
        super.start();
        this.breakingTime = 0;
        this.targetBlockState = null; 
        Lag0sMod.LOGGER.debug("BreakBlockGoal: Starting. Target: {}", this.blockPos);
    }
    
    @Override
	public void stop() {
        Lag0sMod.LOGGER.debug("BreakBlockGoal: Stopping. Final breakingTime: {}", this.breakingTime);
		super.stop();
		this.breakingTime = 0;
        this.targetBlockState = null;
	}

    @Override
    public void tick() {
        super.tick(); 
        Lag0sMod.LOGGER.trace("BreakBlockGoal: Tick. Target: {}, Reached: {}", this.blockPos, this.isReachedTarget());

        if (this.isReachedTarget()) {
            if (this.blockPos == null) return;
            
            // ADDED: Stricter adjacency check before breaking
            if (this.mob.blockPosition().distManhattan(this.blockPos) <= 1) {
                // Target block validation
                BlockState currentState = this.lagosEntity.level().getBlockState(this.blockPos);
                if (!currentState.is(BlockTags.LOGS)) {
                     Lag0sMod.LOGGER.debug("BreakBlockGoal: Target block {} is no longer valid log.", this.blockPos);
                     // Goal should stop naturally now block is invalid
                    return; 
                }
                this.targetBlockState = currentState; 

                // Perform breaking action
                this.lagosEntity.swing(InteractionHand.MAIN_HAND);
                this.breakingTime++;
                Lag0sMod.LOGGER.debug("BreakBlockGoal: Breaking progress {} / {}", this.breakingTime, MAX_BREAK_TIME);

                if (this.breakingTime % 8 == 0) { 
                    SoundType soundtype = targetBlockState.getSoundType();
                    this.lagosEntity.level().playSound(null, this.blockPos, soundtype.getHitSound(), SoundSource.NEUTRAL, soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.8F);
                }

                if (this.breakingTime >= MAX_BREAK_TIME) {
                    Lag0sMod.LOGGER.debug("BreakBlockGoal: FINISHED Breaking block {}.", this.blockPos);
                    this.lagosEntity.gainXp(CapabilityStat.CRAFTING, CRAFTING_XP_PER_BREAK);
                    
                    Level level = this.lagosEntity.level();
                    level.destroyBlock(this.blockPos, true, this.lagosEntity); 
                    level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, this.blockPos, Block.getId(targetBlockState)); 
                    SoundType soundtype = targetBlockState.getSoundType();
                    level.playSound(null, this.blockPos, soundtype.getBreakSound(), SoundSource.NEUTRAL, soundtype.getVolume(), soundtype.getPitch()); 
                    
                    // Reset state - Parent goal should stop now
                    this.breakingTime = 0;
                    this.targetBlockState = null;
                    // We don't call stop() here, let the parent goal detect the block is gone
                }
            } else {
                 // Reached vicinity according to parent, but not truly adjacent yet.
                 // Reset timer if we had started breaking.
                 if (this.breakingTime > 0) {
                     Lag0sMod.LOGGER.debug("BreakBlockGoal: Reached vicinity but lost adjacency, resetting break timer.");
                     this.breakingTime = 0;
                 }
                 // Do nothing else, let parent goal continue trying to path closer/repath
            }
        } else {
             // Not reached target, reset breaking time
             if (this.breakingTime > 0) {
                 Lag0sMod.LOGGER.debug("BreakBlockGoal: No longer ReachedTarget, resetting break timer.");
                 this.breakingTime = 0;
             }
        }
    }

    // Keep default cooldown
    // @Override
    // protected int nextStartTick(net.minecraft.world.entity.PathfinderMob pCreature) {
    //    return reducedTickDelay(20 + pCreature.getRandom().nextInt(20)); 
    // }
} 