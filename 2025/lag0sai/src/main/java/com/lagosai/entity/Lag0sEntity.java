package com.lagosai.entity;

import com.lagosai.Lag0sMod;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
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

public class Lag0sEntity extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID_ID = 
        SynchedEntityData.defineId(Lag0sEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private final PersonalityProfile personality = new PersonalityProfile();
    private Optional<UUID> ownerUUID = Optional.empty();
    private SocietalRank societalRank = SocietalRank.FIELD_ASSOCIATE;
    private Optional<Trade> trade = Optional.empty();

    public Lag0sEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
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

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            player.displayClientMessage(Component.literal("Lag0s personality: " + personality.toString()), false);
            player.displayClientMessage(Component.literal("Rank: " + this.societalRank.name() + " - " + this.societalRank.getDescription()), false);
            this.getTrade().ifPresent(t -> 
                player.displayClientMessage(Component.literal("Trade: " + t.name() + " - " + t.getModernRole()), false)
            );
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
} 