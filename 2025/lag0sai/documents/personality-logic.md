# Lag0s NPC AI Logic Framework

This document outlines the interconnected systems governing the behavior, progression, and state of the Lag0s NPC.

## I. Core Driver: Hierarchy of Needs

This system, inspired by Maslow's hierarchy, provides the fundamental motivation and prioritization for the NPC's actions. Goals are generally selected based on the lowest unmet need.

1.  **Survival (Highest Priority):** Immediate physiological needs and safety.
    *   *Goal Examples:* Avoid Hostiles, Panic/Flee when hurt, Seek Shelter/Light (at night/bad weather), Find Food (Future).
    *   *Focus:* Staying alive, avoiding damage, basic environmental safety.
2.  **Comfort & Safety:** Establishing security and stability.
    *   *Goal Examples:* Gather Basic Resources (Wood, Stone), Build Simple Shelter.
    *   *Focus:* Creating a secure base, acquiring essential materials.
3.  **Social:** Belonging and interaction.
    *   *Goal Examples:* LookAtPlayer, InteractWithPlayer (when possible), FollowPlayer (conditional), InteractWithNPCs (Future).
    *   *Focus:* Engaging with other entities.
4.  **Purpose:** Esteem, achievement, and progression.
    *   *Goal Examples:* Goals related to Trade (Gather specific resources, Craft items), Goals related to Rank (Meet stat/task requirements).
    *   *Focus:* Specializing, achieving status, pursuing defined roles.
5.  **Winning / Self-Actualization:** State when lower needs and current goals are met.
    *   *Goal Examples:* Idle behaviors, Explore (different from basic wandering), Seek Knowledge (gain LEARNING XP), Generate new long-term Purpose goals.
    *   *Focus:* High-level functioning when basic needs are satisfied.

## II. Capability Stats (Effectiveness & Skill)

These represent the NPC's learned skills and raw abilities in performing tasks. They progress through XP gain from relevant actions.

*   **Stat List:**
    *   `PERCEPTION`: Affects detection range, awareness of environment/entities.
    *   `MOBILITY`: Affects movement speed, agility, pathfinding success.
    *   `SURVIVAL`: Affects resilience, health management, effectiveness of panic/avoidance.
    *   `CRAFTING`: Affects ability to manipulate blocks, build, use tools.
    *   `SOCIAL`: Affects effectiveness of social interactions (trading, negotiation - future).
    *   `LEARNING`: Affects the rate of XP gain across other stats.
*   **Progression:**
    *   Tracked via `capabilityXP` map (`float`).
    *   Increased by `gainXp(CapabilityStat stat, float amount)` when performing related actions.
    *   Stat *value* (0.0-1.0, stored in `capabilityStats` map) increases via `levelUpStat(CapabilityStat stat)` when XP threshold is met.
    *   XP threshold scales with current stat value (`getXpThreshold`).
    *   XP generally only increases; the stat value represents the current peak skill level.
*   **NBT Persistence:** Both `capabilityStats` and `capabilityXP` maps are saved.

## III. Personality Traits (Style & Preference)

Based on MBTI axes, these traits determine *how* an NPC prefers to behave and make decisions. They evolve slowly based on experiences.

*   **Axes (stored in `PersonalityProfile.traits` map):**
    *   `EXTRAVERT` / `INTROVERT`: Social orientation.
    *   `SENSING` / `INTUITION`: Information gathering preference.
    *   `THINKING` / `FEELING`: Decision-making preference.
    *   `JUDGING` / `PERCEIVING`: Lifestyle orientation (structured vs. spontaneous).
*   **Evolution:**
    *   Traits adjusted via `personality.evolve(TraitAxis axis, float delta)`.
    *   Currently evolves based on `hurt` (-> Introvert, Sensing) and `mobInteract` (-> Extravert, Feeling).
    *   Future: Add more evolution triggers based on action outcomes (success/failure), environment, etc.
*   **Interpretation:**
    *   `getMBTIType()` provides a 4-letter summary.
    *   `getTraitValue(TraitAxis axis)` provides the raw float value.
*   **NBT Persistence:** `PersonalityProfile` saves/loads its `traits` map.

## IV. Societal Rank (Status & Progression Path)

Represents the NPC's standing within a defined hierarchy. Progression is milestone-based, not XP-based.

*   **Ranks:** Defined in `SocietalRank` enum (FIELD_ASSOCIATE to SOVEREIGN_CHAIR), each with a description.
*   **Progression (Future - Step 5):**
    *   NPCs start at `FIELD_ASSOCIATE`.
    *   Advancement requires meeting criteria (Capability Stat thresholds, task completion, etc.).
    *   Higher ranks are designed to be progressively rarer (Scarcity/Competition mechanics needed).
*   **NBT Persistence:** Current `societalRank` is saved/loaded.

## V. Trade (Specialization)

Provides a specific role or skill focus, primarily relevant for mid-tier ranks.

*   **Trades:** Defined in `Trade` enum (BLACKSMITH, CARPENTER, etc.), each with descriptions.
*   **Assignment (Future - Step 5):**
    *   NPCs start with no trade (`Optional.empty()`).
    *   Trade selection/assignment likely tied to reaching specific ranks (e.g., SKILLED_TECHNICIAN) and potentially influenced by high Capability Stats or Personality.
*   **Progression (Future):** Could potentially have its own XP/Mastery system to unlock trade-specific abilities or recipes.
*   **NBT Persistence:** Current `trade` (if present) is saved/loaded.

## VI. System Interactions & Influences

These systems are designed to work together:

*   **Hierarchy -> Goal Selection:** The primary driver. Goals fulfilling lower unmet needs generally take priority.
*   **Stats -> Goal Effectiveness:** Capability Stats directly impact the *success* or *parameters* of AI Goals (e.g., Perception range, Mobility speed, Survival panic chance, Crafting success - future).
*   **Personality -> Goal Style/Preference:** Personality Traits influence the *likelihood* of choosing certain goals (e.g., E/I affecting social vs. solitary goals, J/P affecting wandering vs. task focus) and the *parameters* within goals (e.g., S/N affecting look duration).
*   **Actions -> Stats & Personality:** Performing actions grants Capability XP (`gainXp`), and the outcome or context of actions can trigger Personality Evolution (`evolve`).
*   **Stats/Tasks -> Rank/Trade:** Achieving specific Capability Stat levels and completing certain tasks will be the criteria for progressing in Rank and potentially selecting a Trade (Future).
*   **Rank/Trade -> Goals:** Higher Ranks or specific Trades will unlock new, more complex AI Goals related to Purpose (Future).
