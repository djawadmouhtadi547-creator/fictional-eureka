# DARKNULL-06 0.2.0 implementation status

## Added in this iteration

- Private data-pack dimension and dark fixed-time dimension type.
- UUID-separated room plots that are checked before generation.
- Central room builder for ten distinct illusion layouts.
- Real staircase and corridor loops; doorway and room transition triggers.
- Oversized interior chamber connected to a small exterior room.
- Four-door repeated-room illusion.
- Armor-stand mirror that tracks delayed mirrored player coordinates and then stops.
- Four movable block statues that advance when not in view.
- Timed concentric shrinking walls with a safe minimum size.
- Ceiling-side raised walkway with suspended objects.
- Procedural connected 5×5 maze regenerated on sharp turns.
- Session cleanup on stop, disconnect, server stop, and next join after an interrupted session.
- A Gradle 8.6 bootstrap script and CI workflow that uses it.

## Explicitly not complete

- ID 9 does not invert vanilla gravity or camera orientation. It is an environmental ceiling-side illusion only.
- ID 6 does not render a full duplicate player model. It uses an armor stand and copied equipment/player-head item.
- No custom client GUI, custom sound assets, or mixin-based camera transitions are included.
- No successful compile or runtime test has been recorded in this environment. The first real build must be run with JDK 17 and network access.

## Test checklist

1. Build with `./gradlew --no-daemon clean build` or `gradlew.bat --no-daemon clean build`.
2. Create a new 1.20.1 Fabric world with Fabric API and the mod installed; restart after adding the mod to an existing world.
3. Confirm `darknull06:darknull_void` loads and `/darknull help` is available to operators.
4. Start each ID from the Overworld and verify that the player is returned to the exact original position and dimension with `/darknull stop`.
5. Verify `/darknull reset`, disconnect cleanup, and recovery after a forced server restart.
6. Test at least two simultaneous players to ensure plot separation.
7. Observe server tick time during plot allocation, room generation, maze regeneration, and cleanup.
