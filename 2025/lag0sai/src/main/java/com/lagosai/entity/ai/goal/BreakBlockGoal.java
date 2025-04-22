package com.lagosai.entity.ai.goal;

import com.lagosai.entity.CapabilityStat;
import com.lagosai.entity.Lag0sEntity;
import com.lagosai.Lag0sMod;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;

public class BreakBlockGoal extends MoveToBlockGoal {
    private final Lag0sEntity lagosEntity;
    private int breakingTime;
    private static final int MAX_BREAK_TIME = 60; // Increase time slightly
    private static final float CRAFTING_XP_PER_BREAK = 2.0f;
    private BlockState targetBlockState; // Store the state for sound/particles

    public BreakBlockGoal(Lag0sEntity pMob, double pSpeedModifier, int pSearchRange) {
        super(pMob, pSpeedModifier, pSearchRange, 6); // Range Vertical is 6 (default for MoveToBlockGoal)
        this.lagosEntity = pMob;
    }

    @Override
    protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
        BlockState blockstate = pLevel.getBlockState(pPos);
        boolean isLog = blockstate.is(BlockTags.LOGS);
        if (isLog) {
            targetBlockState = blockstate; // Store the state when found
        }
        return isLog;
    }

    @Override
    public void start() {
        super.start();
        this.breakingTime = 0;
    }
    
    @Override
	public void stop() {
		super.stop();
		this.mob.fallDistance = 1.0F; // Reset fall distance? Copied from HarvestFarmlandGoal
	}

    @Override
    public void tick() {
        super.tick(); 

        if (this.isReachedTarget()) {
            Lag0sMod.LOGGER.debug("BreakBlockGoal: Reached target {} at entity pos {}", this.blockPos, this.lagosEntity.blockPosition()); // Log positions
            if (targetBlockState == null) { 
                targetBlockState = this.lagosEntity.level().getBlockState(this.blockPos);
                if (!targetBlockState.is(BlockTags.LOGS)) {
                    Lag0sMod.LOGGER.debug("BreakBlockGoal: Target block changed, stopping.");
                    this.stop(); 
                    return;
                }
            }

            // Swing arm while breaking
            Lag0sMod.LOGGER.debug("BreakBlockGoal: Attempting to swing hand."); // Log before swing
            this.lagosEntity.swing(InteractionHand.MAIN_HAND);
            this.breakingTime++;

            // Play step sound occasionally
            if (this.breakingTime % 8 == 0) { 
                SoundType soundtype = targetBlockState.getSoundType();
                 this.lagosEntity.level().playSound(null, this.blockPos, soundtype.getHitSound(), SoundSource.NEUTRAL, soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.8F);
            }

            if (this.breakingTime >= MAX_BREAK_TIME) {
                Lag0sMod.LOGGER.debug("BreakBlockGoal: Breaking block {}.", this.blockPos);
                this.lagosEntity.gainXp(CapabilityStat.CRAFTING, CRAFTING_XP_PER_BREAK);
                
                Level level = this.lagosEntity.level();
                level.destroyBlock(this.blockPos, false, this.lagosEntity); 
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, this.blockPos, Block.getId(targetBlockState)); 
                SoundType soundtype = targetBlockState.getSoundType();
                level.playSound(null, this.blockPos, soundtype.getBreakSound(), SoundSource.NEUTRAL, soundtype.getVolume(), soundtype.getPitch()); 

                targetBlockState = null; 
                this.stop(); 
            }
        } else {
             // Add log if not reached target yet but goal is active
             if (this.breakingTime > 0) { // If we started breaking but moved away
                 Lag0sMod.LOGGER.debug("BreakBlockGoal: Moved away from target before finishing.");
                 this.breakingTime = 0; // Reset timer
             }
        }
    }

    // Lower chance to start this goal compared to vanilla MoveToBlockGoal default
    @Override
    protected int nextStartTick(net.minecraft.world.entity.PathfinderMob pCreature) {
        // Reduce delay significantly for testing (e.g., 20-40 ticks = 1-2 seconds)
        return reducedTickDelay(20 + pCreature.getRandom().nextInt(20)); 
    }
} 