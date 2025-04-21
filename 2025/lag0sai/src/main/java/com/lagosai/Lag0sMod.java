package com.lagosai;

import com.lagosai.entity.Lag0sEntity;
import com.lagosai.registry.ModEntityTypes;
import com.lagosai.registry.ModItems;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
// import net.minecraft.world.level.levelgen.Heightmap; // Keep commented
// import net.minecraft.world.entity.SpawnPlacements; // Keep commented
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
// import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent; // Keep commented
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// import com.lagosai.client.model.Lag0sModel; // Keep commented or remove if not directly used here
import com.lagosai.client.model.ModelLayerLocations;
import com.lagosai.client.renderer.Lag0sRenderer;
import net.minecraft.client.model.PlayerModel; // Add import
import net.minecraft.client.model.geom.builders.LayerDefinition; // Keep import
import net.minecraft.client.model.geom.builders.MeshDefinition; // Add import
import net.minecraft.client.model.geom.builders.CubeDeformation; // Keep import

@Mod(Lag0sMod.MOD_ID)
public class Lag0sMod {
    public static final String MOD_ID = "lag0scraft";
    public static final Logger LOGGER = LogManager.getLogger();

    public Lag0sMod() {
        LOGGER.info("Lag0sMod Initializing!");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register Deferred Registers
        ModItems.ITEMS.register(modEventBus);
        ModEntityTypes.ENTITIES.register(modEventBus);

        // Register Listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::entityAttributeEvent);
        // modEventBus.addListener(this::entitySpawnRestriction); // Keep commented
        // MinecraftForge.EVENT_BUS.register(this); // Keep commented for now
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Initializing Lag0sCraft common setup");
        // World generation, capabilities, etc. might go here later
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.LAG0S_SPAWNER);
        }
        // If you uncomment the spawn egg later, add it here too:
        // if(event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
        //     event.accept(ModItems.LAG0S_SPAWN_EGG); // Make sure LAG0S_SPAWN_EGG is uncommented in ModItems
        // }
    }

    private void entityAttributeEvent(EntityAttributeCreationEvent event) {
        LOGGER.info("Registering Lag0sEntity attributes");
        event.put(ModEntityTypes.LAG0S.get(), Lag0sEntity.createAttributes().build());
    }

    /* // Keep entitySpawnRestriction method commented
    private void entitySpawnRestriction(SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntityTypes.LAG0S.get(),
                SpawnPlacements.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Lag0sEntity::checkMobSpawnRules
        );
    }
    */

    // Inner class for client-only event handling
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Could register keybindings, etc. here later
            LOGGER.info("Initializing Lag0sCraft client setup");
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            LOGGER.info("Registering Humanoid Lag0sModel layer definition");
            // Use PlayerModel.createMesh to get a standard humanoid mesh definition
            MeshDefinition meshDefinition = PlayerModel.createMesh(new CubeDeformation(0.0F), false); 
            // Create LayerDefinition from MeshDefinition (specify texture size 64x64 for player model)
            LayerDefinition layerDefinition = LayerDefinition.create(meshDefinition, 64, 64);
            event.registerLayerDefinition(ModelLayerLocations.LAGOS, () -> layerDefinition);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            LOGGER.info("Registering Lag0sRenderer");
            event.registerEntityRenderer(ModEntityTypes.LAG0S.get(), Lag0sRenderer::new);
        }
    }
}