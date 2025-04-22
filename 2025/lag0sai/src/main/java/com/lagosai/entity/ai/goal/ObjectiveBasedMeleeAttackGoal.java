package com.lagosai.entity.ai.goal;

import com.lagosai.entity.Lag0sEntity;
import com.lagosai.entity.ai.Objective;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

// Wrapper to make melee attacks respect the current objective
public class ObjectiveBasedMeleeAttackGoal extends MeleeAttackGoal {
    private final Lag0sEntity lagosEntity;

    public ObjectiveBasedMeleeAttackGoal(Lag0sEntity pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
        super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
        this.lagosEntity = pMob;
    }

    @Override
    public boolean canUse() {
        // Check 1: Does the parent goal think we can attack (e.g., target exists, in range)?
        if (!super.canUse()) {
            return false;
        }

        // Check 2: Is the current objective compatible with attacking?
        Objective currentObj = lagosEntity.getCurrentObjective();
        boolean canAttackObjective = 
            currentObj == Objective.PURSUE_PURPOSE || // If purpose involves fighting
            currentObj == Objective.ENSURE_SURVIVAL;   // If survival requires fighting (e.g., cannot flee)
            // Could add more nuance later, e.g., based on SOCIAL if defending others

        // Only allow attacking if the objective permits it
        return canAttackObjective; 
    }

    // canContinueToUse might also need an objective check if the objective can change mid-combat
    @Override
    public boolean canContinueToUse() {
         Objective currentObj = lagosEntity.getCurrentObjective();
         boolean canAttackObjective = 
            currentObj == Objective.PURSUE_PURPOSE || 
            currentObj == Objective.ENSURE_SURVIVAL;
            
        return canAttackObjective && super.canContinueToUse();
    }
} 