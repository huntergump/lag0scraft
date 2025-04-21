package com.lagosai.entity;

// Trades relevant primarily for Skilled Technician to Team Squire ranks
public enum Trade {
    BLACKSMITH_METALWORKER("Forge member", "Industrial welder / machinist"),
    MASON_STONECUTTER("Freemason", "Construction foreman"),
    CARPENTER_JOINER("Guild carpenter", "Site carpenter / cabinetmaker"),
    TANNER_LEATHERWORKER("Tanner's guild", "Leather goods craftsman"),
    WEAVER_TEXTILE_WORKER("Weaver's guild", "Textile technician / seamstress"),
    BREWER_DISTILLER("Brewery guild", "Quality-control brewer"),
    TAILOR_CLOTHIER("Tailor's guild", "Apparel designer / tailor"),
    CHANDLER_CANDLEMAKER("Chandler's guild", "Industrial chemist / lab tech"),
    COOPER_BARRELMAKER("Cooper's guild", "Logistics coordinator"), // Note: Cooper listed twice, using barrel context here
    CORDWAINER_SHOEMAKER("Cordwainer's guild", "Footwear technician / cobbler"),
    APOTHECARY_HEALER("Apothecaries' guild", "Pharmacist / clinical lab tech"),
    SCRIBE_CLERK("Scribal guild", "Administrative assistant"),
    MILLER_GRAIN_WORKER("Miller's guild", "Food-processing technician"),
    WOODWORKER("Cooper's guild", "Furniture maker / cabinetmaker"), // Using generic Woodworker for 2nd Cooper entry
    ARCHER_ARMORER("Armorer's guild", "Weapons technician / armour tech"),
    HERBALIST_BOTANIST("Herbalists' guild", "Agricultural extension officer"),
    STONEMASON_SCULPTOR("Guild of masons and sculptors", "Stone restoration specialist");

    private final String medievalRole;
    private final String modernRole;

    Trade(String medievalRole, String modernRole) {
        this.medievalRole = medievalRole;
        this.modernRole = modernRole;
    }

    public String getMedievalRole() {
        return medievalRole;
    }

    public String getModernRole() {
        return modernRole;
    }

    @Override
    public String toString() {
        // Return the enum name by default
        return this.name();
    }
} 