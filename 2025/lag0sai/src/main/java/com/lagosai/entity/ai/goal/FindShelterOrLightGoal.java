package com.lagosai.entity.ai.goal;

import com.lagosai.Lag0sMod;
import com.lagosai.entity.Lag0sEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;

import java.util.EnumSet;

public class FindShelterOrLightGoal extends Goal {
    private final Lag0sEntity mob;
    private final double speedModifier;
    private BlockPos targetPos = null;
    private static final int SEARCH_RANGE = 16; // How far to search for shelter
    private static final int MIN_LIGHT_LEVEL = 8; // Minimum acceptable light level

    public FindShelterOrLightGoal(Lag0sEntity pMob, double pSpeedModifier) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Level level = this.mob.level();
        boolean isNight = level.isNight();
        boolean isRaining = level.isRaining();
        boolean isThundering = level.isThundering();
        boolean canSeeSky = level.canSeeSky(this.mob.blockPosition());
        boolean needsShelterCondition = (isNight || isRaining || isThundering) && canSeeSky;
        
        Lag0sMod.LOGGER.debug("FindShelter: Night={}, Rain={}, Thunder={}, SeeSky={}, NeedsShelterCondition={}", 
                              isNight, isRaining, isThundering, canSeeSky, needsShelterCondition);

        if (!needsShelterCondition) {
            return false;
        }

        boolean currentlyUnsafe = isPositionUnsafe(level, this.mob.blockPosition());
        Lag0sMod.LOGGER.debug("FindShelter: Currently Unsafe = {}", currentlyUnsafe);
        
        if (!currentlyUnsafe) {
             return false; // Don't activate if already safe
        }
        
        boolean foundTarget = findRandomSafePos();
        Lag0sMod.LOGGER.debug("FindShelter: Found Target Pos = {}", foundTarget);
        return foundTarget;
    }

    @Override
    public boolean canContinueToUse() {
        boolean pathDone = this.mob.getNavigation().isDone();
        boolean stillUnsafe = isPositionUnsafe(this.mob.level(), this.mob.blockPosition());
        boolean canContinue = this.targetPos != null && !pathDone && stillUnsafe;
        Lag0sMod.LOGGER.debug("FindShelter: Continue? Target={}, PathDone={}, StillUnsafe={}, Result={}", 
                              this.targetPos != null, pathDone, stillUnsafe, canContinue);
        return canContinue;
    }

    @Override
    public void start() {
        if (this.targetPos != null) {
            this.mob.getNavigation().moveTo(this.targetPos.getX() + 0.5, this.targetPos.getY(), this.targetPos.getZ() + 0.5, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        // If path becomes invalid or target reached, try finding a new one or stop
        if (this.targetPos == null || this.mob.getNavigation().isDone()) {
            if (!findRandomSafePos()) {
                 // Could add logic here to maybe build a rudimentary shelter if none found?
                 // For now, just stop the goal if no path is found
                 this.mob.getNavigation().stop(); // Explicitly stop if find fails
            } else {
                 this.mob.getNavigation().moveTo(this.targetPos.getX() + 0.5, this.targetPos.getY(), this.targetPos.getZ() + 0.5, this.speedModifier);
            }
        }
    }

    private boolean findRandomSafePos() {
        RandomSource random = this.mob.getRandom();
        BlockPos currentPos = this.mob.blockPosition();

        for(int i = 0; i < 10; ++i) { // Try 10 times to find a spot
            BlockPos potentialPos = currentPos.offset(
                random.nextInt(SEARCH_RANGE * 2 + 1) - SEARCH_RANGE, 
                random.nextInt(5) - 2, // Check slightly above/below current Y
                random.nextInt(SEARCH_RANGE * 2 + 1) - SEARCH_RANGE
            );

            // Check if the potential position is safe and reachable
            if (GoalUtils.isOutsideLimits(potentialPos, this.mob) || 
                !this.mob.level().isLoaded(potentialPos) || // Ensure chunk is loaded
                isPositionUnsafe(this.mob.level(), potentialPos)) { 
                continue; 
            }
            
            // Check path reachability 
            PathNavigation navigation = this.mob.getNavigation();
            if (navigation.createPath(potentialPos, 0) != null) { // Check if a path can be generated
                 this.targetPos = potentialPos;
                 return true;
            }
        }
        return false; // Couldn't find a suitable spot
    }
    
    // Helper to determine if a position is unsafe (too dark and exposed to sky)
    private boolean isPositionUnsafe(Level level, BlockPos pos) {
         boolean unsafe = level.getBrightness(LightLayer.BLOCK, pos) < MIN_LIGHT_LEVEL && level.canSeeSky(pos);
         // Uncomment the debug log here
         Lag0sMod.LOGGER.debug("FindShelter: Pos {} Unsafe Check: Brightness={}, SeeSky={}, Result={}", 
                               pos, level.getBrightness(LightLayer.BLOCK, pos), level.canSeeSky(pos), unsafe);
         return unsafe;
    }
} 