# Kill Aura Scripts

## Purpose

Use `KILL_AURA` scripts as a predicate that decides whether Kill Aura may target a specific entity.

## Execution

- Called when Kill Aura evaluates a candidate target.
- Current entity id is available as `id`.
- Must return `true` to allow targeting or `false` to reject targeting.
- No action/update APIs are visible.

## Rules

- Return a boolean on every path.
- Do not perform side effects.
- Do not use async logic.
- Keep checks fast.
- Use the current `id`; do not scan for targets yourself.

## Good fit

- Reject friends or named entities.
- Target only specific entity types.
- Reject entities outside custom conditions.
