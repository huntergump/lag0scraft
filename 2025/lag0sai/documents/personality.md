# Lag0s Entity Development Plan

This plan outlines the development steps for the Lag0s AI NPC, incorporating capability stats, personality traits, societal rank, and trade.

## 1. Implement Core Stat, Trait, Rank & Trade Persistence (NBT)

*   **Goal:** Make the entity remember its core attributes when the game saves and loads.
*   **Capability Stats:**
    *   Define stats in `Lag0sEntity` (e.g., Perception, Mobility, Survival, Crafting, Social, Learning).
    *   Define XP pools/thresholds.
    *   Add NBT saving/loading for stats and XP.
*   **Personality Traits (MBTI):**
    *   Add `saveNBT`/`loadNBT` methods to `PersonalityProfile`.
    *   Call these methods in `Lag0sEntity`.
*   **Societal Rank:**
    *   Define `SocietalRank` enum.
    *   Add `societalRank` field to `Lag0sEntity`, defaulting to `FIELD_ASSOCIATE`.
    *   Add NBT saving/loading for the rank.
*   **Trade:**
    *   Define `Trade` enum.
    *   Add `Optional<Trade> trade` field to `Lag0sEntity`.
    *   Add NBT saving/loading for the trade.

## 2. Develop Capability Stat Logic & Progression

*   **Goal:** Define how capability stats improve and affect actions.
*   **How:**
    *   Implement `gainXp` and `checkLevelUp` methods in `Lag0sEntity`.
    *   Hook basic actions to grant XP.

## 3. Develop Personality Logic & Interpretation

*   **Goal:** Define how MBTI traits translate into categories/flags.
*   **How:**
    *   Implement `getMBTIType()` in `PersonalityProfile`.
    *   Add helper methods as needed (e.g., `isDominant`, `getTraitValue`).

## 4. Develop Rank & Trade Progression Logic (Future Step)

*   **Goal:** Define how an NPC progresses ranks and potentially chooses/changes trades.
*   **How (Ideas):**
    *   Tie rank advancement to reaching certain thresholds in key Capability Stats (e.g., high Crafting needed for Guild Specialist).
    *   Require completion of specific complex tasks or achievements.
    *   Trade selection could be influenced by stats/personality or specific interactions/tasks.
    *   Implement `attemptRankUp()` / `chooseTrade()` logic.
*   **Progression Control & Scarcity:**
    *   **Goal:** Ensure higher ranks are progressively harder to attain, reflecting rarity (inspired by target distributions, e.g., only a tiny % reaching Sovereign Chair).
    *   **Methods (Ideas):**
        *   **Exponential Requirements:** Make the capability stat thresholds, required resources, or task complexity for rank-up increase significantly for higher ranks.
        *   **Limited Slots (Advanced):** Potentially simulate a limited number of positions available for the highest ranks within a region or globally, requiring NPCs to wait for an opening or even compete.
        *   **Success Chance:** Introduce a chance-based element for promotion attempts to higher ranks, influenced by stats, personality, or even world events.
        *   **Maintenance:** Higher ranks might require ongoing actions or resource upkeep to maintain, adding a decay or demotion risk.

## 5. Implement Basic AI Incorporating Stats, Personality, Rank & Trade

*   **Goal:** Start making entity actions reflect its attributes.
*   **How:**
    *   **Capability Checks:** Modify AI goals based on stats.
    *   **XP Awarding:** Award XP via `gainXp`.
    *   **Personality Influence:** Modify goal selection/parameters based on personality.
    *   **Rank/Trade Influence (Future):** Goals could change based on rank/trade (e.g., a Blacksmith seeks ore, a Guild Specialist might have teaching goals).
    *   *Later:* Add more complex custom goals.

## 6. Refine Appearance (Lower Priority)

*   **Goal:** Improve visual presentation.
*   **How:**
    *   Create default entity texture and update fallback in `Lag0sRenderer`.
    *   Create texture/model for `Lag0sSpawnerItem`.
    *   *Optional:* Visual changes based on Rank/Trade.

## 7. Refine Spawner Item (Lower Priority)

*   **Goal:** Determine how players obtain the spawner item in survival.
*   **How:** Add crafting recipe or loot table entry.

---

**Recommended Next Step:** Implement step #2 (Capability Stat Logic & Progression).