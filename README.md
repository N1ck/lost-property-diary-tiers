# Perdu Diary Icons

A RuneLite plugin. When you reclaim an achievement-diary reward item from **Perdu**
(the Lost Property shop), the shop always draws the **easy / tier-1** icon regardless of
which tier you've actually completed. This plugin swaps the displayed icon to the highest
tier you've completed (e.g. shows *Karamja gloves 4* if you've done the Elite diary).

It's purely cosmetic. The icon shown is corrected, but buying/reclaiming is driven by the
shop slot server-side, so the item you receive is unchanged.

## How it works

- Perdu's reclaim screen is the **Lost Property shop** interface (`InterfaceID.LOST_PROPERTY`,
  group `557`), with the stock under `LostProperty.LIST` (component `557.2`). It is *not* the
  standard shop interface (300).
- That shop displays each diary reward under its generic **base** item id (e.g. `1686` "Karamja
  gloves", `762` "Falador shield"). That base icon is the pre-tier one, which always looks like
  easy tier regardless of what you've completed, and that's the bug this plugin fixes.
- It tracks the shop being open via `WidgetLoaded`/`WidgetClosed` for group 557, and re-applies on
  every `ScriptPostFired` while it's open. The interface fills its slots incrementally across
  several scripts (re-writing them with the base icons), so re-applying after each script corrects
  them within the same cycle, before the frame renders, so there's no flash, and it copes with the
  incremental fill. It's a cheap no-op (one boolean check) while the shop is closed.
- For any slot showing a tracked base (or tier) id it reads that diary's completion varbits, finds
  your highest completed tier, and sets both the icon (`Widget#setItemId`) and the slot name
  (`Widget#setName`) to that tier, so the hover/right-click text matches the icon.

The base id, completion varbits, and tier item ids per reward live in `DiaryReward.java`.

## Build / run

```bash
./gradlew compileJava   # compile only
./gradlew run           # launch RuneLite (developer mode) with this plugin loaded
```

`./gradlew run` starts the full client; log in and visit Perdu to test.

### Logging in with a Jagex account

`./gradlew run` launches the client outside the Jagex Launcher, so it has no
session and you'd be stuck at the old login screen. Use the official
credentials-file flow (RuneLite launcher **2.6.3+** required):

1. Configure RuneLite (launcher `--configure`) and add
   `--insecure-write-credentials` to the client arguments.
2. Launch RuneLite once from the Jagex Launcher and log in. It writes your
   session to `~/.runelite/credentials.properties`.
3. `./gradlew run` then reads that file and logs into your account.

The credentials file holds live session tokens in plaintext; delete it when done
and never commit it.

(The `JX_*` environment-variable bridge that works on Linux/Windows does **not**
work on macOS, because the OS blocks reading another signed process's environment.)

## Debugging / verifying item ids

Enable **Debug logging** in the plugin's config panel. When you open Perdu's shop it logs,
per diary slot: the item id currently shown and the tier id the plugin computed. Compare those
against the OSRS Wiki to confirm the mapping in `DiaryReward.java` is right for your account.

If icons ever stop refreshing after a swap, calling `slot.revalidate()` after `setItemId` in
`applyDiaryIcons` is the fallback.
# Perdu Diary Icons - Runelite Plugin
