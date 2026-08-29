# CheatUtils LLM Guide

## How to generate correct, safe, and performant scripts for Minecraft 1.20.1

This document describes the scripting model in this CheatUtils version. Detailed,
generated API and type definitions are available separately in
`cheatutils-api.txt`. Use that file as the authoritative inventory: newer
CheatUtils versions contain script types and methods that are intentionally not
present here.

## 1. Execution model

- Scripts and their callbacks run on the Minecraft client thread.
- Blocking a script blocks the game.
- Avoid heavy computation in per-frame and per-tick scripts.
- Do not use long loops without an asynchronous delay and a clear stop condition.

## 2. Supported script types

Always identify the target script type before generating code. Each type has a
fixed signature and a restricted set of APIs.

### Key Bindings

- Runs when the user presses its assigned key.
- Allows `await` directly in the main script body.
- Intended for actions, module changes, and asynchronous sequences.

Use case: macros, automation flows, and delayed interactions.

### Status Overlay

- Runs every frame while the overlay is enabled.
- Uses the `overlay` API to collect and display text.
- Exposes readonly and overlay-specific methods, not action methods.
- Must stay fast and allocation-light.

Use case: F3-like counters and status indicators.

### Block Automation

- Receives the current block coordinates as `int x`, `int y`, and `int z`.
- Runs every tick for many blocks in the configured range.
- Uses `game.blocks` to inspect the block and `blockAutomation` to request an
  action at the current coordinates.
- Is extremely performance-sensitive; do not rescan the world or allocate large
  collections in the callback.

Use case: farming, placing, breaking, and block interaction rules.

### Villager Roller

- Runs when a new enchanted-book trade appears.
- Uses `villagerRoller` to inspect the current trade.
- Must call `villagerRoller.stop()` when the desired trade is found.

### Events Scripting

- Runs once to register callbacks through the `events` API.
- Saving the script removes the old callbacks before registering the new ones.
- This version currently supports `onHandleKeys`, in-world `onTickEnd`, and
  `onMenuTickEnd`. Do not invent newer event methods.
- Put recurring logic in callbacks rather than in the main body.

Block ESP, Entity ESP, Kill Aura, and Hitbox Size scripting are not part of this
version. Their module settings still exist, but scripts for them must not be
generated.

## 3. Async and await

- `await` is allowed directly in the main body only for Key Binding scripts.
- Async functions can be declared by other script types and called from their
  synchronous callbacks.
- Use `delay.clientTicks(...)` when a delay should continue across logout,
  world unload, or dimension changes.
- Use `delay.inGameTicks(...)` when the delay belongs to the current loaded
  world; it fails if that world unloads.
- Closing the Minecraft client cancels pending script executions and delays.

Example:

```csharp
async void doWork() {
    await delay.clientTicks(20);
    debug.write("One second later at normal tick rate");
}

doWork();
```

## 4. API visibility

Methods annotated with `@ApiVisibility` are available only to matching script
types. Common categories include `ACTION`, `UPDATE`, `OVERLAY`,
`BLOCK_AUTOMATION`, `VILLAGER_ROLLER`, `LOGGING`, and `EVENTS`.

If a method is not allowed for the selected script type, the script will not
compile. Consult the script-type header and method annotations in
`cheatutils-api.txt`; do not work around the restriction through another API.

## 5. Error model

- Compilation errors are reported by the editor and prevent the invalid program
  from replacing the last valid interactive program.
- Uncaught runtime exceptions deliberately propagate and crash the client so a
  broken script cannot fail silently.
- Generated source names and script frames are retained in the stack trace.

Validate indexes, identifiers, ranges, and state before performing an action.
Prefer early returns over risky access.

## 6. State shared between scripts

Script programs are isolated. Use the `variables` API for simple flags, counters,
and coordination between scripts. Do not depend on globals from another script
or on static initialization order.

## 7. API values and unavailable game state

CheatUtils APIs generally return safe empty/default values when the world,
player, entity, item, or requested data is unavailable. They do not use `null`
as a normal scripting result. Exact sentinel behavior varies by method, so use
the generated API descriptions and validate semantic conditions such as slot
indexes, entity ids, enabled state, and empty item stacks.

## 8. Java interop and Minecraft 1.20.1

Java type usage requires Advanced Scripting in Core Config. Prefer CheatUtils
APIs because Minecraft 1.20.1 runtime names and internals differ between loaders
and are less stable with other mods. Only use Java interop when the generated
CheatUtils API cannot express the required behavior.

## 9. Recommended defaults

Generated scripts should include:

- early guards;
- a clear stop condition for loops and automation;
- short comments for non-obvious constants;
- useful diagnostics through `debug.write(...)` when appropriate;
- a delay in every asynchronous loop.

Avoid infinite loops, silent failures, heavy per-frame work, newer-version APIs,
and Java interop unless explicitly requested.

## 10. Final checklist

- The script type and its callback signature are correct.
- Every API/method exists in `cheatutils-api.txt` and is visible to that type.
- Direct `await` is used only where allowed.
- Per-frame/per-block/per-tick logic is bounded and lightweight.
- Indexes and identifiers are validated.
- Loops and automation have a safe stop condition.
- No scripting surface intentionally deferred from this 1.20.1 version is used.
