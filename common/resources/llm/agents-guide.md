# CheatUtils Agents Guide

This guide is for coding agents connected to CheatUtils through MCP.
It describes how to gather the right context before writing or modifying scripts.

## Recommended workflow

1. Identify the target script type.
2. Call `list_script_types`.
3. Read the `documentation_uri` for the selected script type.
4. Read `cheatutils://docs/api` for current API signatures and visibility annotations.
5. Read `cheatutils://docs/language` when syntax, async behavior, or language semantics are unclear.
6. Use `compile_script` before saving whenever possible.

Do not rely on `cheatutils-api.txt` when MCP resources are available. That file is for manual web-chat workflows where the user downloads API text and pastes it into a chat.

## Execution model

- Scripts run on the Minecraft client main thread.
- Blocking a script blocks the game.
- There is no parallel script execution.
- Per-frame and per-tick scripts must stay small and predictable.
- Runtime exceptions are not wrapped and usually crash the game.
- Do not blanket-wrap scripts in `try/catch`; in-game exception display is poor, and example scripts intentionally leave crashes visible.

Avoid expensive work in callbacks that run for many objects. Iterating over visible entities in a Status Overlay script can be acceptable because the script runs once per frame. Iterating over all entities from an Entity ESP script is usually wrong because that script already runs once per relevant entity.

## Script types

Each script type has its own signature, frequency, variables, return value, and allowed API visibility set.

- `KEYBINDING`: key-triggered actions and automation sequences.
- `OVERLAY`: frame-based text display.
- `BLOCK_AUTOMATION`: tick-based logic called once per candidate block.
- `VILLAGER_ROLLER`: trade filtering for Villager Roller.
- `EVENTS`: one-time event handler registration.
- `BLOCK_ESP`: per-block ESP visual decisions.
- `ENTITY_ESP`: per-entity ESP visual decisions.
- `KILL_AURA`: target predicate for Kill Aura.
- `HITBOX_SIZE`: target predicate for Hitbox Size.

Read the script-type documentation resource before writing code for a specific type.

## Async and await

- `await` is allowed directly in the main body only for `KEYBINDING`.
- Other script types must keep the main body synchronous.
- Any script type may declare `async` functions and use `await` inside those async functions.

Pattern for synchronous script types:

```ts
async void doWork() {
    await delay.ticks(20);
}

doWork();
```

## API visibility

The generated API includes `@ApiVisibility(...)` annotations.
Only call methods whose visibility matches the selected script type's allowed `api_types`.
If a method is not visible for the selected script type, the script will not compile.

Advanced APIs require Advanced Scripting to be enabled in the UI. This includes APIs such as `http`, `os`, selected packet hooks, and Java interop. Do not use Advanced APIs unless the user explicitly wants them and understands the risk.

CurseForge builds can also disable methods annotated as CurseForge-restricted. Prefer ordinary CheatUtils APIs over OS, file, HTTP, or Java interop access.

## Safety rules

- Prefer early returns over deeply nested conditions.
- Validate numeric ranges, entity ids, block positions, inventory slots, and user-provided strings.
- Do not pass null values to CheatUtils APIs.
- CheatUtils APIs normally do not return null. Do not generate defensive null guards around ordinary API calls.
- Methods or properties returning `UUID` may return null when no UUID is available.
- Prefer API methods that expose safe default objects or explicit `has*` checks.
- When a type exposes a `has*` property, check it before using the matching optional value.
- Future generated API references may include property-level descriptions with more precise nullability notes.
- Do not assume raw Java interop names are stable across loaders.

## Performance rules

- Avoid unbounded loops.
- Avoid allocations in per-frame, per-entity, per-block, and predicate scripts.
- Avoid world scans inside scripts that already run for each world object.
- Use the current script parameters instead of rediscovering the same object.

## Final check

Before returning a script:

- The script type is explicit.
- The selected APIs are visible for that script type.
- Main-body `await` is used only where allowed.
- Per-frame or per-tick work is bounded.
- Optional data is guarded by semantic checks.
- Automation has a clear stop condition.
