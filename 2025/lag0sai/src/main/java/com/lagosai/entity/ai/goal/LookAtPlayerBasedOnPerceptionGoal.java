package com.lagosai.entity.ai.goal;

import com.lagosai.entity.CapabilityStat;
import com.lagosai.entity.Lag0sEntity;
import com.lagosai.entity.PersonalityProfile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import java.util.EnumSet;
import com.lagosai.entity.ai.Objective;

public class LookAtPlayerBasedOnPerceptionGoal extends Goal {
    protected final Lag0sEntity mob;
    protected LivingEntity lookAt;
    protected final float baseLookDistance;
    protected int lookTime;
    private final float baseProbability;
    private float probability;
    protected final Class<? extends LivingEntity> lookAtType;
    protected final TargetingConditions lookAtContext;
    private static final float PERCEPTION_DISTANCE_MULTIPLIER = 16.0f; // Max distance increase at max perception
    private static final int BASE_LOOK_TIME_TICKS = 40; // Base duration
    private static final int MAX_LOOK_TIME_VARIANCE = 20; // Max random variance
    private static final float PERSONALITY_TIME_FACTOR = 20.0f; // How much S/N affects duration (in ticks)

    public LookAtPlayerBasedOnPerceptionGoal(Lag0sEntity pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance) {
        this(pMob, pLookAtType, pLookDistance, 0.02F); // Default probability
    }

    public LookAtPlayerBasedOnPerceptionGoal(Lag0sEntity pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance, float pProbability) {
        this.mob = pMob;
        this.lookAtType = pLookAtType;
        this.baseLookDistance = pLookDistance;
        this.baseProbability = pProbability;
        this.probability = pProbability;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK)); // Sets this goal controls looking
        // Set up targeting conditions, adjusting for player specifics if needed
        if (pLookAtType == Player.class) {
            this.lookAtContext = TargetingConditions.forNonCombat().range(pLookDistance).selector((p_25530_) -> {
                return p_25530_ instanceof Player && !((Player)p_25530_).isSpectator() && !((Player)p_25530_).isCreative();
            });
        } else {
            this.lookAtContext = TargetingConditions.forNonCombat().range(pLookDistance);
        }
    }

    // Calculate dynamic look distance based on Perception
    private double getDynamicLookDistanceSqr() {
        float perceptionValue = this.mob.getCapabilityStatValue(CapabilityStat.PERCEPTION);
        double actualLookDistance = this.baseLookDistance + (perceptionValue * PERCEPTION_DISTANCE_MULTIPLIER);
        return actualLookDistance * actualLookDistance; // Use squared distance for checks
    }

    @Override
    public boolean canUse() {
        // Check 1: Is the current objective compatible?
        Objective currentObj = this.mob.getCurrentObjective();
        if (currentObj != Objective.SOCIALIZE && 
            currentObj != Objective.EXPLORE_LEARN && 
            currentObj != Objective.IDLE) {
            // Lag0sMod.LOGGER.trace("LookAtPlayerGoal: Objective {} not suitable.", currentObj);
            return false;
        }

        // Check 2: Adjust probability based on E/I trait
        float extraversion = this.mob.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.EXTRAVERT);
        float introversion = this.mob.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.INTROVERT);
        float personalityFactor = 1.0f + (extraversion * 1.5f) - (introversion * 0.8f); 
        float currentProbability = Math.max(0.0f, this.baseProbability * personalityFactor); 
        
        // Check 3: Probability check
        if (this.mob.getRandom().nextFloat() >= currentProbability) {
            return false;
        }
        
        // Check 4: Grant perception XP 
        if (this.mob.tickCount % 40 == 0) { 
            this.mob.gainXp(CapabilityStat.PERCEPTION, 0.05f); 
        }
        
        // Check 5: Find lookAt target using dynamic distance
        if (this.mob.getTarget() != null) {
            this.lookAt = this.mob.getTarget();
        }
        if (this.lookAtType == Player.class) {
            TargetingConditions playerContext = this.lookAtContext.copy().range(Math.sqrt(getDynamicLookDistanceSqr()));
            this.lookAt = this.mob.level().getNearestPlayer(playerContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        } else {
            double dynamicDistance = Math.sqrt(getDynamicLookDistanceSqr());
            this.lookAt = this.mob.level().getNearestEntity(
                this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate(dynamicDistance, 3.0, dynamicDistance), (p_147175_) -> true),
                TargetingConditions.forNonCombat().range(dynamicDistance), 
                this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()
            );
        }

        return this.lookAt != null;
    }

    @Override
    public boolean canContinueToUse() {
        // Stop if entity is gone, too far, or lookTime runs out
        if (this.lookAt == null || !this.lookAt.isAlive()) {
            return false;
        }
        // Check distance using dynamic range
        if (this.mob.distanceToSqr(this.lookAt) > getDynamicLookDistanceSqr()) {
            return false;
        }
        return this.lookTime > 0;
    }

    @Override
    public void start() {
        // Calculate base look time with random variance
        int baseDuration = BASE_LOOK_TIME_TICKS + this.mob.getRandom().nextInt(MAX_LOOK_TIME_VARIANCE);
        
        // Adjust duration based on S/N trait
        float sensingValue = this.mob.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.SENSING);
        float intuitionValue = this.mob.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.INTUITION);
        // Sensing increases duration, Intuition decreases it
        float personalityAdjustment = (sensingValue - intuitionValue) * PERSONALITY_TIME_FACTOR;
        
        this.lookTime = this.adjustedTickDelay(Math.max(1, (int)(baseDuration + personalityAdjustment))); // Ensure lookTime is at least 1 tick

        // Grant Perception XP 
        this.mob.gainXp(CapabilityStat.PERCEPTION, 0.5f); 
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public void tick() {
        // Don't tick if target is gone
        if (this.lookAt == null || !this.lookAt.isAlive()) {
            return;
        }
        // Make the mob look at the target
        this.mob.getLookControl().setLookAt(this.lookAt.getX(), this.lookAt.getEyeY(), this.lookAt.getZ());
        this.lookTime--;
    }
} 