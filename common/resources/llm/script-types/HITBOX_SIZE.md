# Hitbox Size Scripts

## Purpose

Use `HITBOX_SIZE` scripts as a predicate that decides whether the Hitbox Size module should alter a specific entity hitbox.

## Execution

- Called when the game needs entity collision information or renders vanilla debug hitboxes.
- Current entity id is available as `id`.
- Must return `true` to alter the hitbox or `false` to leave it unchanged.
- No action/update APIs are visible.

## Rules

- Return a boolean on every path.
- Do not perform side effects.
- Do not use async logic.
- Keep checks fast and allocation-light.

## Good fit

- Apply hitbox changes only to selected entity types.
- Exclude players, friends, pets, or named entities.
- Apply changes only under simple conditions.
