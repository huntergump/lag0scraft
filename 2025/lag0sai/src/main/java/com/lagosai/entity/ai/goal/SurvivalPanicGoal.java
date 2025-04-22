package com.lagosai.entity.ai.goal;

import com.lagosai.Lag0sMod;
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
        boolean shouldPanicInitially = super.canUse();
        Lag0sMod.LOGGER.debug("SurvivalPanicGoal: super.canUse() = {}", shouldPanicInitially);
        
        if (!shouldPanicInitially) {
            return false;
        }

        float survivalStat = lagosEntity.getCapabilityStatValue(CapabilityStat.SURVIVAL);
        float feelingTrait = lagosEntity.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.FEELING);
        float thinkingTrait = lagosEntity.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.THINKING);

        float panicChance = 0.5f + (0.5f * (1.0f - survivalStat)) + (0.3f * (feelingTrait - thinkingTrait));
        panicChance = Math.max(0.05f, Math.min(1.0f, panicChance));

        float randomRoll = this.lagosEntity.getRandom().nextFloat();
        boolean shouldPanic = randomRoll < panicChance;
        
        Lag0sMod.LOGGER.debug("SurvivalPanicGoal: Survival={}, Feeling={}, Thinking={}, Chance={}, Roll={}, ShouldPanic={}", 
            survivalStat, feelingTrait, thinkingTrait, panicChance, randomRoll, shouldPanic);

        if(shouldPanic) {
            lagosEntity.gainXp(CapabilityStat.SURVIVAL, 0.5f);
        }

        return shouldPanic;
    }
} 