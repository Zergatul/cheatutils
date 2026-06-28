# Events Scripts

## Purpose

Use `EVENTS` scripts to attach event handlers that react to game events, chat, packets, players, ticks, or server state.

## Execution

- Runs once when saved or loaded.
- The main body should register handlers through the `events` API.
- When the script is saved again, old handlers are automatically detached.

## Rules

- Do not put recurring logic directly in the main body.
- Put recurring logic inside event callbacks.
- Keep high-frequency handlers small, especially tick and packet handlers.
- It is fine to register multiple handlers for the same event when they implement different features.
- Existing module help often provides snippets that users can paste at the end of the Events Scripting script.
- Avoid accidentally pasting the same handler block twice unless duplicate behavior is intended.

## Advanced hooks

Some packet-level hooks are Advanced APIs. Use them only when Advanced Scripting is enabled and the user explicitly needs packet inspection or mutation.

## Good fit

- Chat reactions.
- Player join or visibility notifications.
- Tick-based key handling.
- Key handling snippets for modules such as Zoom and Aim Assist.
- Tick-end safety checks for modules such as Blink.
- Packet inspection when advanced scripting is appropriate.
