# Lag0s Entity Development Plan

This plan outlines the development steps for the Lag0s AI NPC, incorporating capability stats, personality traits, societal rank, and trade.

## 1. Implement Core Stat, Trait, Rank & Trade Persistence (NBT)

*   **Goal:** Make the entity remember its core attributes when the game saves and loads.
*   **Capability Stats:**
    *   Define stats in `Lag0sEntity` (e.g., Perception, Mobility, Survival, Crafting, Social, Learning).
    *   Define XP pools/thresholds (XP should generally only increase).
    *   Add NBT saving/loading for stats and XP.
*   **Personality Traits (MBTI):**
    *   Add `saveNBT`/`loadNBT` methods to `PersonalityProfile`.
    *   Call these methods in `Lag0sEntity`.
*   **Societal Rank:**
    *   Define `SocietalRank` enum with descriptions.
    *   Add `societalRank` field to `Lag0sEntity`, defaulting to `FIELD_ASSOCIATE`.
    *   Add NBT saving/loading for the rank.
*   **Trade:**
    *   Define `Trade` enum with descriptions.
    *   Add `Optional<Trade> trade` field to `Lag0sEntity`.
    *   Add NBT saving/loading for the trade.

## 2. Develop Capability Stat Logic & Progression

*   **Goal:** Define how capability stats improve through experience and affect actions.
*   **How:**
    *   Implement `gainXp(CapabilityStat stat, float amount)` method in `Lag0sEntity`. XP gain reflects practice/learning and should generally be positive.
    *   Implement `checkLevelUp(CapabilityStat stat)` method in `Lag0sEntity` to increase the *stat value* when XP threshold is met. Reset/scale XP requirement for the next level.
    *   Hook basic entity actions (e.g., moving, interacting, crafting attempts, taking damage, observing) to grant small amounts of XP to relevant stats (`MOBILITY`, `SOCIAL`, `CRAFTING`, `SURVIVAL`, `PERCEPTION`, etc.).

## 3. Develop Personality Logic & Evolution

*   **Goal:** Define how MBTI traits translate into categories/flags and evolve based on experience.
*   **How:**
    *   Implement `getMBTIType()` in `PersonalityProfile` based on trait values.
    *   Add helper methods as needed (e.g., `isDominant`, `getTraitValue`).
    *   Implement logic using `personality.evolve()` based on outcomes of actions or significant events (e.g., successful trade increases SOCIAL, failed exploration attempt might shift SENSING/INTUITION).

## 4. Develop Rank & Trade Progression Logic (Future Step)

*   **Goal:** Define how an NPC progresses ranks and potentially chooses/changes trades, incorporating scarcity.
*   **How (Ideas):**
    *   **Rank Criteria:** Tie rank advancement primarily to reaching specific thresholds in key Capability Stats, potentially combined with completing specific tasks or acquiring items/knowledge.
    *   **Trade Selection:** Link trade choice/assignment to specific ranks (e.g., available from Skilled Technician onwards) and potentially influence selection based on high Capability Stats or Personality Traits.
    *   **Progression Control & Scarcity:**
        *   **Goal:** Ensure higher ranks are difficult to attain, reflecting rarity.
        *   **Methods (Ideas):** Exponential requirements (stats, tasks, resources), limited high-rank slots (requiring competition/waiting), success chance for promotion attempts, rank maintenance requirements.
    *   Implement `attemptRankUp()` / `chooseTrade()` logic triggered periodically or by events.

## 5. Implement Basic AI Incorporating Stats, Personality, Rank & Trade

*   **Goal:** Start making entity actions reflect its attributes.
*   **How:**
    *   **Capability Checks:** Modify AI goals based on stats.
    *   **XP Awarding:** Award XP via `gainXp`.
    *   **Personality Influence:** Modify goal selection/parameters based on personality.
    *   **Rank/Trade Influence (Future):** Goals could change based on rank/trade.
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

**Recommended Next Step:** Continue implementing step #2 (Capability Stat Logic & Progression - Awarding XP).