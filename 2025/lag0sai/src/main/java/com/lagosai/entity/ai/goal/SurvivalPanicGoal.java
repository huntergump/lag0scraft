package com.lagosai.entity.ai.goal;

import com.lagosai.entity.CapabilityStat;
import com.lagosai.entity.Lag0sEntity;
import com.lagosai.entity.PersonalityProfile;
import net.minecraft.world.entity.ai.goal.PanicGoal;

public class SurvivalPanicGoal extends PanicGoal {
    private final Lag0sEntity lagosEntity;

    public SurvivalPanicGoal(Lag0sEntity pMob, double pSpeedModifier) {
        super(pMob, pSpeedModifier);
        this.lagosEntity = pMob;
    }

    @Override
    public boolean canUse() {
        // Only trigger if the default PanicGoal condition (being hurt) is met
        if (!super.canUse()) {
            return false;
        }

        // Now, add custom logic based on stats/personality
        float survivalStat = lagosEntity.getCapabilityStatValue(CapabilityStat.SURVIVAL);
        float feelingTrait = lagosEntity.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.FEELING);
        float thinkingTrait = lagosEntity.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.THINKING);

        // Calculate chance to panic. Lower survival or higher feeling increases chance.
        // Example: Base 50% chance, modified by stats/traits
        float panicChance = 0.5f + (0.5f * (1.0f - survivalStat)) + (0.3f * (feelingTrait - thinkingTrait));
        panicChance = Math.max(0.05f, Math.min(1.0f, panicChance)); // Clamp chance between 5% and 100%

        boolean shouldPanic = this.lagosEntity.getRandom().nextFloat() < panicChance;

        if(shouldPanic) {
            lagosEntity.gainXp(CapabilityStat.SURVIVAL, 0.5f); // Gain some survival XP for panicking
        }

        return shouldPanic;
    }
} 