package com.lagosai.entity;

public enum CapabilityStat {
    PERCEPTION, // How far/smart they "see" (detect blocks, entities, events)
    MOBILITY,   // Movement speed, jump height, pathfinding agility
    SURVIVAL,   // Health, damage resistance, hunger/energy management (Note: May interact with base Attributes)
    CRAFTING,   // Ability to manipulate blocks, build structures, use tools
    SOCIAL,     // Willingness/ability to trade, follow, or collaborate
    LEARNING    // How quickly they form new behaviors or gain XP
} 