# Villager Roller Scripts

## Purpose

Use `VILLAGER_ROLLER` scripts to inspect enchanted book trades and stop the Villager Roller module when a desired trade appears.

## Execution

- Runs when the Villager Roller module sees a new enchanted book trade.
- Invoked per trade.
- Use `villagerRoller` API to inspect the current trade.
- Logging APIs are also visible.

## Rules

- Explicitly call `villagerRoller.stop()` when the desired trade is found.
- Keep checks small and deterministic.
- Do not perform unrelated automation in this script.

## Good fit

- Stop on a specific enchantment.
- Stop on a maximum price.
- Stop on a combination of enchantment, level, and cost.

## Related state

Other scripts can call `villagerRoller.getState()` to inspect module state such as manual setup, placing lectern, breaking lectern, or waiting for profession gain.
