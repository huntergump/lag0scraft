package com.lagosai.client.model;

import com.lagosai.entity.Lag0sEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

// Basic placeholder model - just a single cube
public class Lag0sModel<T extends Lag0sEntity> extends HumanoidModel<T> {
    public Lag0sModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Call the superclass animation setup for standard humanoid movement
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        // We can add custom animations based on personality later
    }
} 