# Key Binding Scripts

## Purpose

Use `KEYBINDING` scripts for user-triggered actions, macros, and automation flows started from the in-game keybindings menu.

## Execution

- Runs when the assigned key is pressed.
- Compiles as an async runnable.
- Allows `await` directly in the main script body.
- Runs on the Minecraft client main thread.

## Rules

- Use this script type for action sequences such as wait, click, wait, interact.
- Include delays in loops that wait for game state.
- Add a clear exit condition for long automation.
- Avoid blocking calls and expensive computation.

## Good fit

- Inventory or container macros.
- Movement or interaction sequences.
- Module toggles followed by timed actions.
- Scripts that need direct `await` in the main body.
