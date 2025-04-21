package com.lagosai.entity;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;

public class PersonalityProfile {
    public enum TraitAxis { EXTRAVERT, INTROVERT, SENSING, INTUITION, THINKING, FEELING, JUDGING, PERCEIVING }
    private final EnumMap<TraitAxis, Float> traits = new EnumMap<>(TraitAxis.class);
    private static final Random rand = new Random();

    public PersonalityProfile() {
        for (TraitAxis t : TraitAxis.values()) traits.put(t, rand.nextFloat());
    }
    public void evolve(TraitAxis axis, float delta) { traits.compute(axis, (k,v) -> Math.max(0f, Math.min(1f, v != null ? v+delta : delta))); }
    public String getMBTIType() {
        StringBuilder mbti = new StringBuilder();
        mbti.append(traits.getOrDefault(TraitAxis.EXTRAVERT, 0f) >= traits.getOrDefault(TraitAxis.INTROVERT, 0f) ? "E" : "I");
        mbti.append(traits.getOrDefault(TraitAxis.SENSING, 0f) >= traits.getOrDefault(TraitAxis.INTUITION, 0f) ? "S" : "N");
        mbti.append(traits.getOrDefault(TraitAxis.THINKING, 0f) >= traits.getOrDefault(TraitAxis.FEELING, 0f) ? "T" : "F");
        mbti.append(traits.getOrDefault(TraitAxis.JUDGING, 0f) >= traits.getOrDefault(TraitAxis.PERCEIVING, 0f) ? "J" : "P");
        return mbti.toString();
    }
    @Override public String toString() { return getMBTIType()+traits; }

    public CompoundTag saveNBT(CompoundTag nbt) {
        CompoundTag traitsTag = new CompoundTag();
        for (Map.Entry<TraitAxis, Float> entry : traits.entrySet()) {
            traitsTag.putFloat(entry.getKey().name(), entry.getValue());
        }
        nbt.put("PersonalityTraits", traitsTag);
        return nbt;
    }

    public void loadNBT(CompoundTag nbt) {
        if (nbt.contains("PersonalityTraits", CompoundTag.TAG_COMPOUND)) {
            CompoundTag traitsTag = nbt.getCompound("PersonalityTraits");
            for (TraitAxis trait : TraitAxis.values()) {
                if (traitsTag.contains(trait.name(), CompoundTag.TAG_FLOAT)) {
                    traits.put(trait, traitsTag.getFloat(trait.name()));
                } else {
                    traits.put(trait, rand.nextFloat());
                }
            }
        } else {
            for (TraitAxis t : TraitAxis.values()) {
                traits.put(t, rand.nextFloat());
            }
        }
    }
} 