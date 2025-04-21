package com.lagosai.entity;

// Based on the provided hierarchy
public enum SocietalRank {
    FIELD_ASSOCIATE("Peasant / Front-line Worker"),
    SKILLED_TECHNICIAN("Villein / Skilled Contractor"),
    GUILD_SPECIALIST("Artisan / Professional"),
    COMMERCE_CAPTAIN("Merchant / Entrepreneur"),
    TEAM_SQUIRE("Minor Gentry / Team Lead"),
    CORPORATE_KNIGHT("Knight / Mid-Level Manager"),
    DIRECTOR_BARON("Baron / Senior Manager"),
    VICE_VISCOUNT("Viscount / Vice President"),
    EARL_SVP("Count/Earl / Senior Vice President"),
    MARGRAVE_EXECUTIVE("Marquess / C-Suite Officer"),
    DUKE_CEO("Duke / Chief Executive Officer"),
    SOVEREIGN_CHAIR("King / Board Chair or Head of State");

    private final String description;

    SocietalRank(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // Optional: Override toString for cleaner logging/display if needed
    @Override
    public String toString() {
        // Default toString() uses the enum constant name (e.g., FIELD_ASSOCIATE)
        // We could customize it here if desired, e.g., return this.name() + " (" + description + ")";
        return super.toString(); 
    }
} 