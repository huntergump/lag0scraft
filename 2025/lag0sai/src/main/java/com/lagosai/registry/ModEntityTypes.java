package com.lagosai.registry;

import com.lagosai.Lag0sMod;
import com.lagosai.entity.Lag0sEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Lag0sMod.MOD_ID);

    public static final RegistryObject<EntityType<Lag0sEntity>> LAG0S = ENTITIES.register("lag0s",
        () -> EntityType.Builder.of(Lag0sEntity::new, MobCategory.CREATURE)
                .sized(0.8F, 1.0F)
                .build("lag0s")
    );
} 