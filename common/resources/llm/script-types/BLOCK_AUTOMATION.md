# Block Automation Scripts

## Purpose

Use `BLOCK_AUTOMATION` scripts to automatically place blocks, break blocks, or use items on blocks that the Block Automation module scans.

## Execution

- Runs every game tick while the module is active.
- The script body is called once for each candidate block in range.
- Current block coordinates are available as `x`, `y`, and `z`.
- Use `blockAutomation` APIs for block actions.

## Rules

- Only react to the current block.
- Do not scan the world.
- Do not allocate large arrays or collections.
- Do not iterate entities or blocks unrelated to the current coordinates.
- Assume this may run hundreds or thousands of times per second.

## Item-use limitation

Some items require the player to look in the correct direction and send a second packet. Those interactions are not supported by Block Automation. Examples include filling or emptying buckets and glass bottles.

## Good fit

- Farming logic.
- Replacing blocks.
- Breaking matching blocks.
- Simple block interaction decisions based on `x`, `y`, `z`.
