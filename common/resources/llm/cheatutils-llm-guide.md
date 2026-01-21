# CheatUtils LLM Guide
## How to generate correct, safe, and performant scripts
This document describes how scripts are executed, what rules must be respected,
and how to write scripts that will actually work in-game.

This file is intended primarily for LLM-assisted scripting.
Detailed API and type definitions are provided separately in `cheatutils-api.txt`.

## 1. Execution model (very important)
Main thread
- All scripts run on the Minecraft client main thread
- There is no parallel execution
- Blocking the script blocks the game

Because of this:
- Avoid heavy computation in per-frame / per-tick scripts
- Avoid heavy loops that are not normal for Minecraft. Examples:
    - it is ok to iterate over all entities every frame, inside Status Overlay script, since Minecraft itself does this a lot
    - it is not ok to iterate over all entities from Entity ESP script, since this may cause O(n^2) complexity

## 2. Script types and how they execute
Each script has a fixed signature, execution frequency, and API restrictions.
Always identify the script type first and follow its rules.

### Key Bindings
- Runs when the user presses the assigned key
- Allows `await` directly in the main script body
- Intended for active actions and automation
- Safe place for sequences like: wait → click → wait → interact → wait

Use case: macros, automation flows, container logic.

### Status Overlay
- Runs every frame
- Used only to collect and display text
- Readonly only — no actions, no state changes, no network calls
- Must be fast and allocation-light

Use case: F3-like overlays, counters, status indicators.

### Block Automation
- Runs every tick for many blocks
- Script body is called once per block in range
- Extremely performance-sensitive

Rules:
- Never allocate large arrays
- Never scan the world
- Only react to the current block
- Use `blockAutomation.*` methods for actions
- Assume this runs hundreds or thousands of times per second

Use case: farming, placing, breaking, block interaction logic.

### Villager Roller
- Runs when a new enchanted book trade appears
- Script is invoked per trade
- You must explicitly stop the module when done

Use case: rolling trades until conditions are met.

### Events Scripting
- Script runs once
- Used to attach event handlers
- When the script is saved again: all old handlers are automatically detached

Rules:
- Do not perform logic in the main body
- Put logic inside event callbacks
- Avoid heavy work in high-frequency events (ticks, packets)

Use case: reacting to chat, players, packets, ticks.

### Block ESP
- Runs every frame for every ESP-highlighted block
- Used only to enable/disable visual elements

Rules:
- No actions
- No world modification
- Only control tracer / outline / overlay visibility

### Entity ESP
- Runs every frame for each relevant entity
- Used to customize ESP visuals per entity

Rules:
- No actions
- No movement or interaction
- Only visual decisions

### Kill Aura / Hitbox Size
- Pure predicate scripts
- Must return true or false
- Called frequently by the engine

Rules:
- Must be fast
- No side effects
- No async logic

## 3. Async / await rules
Where `await` is allowed:
- Allowed directly in main script body ONLY for Key Bindings
- In all other script types: the main body must be synchronous

Async functions and methods:
- You may always declare `async` functions
- You may always `await` inside async functions
- This applies to all script types

Example pattern:
```ts
async void doWork() {
    await delay.ticks(20);
    // ...
}

doWork(); // called from synchronous script body
```

## 4. API visibility and restrictions
A lot of API methods are annotated with `@ApiVisibility`.

Typical values:
- `ACTION` — sends packets, interacts with the world
- `UPDATE` — toggles module state or configuration
- `OVERLAY` — overlay-only rendering
- `BLOCK_AUTOMATION` — safe only in Block Automation scripts
- `EVENTS`, `LOGGING`, `VILLAGER_ROLLER`, etc.

Rules:
- Never call APIs that are not allowed for the current script type
- If a script violates visibility rules it will simply not compile

Some APIs required "Advanced Scripting" to be enabled from UI.
These APIs are considered dangerous, and this was done prevent unexpirienced user to copy-paste script from the internet and get hacked.
`OsApi` allows to start processes, `HttpApi` allows to send HTTP requests to external parties.
Using Java Interop also requires "Advanced Scripting" for obvious reasons.

## 5. Error model (important!)
Exceptions:
- Any uncaught exception crashes the game
- There is currently no try/catch support
- Scripts are assumed to be written by advanced users

Because of this:
- Always validate inputs
- Always check indices
- Always guard against null-like conditions
- Prefer early returns over risky access

Example:
```ts
if (id <= 0) return;
if (!containers.isValidSlotIndex(slot)) return;
```

## 6. State sharing between scripts
Scripts are isolated.
The only supported way to share state between scripts is:
- `variables` API

Use it for:
- flags
- counters
- simple coordination between scripts

Do not rely on globals or static initialization order.

## 7. API safety and default values
CheatUtils APIs are designed to be null-safe and beginner-friendly.

General guarantees:
- API methods perform internal validation
- API methods should not throw exceptions under normal usage
- No API method ever returns null
- No API method expects null as an argument

Default return values
- When data is unavailable or invalid:
- Methods returning string → return empty string ("")
- Methods returning objects → return a valid default instance
- Methods returning collections → return empty collections
- Methods returning numbers:
    - return zero if parameters/state was correct, but something was missing
    - return `int.MIN_VALUE` or `NaN` is parameters/state was invalid

Example:
```ts
ItemStack stack = inventory.getHotbarItem(-100);
// Safe: returns empty ItemStack, never null

int count1 = game.entities.getCount("net.minecraft.BlackHoleEntity");
// Safe: returns int.MIN_VALUE since such class doesn't exist

int count2 = game.entities.getCount("net.minecraft.world.entity.monster.Slime");
// Safe: calculates slimes around the player, or returns 0 if no world is loaded
```

Even if:
- an index is invalid
- no world is loaded
- player entity does not exist

…the method still returns a safe default.

What NOT to do

Do not generate:
- `if (value != null)` checks for API return values
- defensive null guards around API calls
- null arguments passed into API methods

Such checks are unnecessary and should be avoided.

What you SHOULD validate

You should still validate:
- numeric ranges (slot indexes, entity ids)
- semantic correctness (e.g. `id > 0`)
- logical conditions (e.g. `isEnabled()`, `hasItemAtSlot()`)

## 8. Java interop and obfuscation (temporary)
Current situation (Minecraft 1.21.11)
- Fabric runtime uses obfuscated class names
- Java interop code may differ between loaders

This is temporary:
- CheatUtils targets a single version
- Future Minecraft versions (26.1+) remove obfuscation

Guidelines:
- Avoid Java interop unless necessary
- Prefer CheatUtils APIs over raw Java access
- Expect this section to be removed in future versions

## 9. Recommended defaults for generated scripts
When generating a script, prefer to include:
- Early guards
- Safe exit conditions
- Logging via `debug.write` or `ui.systemMessage`
- A clear stop condition for loops or automation

Avoid:
- Infinite loops without delays
- Silent failures
- Hard-coded magic numbers without comments

## 10. Final checklist for LLM-generated scripts
Before returning a script, ensure:
- Script type is identified and correct
- API visibility rules are respected
- No forbidden `await` usage
- No heavy logic in per-frame/per-tick contexts
- All risky accesses are guarded
- Script can be safely stopped or disabled