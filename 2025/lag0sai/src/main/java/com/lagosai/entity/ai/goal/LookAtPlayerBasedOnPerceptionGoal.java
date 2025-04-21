package com.lagosai.entity.ai.goal;

import com.lagosai.entity.CapabilityStat;
import com.lagosai.entity.Lag0sEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import java.util.EnumSet;

public class LookAtPlayerBasedOnPerceptionGoal extends Goal {
    protected final Lag0sEntity mob;
    protected LivingEntity lookAt;
    protected final float baseLookDistance;
    protected int lookTime;
    private final float probability;
    protected final Class<? extends LivingEntity> lookAtType;
    protected final TargetingConditions lookAtContext;
    private static final float PERCEPTION_DISTANCE_MULTIPLIER = 16.0f; // Max distance increase at max perception

    public LookAtPlayerBasedOnPerceptionGoal(Lag0sEntity pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance) {
        this(pMob, pLookAtType, pLookDistance, 0.02F); // Default probability
    }

    public LookAtPlayerBasedOnPerceptionGoal(Lag0sEntity pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance, float pProbability) {
        this.mob = pMob;
        this.lookAtType = pLookAtType;
        this.baseLookDistance = pLookDistance;
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
        // Check probability
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
        }
        
        // Grant perception XP occasionally just for trying
        if (this.mob.tickCount % 40 == 0) { // Reduced frequency
            this.mob.gainXp(CapabilityStat.PERCEPTION, 0.05f); 
        }
        
        // Find closest entity of the target type within dynamic range
        if (this.mob.getTarget() != null) { // Don't look if already targeting something else
            this.lookAt = this.mob.getTarget();
        }

        if (this.lookAtType == Player.class) {
            // Use adjusted look context for players
            TargetingConditions playerContext = this.lookAtContext.copy().range(Math.sqrt(getDynamicLookDistanceSqr()));
            this.lookAt = this.mob.level().getNearestPlayer(playerContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        } else {
            this.lookAt = this.mob.level().getNearestEntity(
                this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate(Math.sqrt(getDynamicLookDistanceSqr()), 3.0, Math.sqrt(getDynamicLookDistanceSqr())), (p_147175_) -> true),
                TargetingConditions.forNonCombat().range(Math.sqrt(getDynamicLookDistanceSqr())), 
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
        // Set look time (how long to stare)
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(20));
        // Grant more significant Perception XP upon starting to look at something
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