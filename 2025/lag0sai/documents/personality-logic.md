# Lag0s NPC AI Logic Framework

This document outlines the interconnected systems governing the behavior, progression, and state of the Lag0s NPC, using a hybrid architecture inspired by LLM-agent principles but implemented with game-performance constraints in mind.

## I. Core Architecture: Reasoning Layer + Action Layer

1.  **Reasoning Layer (`BehaviorManager` - Conceptual):** Runs periodically (e.g., every ~1 second). Responsible for high-level decision making.
    *   **Assesses State:** Evaluates internal state (Needs, Stats, Personality, Rank, Trade) and relevant external context (Time, Weather, Nearby Entities/Blocks).
    *   **Consults Memory:** Reads recent events, known locations, interaction history from the `EntityMemory` component.
    *   **Prioritizes Need:** Determines the most pressing need based on the Hierarchy of Needs.
    *   **Selects Objective:** Chooses a high-level objective (e.g., `EnsureSafety`, `GatherResources`, `Socialize`, `PursuePurpose`) based on the primary need, influenced by Personality, Stats, Rank, Trade, and Memory.
    *   **Manages Goals:** Activates/deactivates specific low-level AI Goals in the Action Layer to achieve the selected objective. Manages conflicting goals (e.g., deactivates wandering when fleeing).
    *   **Updates Memory:** Logs significant events, discoveries, or outcomes back to the `EntityMemory`.
2.  **Action Layer (Minecraft Goal System):** Executes specific, low-level tasks.
    *   Comprises standard (`FloatGoal`, `AvoidEntityGoal`) and custom (`BreakBlockGoal`, `FindShelterGoal`, etc.) AI Goals.
    *   Goals are managed (added/removed) by the Reasoning Layer.
    *   Goals focus on execution (pathfinding, animation, interaction logic) and may directly grant Capability XP or trigger Personality evolution upon completion/ticks.
3.  **Memory Component (`EntityMemory` - Conceptual):** Stores contextual information persistently via NBT.
    *   *Examples:* Last known threat location, home/shelter location, list of recent interactions, significant events (level ups, rank changes, major fights).

## II. Core Driver: Hierarchy of Needs

Guides the **Reasoning Layer's** prioritization.

1.  **Survival:** Avoid threats, seek immediate safety (light/shelter at night/rain). Innate behaviors prioritized.
2.  **Comfort & Safety:** Establish security (better shelter, resources, tools).
3.  **Social:** Interact with others.
4.  **Purpose:** Pursue goals related to Rank & Trade.
5.  **Winning / Self-Actualization:** Idle, explore, learn, seek new purpose when other needs met.

## III. Modifying Factors

These influence both the Reasoning Layer's decisions and the Action Layer's execution:

*   **Capability Stats:** Primarily affect Action Layer effectiveness (speed, range, success chance) and are prerequisites for Purpose/Rank goals.
*   **Personality Traits:** Primarily affect Reasoning Layer objective selection (preference for social vs. solitary, planned vs. spontaneous) and Action Layer style (look duration, panic chance).
*   **Societal Rank:** Primarily gates Purpose goals and influences social interactions (Future).
*   **Trade:** Primarily gates Purpose goals related to specific skills (Future).

## IV. Development Steps (Revised)

### 1. Implement Core Data Structures & Persistence (NBT)

*   **Status:** DONE
*   **Components:** `CapabilityStat` (Values & XP), `PersonalityProfile` (Traits), `SocietalRank`, `Trade`.

### 2. Develop Capability Stat XP/Leveling Logic

*   **Status:** DONE
*   **Components:** `gainXp`, `levelUpStat`, `getXpThreshold`.

### 3. Develop Personality Logic & Evolution

*   **Status:** DONE (Basic)
*   **Components:** `getMBTIType`, `evolve`, initial evolution triggers.

### 4. Implement Basic Action Layer Goals

*   **Status:** IN PROGRESS
*   **Goal:** Create the library of low-level actions the Reasoning Layer can choose from.
*   **Components:**
    *   Survival: `FloatGoal`, `SurvivalPanicGoal`, `AvoidEntityGoal`, `FindShelterOrLightGoal` (DONE - needs debugging).
    *   Comfort/Safety: `BreakBlockGoal` (DONE - needs debugging).
    *   Social: `LookAtPlayerBasedOnPerceptionGoal` (DONE).
    *   Movement: `RandomStrollGoal` (Vanilla - DONE).
    *   *Future:* Add more goals for each need category.

### 5. Implement Reasoning Layer (BehaviorManager & Memory)

*   **Status:** NOT STARTED
*   **Goal:** Create the core decision-making engine.
*   **How:**
    *   Create `BehaviorManager` class/logic within `Lag0sEntity`'s `tick()`.
    *   Implement Needs Assessment logic.
    *   Implement basic Objective Selection logic (initially focusing on Survival/Safety).
    *   Implement dynamic Goal Management (add/remove goals based on objective).
    *   Create basic `EntityMemory` class and NBT persistence.

### 6. Integrate Systems in AI Goals

*   **Status:** IN PROGRESS
*   **Goal:** Ensure Action Layer goals correctly use Stats/Personality and grant XP/trigger evolution.
*   **How:** Refine existing goals (`BreakBlockGoal` animation/targeting, `FindShelterGoal` stopping logic), add checks/influences to future goals.

### 7. Develop Rank & Trade Progression Logic (Future Step)

*   **Status:** NOT STARTED

### 8. Implement Trade & Purpose-Driven AI Goals (Future Step)

*   **Status:** NOT STARTED

### 9. Refine Appearance (Lower Priority)

*   **Status:** NOT STARTED

### 10. Refine Spawner Item (Lower Priority)

*   **Status:** NOT STARTED

### 11. Future Enhancements / Advanced AI

*   Recovery Score, Advanced Needs, Complex Learning, NPC<->NPC Interaction, Storytelling.

---

**Recommended Next Step:** Debug and refine the existing Survival/Safety goals (`FindShelterOrLightGoal`, `BreakBlockGoal`) as part of Step #6, then begin implementing the basic Reasoning Layer structure (Step #5).
