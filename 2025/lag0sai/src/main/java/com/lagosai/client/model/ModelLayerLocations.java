package com.lagosai.client.model;

import com.lagosai.Lag0sMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModelLayerLocations {
    // Define a unique ModelLayerLocation for our entity
    public static final ModelLayerLocation LAGOS = 
        new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Lag0sMod.MOD_ID, "lagos"), "main");
} 