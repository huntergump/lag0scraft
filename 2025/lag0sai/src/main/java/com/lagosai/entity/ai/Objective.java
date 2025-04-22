package com.lagosai.entity.ai;

// Represents the high-level objective the entity is currently focused on.
public enum Objective {
    ENSURE_SURVIVAL,    // Immediate safety (fleeing, finding shelter/light)
    IMPROVE_COMFORT,    // Medium-term safety/resources (gathering, building)
    SOCIALIZE,          // Interaction with others
    PURSUE_PURPOSE,     // Goals related to Rank/Trade
    EXPLORE_LEARN,      // Wandering with intent to gain XP or map area
    IDLE                // Default state when other needs/objectives are met
} 