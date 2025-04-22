package com.lagosai.entity.ai.goal;

import com.lagosai.Lag0sMod;
import com.lagosai.entity.CapabilityStat;
import com.lagosai.entity.Lag0sEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Comparator;

public class GoalFindAndEatFood extends Goal {
    protected final Lag0sEntity mob;
    protected final double speedModifier;
    protected ItemEntity targetFoodItem = null;
    protected int nextStartTick;
    private static final int SEARCH_RANGE = 10; // How far to look for food
    private static final float EAT_XP_GAIN = 1.5f;
    private static final int COOLDOWN_TICKS_SUCCESS = 100; // Cooldown after eating
    private static final int COOLDOWN_TICKS_FAIL = 400;    // Longer cooldown if search fails

    public GoalFindAndEatFood(Lag0sEntity pMob, double pSpeedModifier) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Cooldown
        if (this.nextStartTick > 0) {
            --this.nextStartTick;
            return false;
        }
        
        // --- TODO: Replace this with actual Hunger/Need check from BehaviorManager --- 
        // Simulate need for food occasionally
        if (this.mob.getRandom().nextInt(150) != 0) { 
            return false;
        }
        // --- End TODO ---

        // Find the closest edible item
        this.targetFoodItem = findClosestEdibleItem();
        if (this.targetFoodItem == null) {
            // Set LONGER cooldown if search fails
            this.nextStartTick = COOLDOWN_TICKS_FAIL; 
            return false;
        }
        
        Lag0sMod.LOGGER.debug("FindFoodGoal: Found food item {} at {}", 
                             this.targetFoodItem.getItem().getDisplayName().getString(), this.targetFoodItem.blockPosition());
        return true;
    }

    private ItemEntity findClosestEdibleItem() {
        List<ItemEntity> items = this.mob.level().getEntitiesOfClass(ItemEntity.class, 
            this.mob.getBoundingBox().inflate(SEARCH_RANGE, 4.0, SEARCH_RANGE), 
            (itemEntity) -> !itemEntity.getItem().isEmpty() && itemEntity.getItem().isEdible() // Check if item is edible
        );
        
        return items.stream()
                    .min(Comparator.comparingDouble(item -> item.distanceToSqr(this.mob)))
                    .orElse(null);
    }

    @Override
    public boolean canContinueToUse() {
        // Continue if the target item still exists and is reachable
        return this.targetFoodItem != null && 
               !this.targetFoodItem.isRemoved() && 
               !this.mob.getNavigation().isDone(); // Continue while pathing
    }

    @Override
    public void start() {
        Lag0sMod.LOGGER.debug("FindFoodGoal: Starting. Moving to {}", this.targetFoodItem.blockPosition());
        moveToTarget();
        this.nextStartTick = 0; // Reset cooldown as we started
    }

    protected void moveToTarget() {
         if (this.targetFoodItem != null) {
             Path path = this.mob.getNavigation().createPath(this.targetFoodItem, 0);
             if (path != null && this.mob.getNavigation().moveTo(path, this.speedModifier)) {
                  Lag0sMod.LOGGER.debug("FindFoodGoal: Pathing started to {}", this.targetFoodItem.blockPosition());
             } else {
                 Lag0sMod.LOGGER.warn("FindFoodGoal: Cannot path to target food item {}", this.targetFoodItem.blockPosition());
                 this.stop(); // Stop if pathing fails
             }
         }
    }

    @Override
    public void stop() {
        Lag0sMod.LOGGER.debug("FindFoodGoal: Stopping. Target: {}, BreakingTime: {}", this.targetFoodItem != null ? this.targetFoodItem.blockPosition() : "null", 0); // BreakingTime irrelevant here
        this.mob.getNavigation().stop();
        this.targetFoodItem = null;
        // Use SUCCESS cooldown when stopping normally (could be after eating or path fail)
        this.nextStartTick = COOLDOWN_TICKS_SUCCESS; 
    }

    protected boolean isCloseEnoughToEat() {
        if (this.targetFoodItem == null) return false;
        // Check if entity is very close to the item
        return this.mob.distanceToSqr(this.targetFoodItem) < 2.0D; // Adjust distance as needed
    }

    @Override
    public void tick() {
         if (this.targetFoodItem == null || this.targetFoodItem.isRemoved()) {
            Lag0sMod.LOGGER.debug("FindFoodGoal: Target became null or removed, stopping.");
            stop(); 
            return;
        }
        
        this.mob.getLookControl().setLookAt(this.targetFoodItem, 10.0F, (float)this.mob.getMaxHeadXRot());

        if (!isCloseEnoughToEat()) {
            if (this.mob.getNavigation().isDone()) {
                 Lag0sMod.LOGGER.debug("FindFoodGoal: Navigation idle but not close enough, retrying move.");
                 moveToTarget();
            }
            return; 
        }

        // --- Close enough: Eat the item --- 
        Lag0sMod.LOGGER.debug("FindFoodGoal: Eating item {}.", this.targetFoodItem.getItem().getDisplayName().getString());
        
        this.mob.level().playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(), 
            SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.5F, this.mob.getRandom().nextFloat() * 0.1F + 0.9F);
        
        this.mob.gainXp(CapabilityStat.SURVIVAL, EAT_XP_GAIN);
        
        this.targetFoodItem.discard(); 
        
        // --- Check for more nearby food --- 
        ItemEntity nextFood = findClosestEdibleItem();
        if (nextFood != null) {
            Lag0sMod.LOGGER.debug("FindFoodGoal: Found more food nearby ({}), continuing goal.", nextFood.getItem().getDisplayName().getString());
            this.targetFoodItem = nextFood;
            moveToTarget(); // Start moving towards the new item
        } else {
             Lag0sMod.LOGGER.debug("FindFoodGoal: No more food nearby, stopping.");
             this.stop(); // Stop if no more food found
        }
    }
} 