package com.lagosai.entity.ai.goal;

import com.lagosai.entity.CapabilityStat;
import com.lagosai.entity.Lag0sEntity;
import com.lagosai.entity.PersonalityProfile;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;

public class PersonalityRandomStrollGoal extends RandomStrollGoal {
    private final Lag0sEntity lagosEntity;
    private static final float BASE_STROLL_PROBABILITY = 0.001f; // Vanilla default
    private static final float MOBILITY_XP_PER_STROLL_TICK = 0.01f;
    private static final float PERSONALITY_FACTOR_MULTIPLIER = 0.5f; // How much personality affects chance

    public PersonalityRandomStrollGoal(Lag0sEntity pMob, double pSpeedModifier) {
        super(pMob, pSpeedModifier);
        this.lagosEntity = pMob;
    }

    @Override
    public boolean canUse() {
        // Calculate probability based on E/I and J/P
        float extraversion = lagosEntity.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.EXTRAVERT);
        float introversion = lagosEntity.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.INTROVERT);
        float judging = lagosEntity.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.JUDGING);
        float perceiving = lagosEntity.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.PERCEIVING);

        // E & P increase chance, I & J decrease it
        float personalityFactor = 1.0f + ((extraversion - introversion) * PERSONALITY_FACTOR_MULTIPLIER) + ((perceiving - judging) * PERSONALITY_FACTOR_MULTIPLIER);
        float currentProbability = Math.max(0.0f, BASE_STROLL_PROBABILITY * personalityFactor);
        currentProbability = Math.min(0.01f, currentProbability); // Clamp max chance

        // Check probability first
        if (this.mob.getRandom().nextFloat() >= currentProbability) {
            return false;
        }

        // If probability allows, check original conditions
        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        // Grant Mobility XP (moved from anonymous class)
        if (this.lagosEntity.tickCount % 20 == 0) {
            this.lagosEntity.gainXp(CapabilityStat.MOBILITY, MOBILITY_XP_PER_STROLL_TICK * 20);
        }
    }
    
    // Ensure it can be interrupted if another goal takes priority (like panic)
    @Override
	public boolean isInterruptable() {
		return true;
	}
} 