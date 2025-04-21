package com.lagosai.client.renderer;

import com.lagosai.Lag0sMod;
import com.lagosai.client.model.Lag0sModel;
import com.lagosai.entity.Lag0sEntity;
import com.lagosai.client.model.ModelLayerLocations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import java.util.Optional;
import java.util.UUID;

public class Lag0sRenderer extends HumanoidMobRenderer<Lag0sEntity, Lag0sModel<Lag0sEntity>> {
    // Define a placeholder texture location
    private static final ResourceLocation DEFAULT_TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(Lag0sMod.MOD_ID, "textures/entity/lagos_placeholder.png");

    public Lag0sRenderer(EntityRendererProvider.Context context) {
        // Call HumanoidMobRenderer constructor, passing our model layer
        // The next two nulls are for inner/outer armor layers, which we don't have
        super(context, new Lag0sModel<>(context.bakeLayer(ModelLayerLocations.LAGOS)), 0.5f); 
    }

    @Override
    public ResourceLocation getTextureLocation(Lag0sEntity entity) {
        Optional<UUID> ownerUUID = entity.getOwnerUUID();
        if (ownerUUID.isPresent()) {
            try {
                UUID uuid = ownerUUID.get();
                // Get the network handler to look up player info
                ClientPacketListener handler = Minecraft.getInstance().getConnection();
                if (handler != null) {
                    var playerInfo = handler.getPlayerInfo(uuid);
                    if (playerInfo != null) {
                        // Get skin texture location from PlayerInfo
                        ResourceLocation skinTexture = playerInfo.getSkinLocation();
                        if (skinTexture != null) {
                            return skinTexture;
                        }
                    }
                }
                // Fallback to default Steve/Alex based on UUID
                 return DefaultPlayerSkin.getDefaultSkin(uuid);
            } catch (Exception e) {
                Lag0sMod.LOGGER.warn("Failed to get skin for UUID: {}", ownerUUID.get(), e);
            }
        }
        // Fallback to our placeholder texture if no UUID or error
        return DEFAULT_TEXTURE;
    }
} 