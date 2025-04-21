package com.lagosai.registry;

import net.minecraft.world.item.Item;
// import net.minecraftforge.common.ForgeSpawnEggItem; // Re-comment this
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.lagosai.Lag0sMod;
import com.lagosai.item.Lag0sSpawnerItem; // Import the new item

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, Lag0sMod.MOD_ID);

    /* // Re-comment spawn egg
    public static final RegistryObject<ForgeSpawnEggItem> LAG0S_SPAWN_EGG = ITEMS.register("lag0s_spawn_egg",
        () -> new ForgeSpawnEggItem(
            ModEntityTypes.LAG0S,
            0x000000,
            0x9f00ff,
            new Item.Properties()
        )
    );
    */

    // Register the new spawner item
    public static final RegistryObject<Item> LAG0S_SPAWNER = ITEMS.register("lag0s_spawner",
        () -> new Lag0sSpawnerItem(new Item.Properties()));
} 