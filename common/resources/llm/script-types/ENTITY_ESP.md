# Entity ESP Scripts

## Purpose

Use `ENTITY_ESP` scripts to customize ESP visuals for entities handled by an Entity ESP config.

## Execution

- Runs every frame for each relevant entity.
- Current entity id is available as `id`.
- Current visual event is available as `event` of type `EntityEspEvent`.
- Use `game.entities` to inspect the current entity.

## Rules

- Do not perform actions.
- Do not move, attack, interact, or modify module state.
- Do not iterate all entities from this script.
- Only decide visual state for the current entity.
- Keep code fast and allocation-light.

## Good fit

- Disable tracer, outline, overlay, or collision box for specific entities.
- Change displayed title text.
- Filter visuals by entity id, type, distance, or state.
