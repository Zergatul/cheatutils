# Status Overlay Scripts

## Purpose

Use `OVERLAY` scripts to collect strings and display them on screen, similar to a customizable F3 overlay.

## Execution

- Runs every frame.
- Runs once per frame, not once per entity or block.
- Uses the `overlay` API to add text and position it.
- Only overlay/read-only style APIs are visible.

## Rules

- Do not perform actions.
- Do not change world, player, module, or server state.
- Keep allocations low.
- Keep work bounded and predictable.

## Good fit

- Counters.
- Status indicators.
- Nearby information summaries.
- Debug values that should update every frame.
