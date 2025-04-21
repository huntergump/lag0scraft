package com.lagosai.item;

import com.lagosai.entity.Lag0sEntity;
import com.lagosai.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;

public class Lag0sSpawnerItem extends Item {

    public Lag0sSpawnerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS; // Only run on server
        }

        Player player = pContext.getPlayer();
        BlockPos clickedPos = pContext.getClickedPos();
        Vec3 spawnPos = Vec3.atBottomCenterOf(clickedPos).relative(pContext.getClickedFace(), 1.0);

        // Spawn the entity
        Lag0sEntity lagosEntity = ModEntityTypes.LAG0S.get().spawn(
            serverLevel,
            pContext.getItemInHand(), // Pass item stack for context
            player, // Pass player for context
            BlockPos.containing(spawnPos), 
            null, // Still passing null for MobSpawnType
            true, 
            !clickedPos.equals(BlockPos.containing(spawnPos))
        );

        // Set the owner UUID if the entity spawned and player exists
        if (lagosEntity != null && player != null) {
            lagosEntity.setOwnerUUID(player.getUUID());
        }

        // Consume item if not in creative
        if (player == null || !player.getAbilities().instabuild) {
            pContext.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
} 