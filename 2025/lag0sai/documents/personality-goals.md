# Lag0s Entity Development Plan

This plan outlines the development steps for the Lag0s AI NPC, incorporating capability stats, personality traits, societal rank, trade, and a needs-based goal hierarchy.

## Core AI Driver: Hierarchy of Needs

The NPC's goal selection and prioritization will be fundamentally driven by an adapted Maslow's Hierarchy:

1.  **Survival (Highest Priority):** Basic physiological needs and immediate safety.
    *   *Examples:* Seeking warmth/light, resting (shelter/safety), avoiding immediate threats (hostile mobs, environmental hazards). Food/hunger is a vanilla mechanic but active goal-driven seeking is deferred.
2.  **Comfort & Safety:** Establishing security and stability.
    *   *Examples:* Seeking/building dedicated shelter, acquiring basic resources (wood, stone), obtaining basic tools/currency.
3.  **Social:** Belonging and interaction.
    *   *Examples:* Interacting with players or other NPCs, forming connections (if implemented), participating in community activities.
4.  **Purpose:** Esteem, achievement, and progression within the defined structures.
    *   *Examples:* Goals related to the NPC's `Trade` (crafting, gathering specific resources), goals related to achieving `SocietalRank` requirements (skill thresholds, specific tasks).
5.  **Winning / Self-Actualization (Lowest Priority / Default State):** State when lower needs are met and current purpose goals are achieved.
    *   *Examples:* Idle behaviors, leisure activities, seeking knowledge (`LEARNING` XP), generating new long-term goals.

*   **Influence:** Capability Stats determine *effectiveness*. Personality Traits influence *style* and *preference*.

---

## Development Steps

### 1. Implement Core Stat, Trait, Rank & Trade Persistence (NBT)

*   **Status:** DONE
*   **Goal:** Make the entity remember its core attributes when the game saves and loads.
*   **(Sub-steps completed)**

### 2. Develop Capability Stat Logic & Progression

*   **Status:** DONE (Basic system)
*   **Goal:** Define how capability stats improve through experience.
*   **How:**
    *   Implement `gainXp` and `checkLevelUp` methods (DONE).
    *   Hook basic entity actions to grant XP (DONE for Mobility, Social, Perception, Survival).
    *   *Future:* Add more XP triggers.

### 3. Develop Personality Logic & Evolution

*   **Status:** DONE (Basic system)
*   **Goal:** Define how MBTI traits translate into categories/flags and evolve based on experience.
*   **How:**
    *   Implement `getMBTIType()` (DONE).
    *   Implement `personality.evolve()` based on outcomes (DONE for basic interact/hurt triggers).
    *   *Future:* Refine evolution logic, add more triggers, add helper methods if needed.

### 4. Implement AI Goals Based on Hierarchy & Attributes

*   **Status:** IN PROGRESS
*   **Goal:** Implement AI goals prioritized by the Hierarchy of Needs and influenced by Stats/Personality.
*   **How:**
    *   **Survival Goals:**
        *   `SurvivalPanicGoal` (DONE - Reactive flee when hurt, influenced by Survival/T/F).
        *   `AvoidEntityGoal` (NEXT - Innate proactive avoidance of hostiles).
        *   `FindShelterOrLightGoal` (NEXT - Innate seeking safety at night/during storms).
        *   *Future:* `MeleeAttackGoal` (Add basic combat ability).
        *   *Future:* Fight/Flee Decision Logic (Choose between panic, avoid, attack based on context, stats, personality).
    *   **Comfort/Safety Goals:** *Future:* GatherResourcesGoal, BuildSimpleShelterGoal.
    *   **Social Goals:** `LookAtPlayerBasedOnPerceptionGoal` (DONE), *Future:* FollowPlayerGoal, InteractWithVillagerGoal.
    *   **Capability/Personality Integration:** Continue refining how stats/traits influence existing and new goal parameters and selection (Partially DONE).
    *   **Hook XP Gain:** Ensure new goals grant appropriate Capability XP (Ongoing).

### 5. Develop Rank & Trade Progression Logic (Future Step)

*   **Status:** NOT STARTED
*   **Goal:** Define how an NPC progresses ranks and potentially chooses/changes trades, incorporating scarcity & competition.
*   **How (Ideas):** Rank Criteria (stats, tasks), Trade Selection (rank, stats, personality), Progression Control (exponential requirements, limited slots, success chance, maintenance), Competition (interaction between NPCs for limited slots).

### 6. Implement Trade & Purpose-Driven AI Goals (Future Step)

*   **Status:** NOT STARTED
*   **Goal:** Add goals specific to the NPC's chosen Trade and Rank aspirations.
*   **How:** Custom goals (e.g., `GoalSeekWorkstation`, `GoalMeetRankCriteria`), prioritize based on Hierarchy (Level 4).

### 7. Refine Appearance (Lower Priority)

*   **Status:** NOT STARTED
*   **Goal:** Improve visual presentation.
*   **How:** Default entity texture, spawner item texture/model, optional Rank/Trade visuals.

### 8. Refine Spawner Item (Lower Priority)

*   **Status:** NOT STARTED
*   **Goal:** Determine how players obtain the spawner item in survival.
*   **How:** Crafting recipe or loot table entry.

### 9. Future Enhancements / Advanced AI

*   **Recovery Score:** Implement a system tracking rest/exertion to influence performance and mood.
*   **Advanced Needs:** Implement active hunger/thirst management goals.
*   **Complex Learning:** Implement more sophisticated learning mechanisms beyond simple XP gain.
*   **NPC<->NPC Interaction:** Develop goals and systems for NPCs interacting with each other (social groups, competition, collaboration).

---

**Recommended Next Step:** Continue implementing step #4 (Implement AI Goals Based on Hierarchy & Attributes), focusing on innate Survival goals: `AvoidEntityGoal` and `FindShelterOrLightGoal`.