# Block ESP Scripts

## Purpose

Use `BLOCK_ESP` scripts to customize ESP visuals for blocks found by the Block ESP module.

## Execution

- Runs every frame for every block found by the Block ESP module.
- Current block position is available as `pos` of type `BlockPos`.
- Current visual event is available as `event` of type `BlockEspEvent`.
- Use `game.blocks` to inspect the current block if needed.

## Rules

- Do not perform actions.
- Do not modify the world.
- Do not scan unrelated blocks or entities.
- Only decide visual state for the current block.
- You cannot override tracer, box, or overlay state outside the configured range.

## Good fit

- Hide ESP for specific block states.
- Change visibility based on current block position.
- Filter visual elements for the current block.
