package com.lagosai.entity;

import com.lagosai.Lag0sMod;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import com.lagosai.entity.ai.goal.LookAtPlayerBasedOnPerceptionGoal;
import com.lagosai.entity.ai.goal.SurvivalPanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import com.lagosai.entity.ai.goal.BreakBlockGoal;
import com.lagosai.entity.ai.goal.FindShelterOrLightGoal;

public class Lag0sEntity extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID_ID = 
        SynchedEntityData.defineId(Lag0sEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private final PersonalityProfile personality = new PersonalityProfile();
    private Optional<UUID> ownerUUID = Optional.empty();
    private SocietalRank societalRank = SocietalRank.FIELD_ASSOCIATE;
    private Optional<Trade> trade = Optional.empty();
    
    // Capability Stats and XP
    private final EnumMap<CapabilityStat, Float> capabilityStats = new EnumMap<>(CapabilityStat.class);
    private final EnumMap<CapabilityStat, Float> capabilityXP = new EnumMap<>(CapabilityStat.class);
    private static final float INITIAL_STAT_VALUE = 0.1f; // Example starting value
    private static final float INITIAL_XP_VALUE = 0.0f;
    // XP Threshold constants
    private static final float BASE_XP_THRESHOLD = 100.0f;
    private static final float XP_THRESHOLD_SCALING = 1.1f; // Threshold increases by 10% each level (example)
    private static final float MAX_STAT_VALUE = 1.0f; // Example max stat value
    private static final float MOBILITY_XP_PER_STROLL_TICK = 0.01f; // Tiny amount per tick
    private static final float SOCIAL_XP_PER_INTERACTION = 1.0f;
    private static final float BASE_MOVEMENT_SPEED = 0.25f; // Corresponds to Attribute
    private static final float MOBILITY_SPEED_FACTOR = 0.5f; // How much mobility affects speed (0.0 to 1.0)
    private static final float SURVIVAL_XP_PER_DAMAGE = 0.5f; // Example
    private static final float SOCIAL_EVOLVE_DELTA = 0.01f;
    private static final float HURT_EVOLVE_DELTA = 0.015f;

    // Modifier for movement speed based on MOBILITY stat
    private static final UUID MOBILITY_MODIFIER_ID = UUID.fromString("1a8f8e66-b2e4-4b1c-8a1a-a0d78e8b0b9f"); // Random UUID
    private boolean needsSpeedUpdate = true; // Flag to update modifier initially/on load

    private static final float BASE_STROLL_PROBABILITY = 0.001F; // Vanilla default chance per tick
    private static final float BASE_INTERACTION_INFO_CHANCE = 0.75f; // Base chance to share info
    private static final float PERSONALITY_INFO_FACTOR = 0.5f; // How much F/T affects chance

    public Lag0sEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        // Initialize capabilities
        for (CapabilityStat stat : CapabilityStat.values()) {
            capabilityStats.put(stat, INITIAL_STAT_VALUE);
            capabilityXP.put(stat, INITIAL_XP_VALUE);
        }
        
        // AI Goals (Lower number = higher priority)
        this.goalSelector.addGoal(0, new FloatGoal(this)); 
        this.goalSelector.addGoal(1, new SurvivalPanicGoal(this, 1.2D)); 
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 1.0D, 1.2D)); 
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Creeper.class, 10.0F, 1.1D, 1.3D)); 
        this.goalSelector.addGoal(3, new FindShelterOrLightGoal(this, 1.0D)); 
        this.goalSelector.addGoal(4, new BreakBlockGoal(this, 1.0D, 8)); 
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D)); 
        this.goalSelector.addGoal(6, new LookAtPlayerBasedOnPerceptionGoal(this, Player.class, 6.0F)); 
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_UUID_ID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        this.personality.saveNBT(pCompound);
        this.getOwnerUUID().ifPresent(uuid -> pCompound.putUUID("OwnerUUID", uuid));
        pCompound.putString("SocietalRank", this.societalRank.name());
        this.trade.ifPresent(t -> pCompound.putString("Trade", t.name()));

        // Save Capability Stats
        CompoundTag capsTag = new CompoundTag();
        for (Map.Entry<CapabilityStat, Float> entry : capabilityStats.entrySet()) {
            capsTag.putFloat(entry.getKey().name(), entry.getValue());
        }
        pCompound.put("CapabilityStats", capsTag);

        // Save Capability XP
        CompoundTag xpTag = new CompoundTag();
        for (Map.Entry<CapabilityStat, Float> entry : capabilityXP.entrySet()) {
            xpTag.putFloat(entry.getKey().name(), entry.getValue());
        }
        pCompound.put("CapabilityXP", xpTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.personality.loadNBT(pCompound);
        if (pCompound.hasUUID("OwnerUUID")) {
            this.setOwnerUUID(pCompound.getUUID("OwnerUUID"));
        } else {
            this.setOwnerUUID(null);
        }
        if (pCompound.contains("SocietalRank", CompoundTag.TAG_STRING)) {
            try {
                this.societalRank = SocietalRank.valueOf(pCompound.getString("SocietalRank"));
            } catch (IllegalArgumentException e) {
                Lag0sMod.LOGGER.warn("Invalid SocietalRank found in NBT: {}", pCompound.getString("SocietalRank"));
                this.societalRank = SocietalRank.FIELD_ASSOCIATE;
            }
        } else {
            this.societalRank = SocietalRank.FIELD_ASSOCIATE;
        }
        if (pCompound.contains("Trade", CompoundTag.TAG_STRING)) {
            try {
                this.trade = Optional.of(Trade.valueOf(pCompound.getString("Trade")));
            } catch (IllegalArgumentException e) {
                Lag0sMod.LOGGER.warn("Invalid Trade found in NBT: {}", pCompound.getString("Trade"));
                this.trade = Optional.empty();
            }
        } else {
            this.trade = Optional.empty();
        }

        // Load Capability Stats
        if (pCompound.contains("CapabilityStats", CompoundTag.TAG_COMPOUND)) {
            CompoundTag capsTag = pCompound.getCompound("CapabilityStats");
            for (CapabilityStat stat : CapabilityStat.values()) {
                capabilityStats.put(stat, capsTag.getFloat(stat.name())); // Defaults to 0f if not found
            }
        } // Consider initializing if tag doesn't exist?

        // Load Capability XP
        if (pCompound.contains("CapabilityXP", CompoundTag.TAG_COMPOUND)) {
            CompoundTag xpTag = pCompound.getCompound("CapabilityXP");
            for (CapabilityStat stat : CapabilityStat.values()) {
                capabilityXP.put(stat, xpTag.getFloat(stat.name())); // Defaults to 0f if not found
            }
        } // Consider initializing if tag doesn't exist?

        // Flag that speed needs recalculating after loading stats
        this.needsSpeedUpdate = true; 
    }

    @Override
    public void tick() {
        super.tick();
        // Update speed modifier if needed (e.g., after loading or level up)
        if (this.needsSpeedUpdate && !this.level().isClientSide()) {
            this.updateMobilitySpeedModifier();
            this.needsSpeedUpdate = false;
        }
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID_ID);
    }

    public void setOwnerUUID(@javax.annotation.Nullable UUID uuid) {
        this.ownerUUID = Optional.ofNullable(uuid);
        this.entityData.set(DATA_OWNER_UUID_ID, this.ownerUUID);
    }

    public SocietalRank getSocietalRank() {
        return this.societalRank;
    }

    public Optional<Trade> getTrade() {
        return this.trade;
    }

    public void setTrade(Optional<Trade> trade) {
        this.trade = trade;
    }

    public PersonalityProfile getPersonalityProfile() {
        return this.personality;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        boolean wasHurt = super.hurt(pSource, pAmount);
        if (wasHurt && !this.level().isClientSide()) {
            // Grant survival XP
            this.gainXp(CapabilityStat.SURVIVAL, pAmount * SURVIVAL_XP_PER_DAMAGE);
            Lag0sMod.LOGGER.debug("{} took {} damage, gained {} SURVIVAL XP", 
                this.getName().getString(), pAmount, pAmount * SURVIVAL_XP_PER_DAMAGE);
            
            // Evolve personality based on negative experience
            this.personality.evolve(PersonalityProfile.TraitAxis.INTROVERT, HURT_EVOLVE_DELTA);
            this.personality.evolve(PersonalityProfile.TraitAxis.SENSING, HURT_EVOLVE_DELTA); // Focus on immediate facts
        }
        return wasHurt;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            // Determine willingness to share info based on T/F
            float feelingTrait = this.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.FEELING);
            float thinkingTrait = this.getPersonalityProfile().getTraitValue(PersonalityProfile.TraitAxis.THINKING);
            float feelingFactor = (feelingTrait - thinkingTrait); // Ranges -1 (Strong T) to +1 (Strong F)
            // Feeling increases chance, Thinking decreases it
            float shareChance = BASE_INTERACTION_INFO_CHANCE + (feelingFactor * PERSONALITY_INFO_FACTOR);
            shareChance = Math.max(0.1f, Math.min(1.0f, shareChance)); // Clamp chance 10%-100%

            if (this.random.nextFloat() < shareChance) {
                // Display info only if check passes
                player.displayClientMessage(Component.literal("--- Lag0s Info ---"), false);
                player.displayClientMessage(Component.literal("Personality: " + personality.toString()), false);
                player.displayClientMessage(Component.literal("Rank: " + this.societalRank.name() + " - " + this.societalRank.getDescription()), false); 
                this.getTrade().ifPresent(t -> 
                    player.displayClientMessage(Component.literal("Trade: " + t.name() + " - " + t.getModernRole()), false)
                );
                player.displayClientMessage(Component.literal(String.format("STATS: Perception=%.2f XP: %.2f / %.2f", 
                    getCapabilityStatValue(CapabilityStat.PERCEPTION), 
                    getCapabilityXP(CapabilityStat.PERCEPTION), 
                    getXpThreshold(CapabilityStat.PERCEPTION))), false);
                player.displayClientMessage(Component.literal(String.format("SURVIVAL=%.2f XP: %.2f / %.2f", 
                    getCapabilityStatValue(CapabilityStat.SURVIVAL), 
                    getCapabilityXP(CapabilityStat.SURVIVAL), 
                    getXpThreshold(CapabilityStat.SURVIVAL))), false);
                player.displayClientMessage(Component.literal(String.format("SOCIAL XP +%.2f -> %.2f", 
                     SOCIAL_XP_PER_INTERACTION, getCapabilityXP(CapabilityStat.SOCIAL) + SOCIAL_XP_PER_INTERACTION)), false); 
            } else {
                 player.displayClientMessage(Component.literal("Lag0s seems preoccupied..."), false);
            }

            // Grant Social XP & Evolve personality regardless of sharing info
            this.gainXp(CapabilityStat.SOCIAL, SOCIAL_XP_PER_INTERACTION);
            this.personality.evolve(PersonalityProfile.TraitAxis.EXTRAVERT, SOCIAL_EVOLVE_DELTA);
            this.personality.evolve(PersonalityProfile.TraitAxis.FEELING, SOCIAL_EVOLVE_DELTA);
        }
        return InteractionResult.SUCCESS;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    public static boolean checkMobSpawnRules(EntityType<? extends PathfinderMob> entityType, LevelAccessor level, BlockPos pos, RandomSource random) {
        return true;
    }

    /* Old attribute registration - commented out
    public static void registerAttributes() {
        // ...
    }
    */

    // Add getters for Capability Stats/XP (more complex logic later)
    public float getCapabilityStatValue(CapabilityStat stat) {
        return this.capabilityStats.getOrDefault(stat, INITIAL_STAT_VALUE);
    }

    public float getCapabilityXP(CapabilityStat stat) {
        return this.capabilityXP.getOrDefault(stat, INITIAL_XP_VALUE);
    }

    // Method to calculate XP threshold for the *next* level of a stat
    private float getXpThreshold(CapabilityStat stat) {
        float currentLevelFactor = getCapabilityStatValue(stat) / INITIAL_STAT_VALUE; // Rough level factor
        // Simple scaling example: base * (scaling ^ (level - 1))
        // Since our stat value isn't integer levels, we use the factor
        return BASE_XP_THRESHOLD * (float)Math.pow(XP_THRESHOLD_SCALING, Math.max(0, currentLevelFactor - 1)); 
    }

    // Method to calculate and apply the speed modifier based on MOBILITY
    private void updateMobilitySpeedModifier() {
        AttributeInstance speedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        // Remove existing modifier using its UUID
        speedAttribute.removeModifier(MOBILITY_MODIFIER_ID);
        // No need to track the modifier object itself anymore if we always remove by ID
        // this.mobilitySpeedModifier = null; 

        // Calculate new modifier value 
        float mobilityValue = getCapabilityStatValue(CapabilityStat.MOBILITY);
        double baseSpeed = speedAttribute.getBaseValue(); 
        double addedSpeed = baseSpeed * mobilityValue * MOBILITY_SPEED_FACTOR;

        // Create and add the new modifier
        AttributeModifier newModifier = new AttributeModifier(MOBILITY_MODIFIER_ID, 
                                                         "Mobility Bonus", 
                                                         addedSpeed, 
                                                         AttributeModifier.Operation.ADDITION); 
        
        speedAttribute.addPermanentModifier(newModifier);
        
        Lag0sMod.LOGGER.debug("Applied speed modifier {} based on mobility {}", addedSpeed, mobilityValue);
    }

    // Override levelUpStat to flag speed update
    private void levelUpStat(CapabilityStat stat) {
        float currentValue = getCapabilityStatValue(stat);
        if (currentValue < MAX_STAT_VALUE) { 
            float newValue = Math.min(MAX_STAT_VALUE, currentValue + 0.1f); 
            capabilityStats.put(stat, newValue);
            Lag0sMod.LOGGER.info("{} leveled up {} to {}", this.getName().getString(), stat.name(), newValue);
            
            // Flag for speed update if MOBILITY changed
            if (stat == CapabilityStat.MOBILITY) {
                this.needsSpeedUpdate = true;
            }
        }
    }

    // Gain XP and check for level up
    public void gainXp(CapabilityStat stat, float amount) {
        if (amount <= 0) return;

        float currentXP = getCapabilityXP(stat);
        float threshold = getXpThreshold(stat);
        float newXP = currentXP + amount;

        Lag0sMod.LOGGER.debug("Gaining {} XP for {}. Current: {}, Threshold: {}", amount, stat.name(), currentXP, threshold); // Debug log

        while (newXP >= threshold) {
            levelUpStat(stat);
            newXP -= threshold;
            threshold = getXpThreshold(stat); // Recalculate threshold for the new level
             if (getCapabilityStatValue(stat) >= MAX_STAT_VALUE) {
                newXP = 0; // Cap XP if max level reached to prevent infinite loops
                break;
            }
        }

        capabilityXP.put(stat, newXP);
    }

    // Add a method to get the dynamic speed based on MOBILITY
    public float getDynamicSpeed() {
        float mobilityValue = getCapabilityStatValue(CapabilityStat.MOBILITY);
        // Adjust base speed attribute value based on mobility
        return BASE_MOVEMENT_SPEED * (1.0f + (mobilityValue * MOBILITY_SPEED_FACTOR));
    }

    // Override getMovementSpeed() to potentially use our dynamic speed
    // NOTE: This affects ALL movement, not just strolling. Consider carefully.
    /* 
    @Override
    public float getMovementSpeed() {
        // Could return getDynamicSpeed() here, but it might interfere with other systems.
        // A better approach might be needed, like modifying the attribute instance directly.
        return super.getMovementSpeed(); 
    }
    */
} 