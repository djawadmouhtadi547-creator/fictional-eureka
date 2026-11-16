# DARKNULL-06

**A NON-EUCLIDEAN HORROR EXPERIENCE**  
Created by **djawad1**  
Target: **Minecraft Java 1.20.1 · Fabric · Java 17**  
Current source version: **0.2.0**

> Reality is not what you think.

DARKNULL-06 is a server-authoritative horror/illusion mod. It builds numbered illusion rooms in its own void dimension (`darknull06:darknull_void`) rather than replacing blocks in the player's Overworld, Nether, or End builds.

## What is implemented in 0.2.0

| ID | In-game trick | Current implementation |
|---:|---|---|
| 1 | Infinite Staircase | Built stairwell; reaching the upper threshold teleports the player back to the bottom with portal particles and sound. |
| 2 | Endless Corridor | Built enclosed corridor; crossing its far threshold returns the player to the entrance and changes a lamp detail. |
| 3 | Lying Door | Built furnished room with an open doorway; crossing it loops the player back and changes room decoration. |
| 4 | Bigger on the Inside | Small exterior room connects through a doorway to a separate oversized hall; a second portal returns the player. |
| 5 | Infinite Room | Four open doorways surround one room; crossing an edge returns the player to the centre and changes a detail. |
| 6 | Mirror Room | An armor-stand reflection follows the player's mirrored position with an eight-tick delay, then stops and turns toward the player. It is a mannequin-based reflection, not a duplicate player entity. |
| 7 | Weeping Blocks | Four multi-block stone figures move one block toward the player when outside the player's view. |
| 8 | Shrinking Room | Concentric walls are added on a timer. The player is moved inward before a wall can trap or crush them; shrinking stops at a safe minimum. |
| 9 | Reverse Gravity | A raised ring walkway near the ceiling and suspended objects create a ceiling-side spatial illusion. **Vanilla gravity, camera orientation, and movement physics are not inverted in this version.** |
| 10 | Shifting Maze | A connected 5×5 procedural maze is generated with a depth-first search. A sharp turn rebuilds its passages; the maze includes an entrance and exit. |

The rooms use vanilla blocks, entities, particles, and sounds. No shader, resource pack, or additional gameplay mod is required.

## Commands

Commands require permission level 2 (operator / cheats enabled).

| Command | Purpose |
|---|---|
| `/darknull 1` … `/darknull 10` | Start the numbered illusion. |
| `/darknull help` | List all ten illusions. |
| `/darknull stop` | End the active illusion, clear its plot, and return to the saved location. |
| `/darknull reset` | Same safe cleanup as `stop`. |
| `/darknull tutorial` | Replay the tutorial messages. |

## First-join tutorial

The first-join sequence begins with the exact text `to darknull-06`, then stages connection, warning, command help, and the final credit. The completion marker is stored as a player command tag. The tutorial can be replayed with `/darknull tutorial`.

## World safety and recovery

- The mod-owned void dimension is the only dimension where this version places trick geometry.
- Every player receives a separated plot, allocated from their UUID and checked for existing blocks before use.
- `stop` and `reset` clear the allocated plot and return the player to their saved dimension and coordinates.
- Disconnect and graceful server shutdown handlers clear active plots.
- An active plot coordinate is stored in a player command tag. If the server crashes during a session, the next join removes the orphaned plot and moves a stranded player to the Overworld spawn.
- Plot allocation scans about 34,000 positions and cleanup checks the same reserved area. Running many first-time sessions simultaneously may create a short server tick spike.

The custom dimension is part of the mod data pack. Install the mod and restart the world/server before using commands. Back up important worlds before testing any mod, especially on an existing server.

## Build

Requirements: JDK 17 and network access on the first build. The included `gradlew` / `gradlew.bat` bootstrap scripts download the pinned Gradle 8.6 distribution into the user's Gradle cache, then execute it. The project uses Fabric Loom 1.6.12.

Windows:

```bat
gradlew.bat --no-daemon clean build
```

Linux/macOS:

```sh
chmod +x gradlew
./gradlew --no-daemon clean build
```

The mod JAR is expected under `build/libs/`. GitHub Actions runs the same build command and uploads the resulting JARs as an artifact.

**Build verification status for this handoff:** the source and workflow have been prepared, but no Gradle build was executed in the current isolated environment because Gradle is not installed and dependency downloads are unavailable. Therefore this archive does not claim to contain a tested JAR. Run the command above or the GitHub Actions workflow to expose and fix any remaining Yarn/API mismatch.

## Project structure

```text
DARKNULL-06/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── gradle/wrapper/gradle-wrapper.properties
├── .github/workflows/build.yml
├── src/main/java/com/djawad1/darknull/
│   ├── DarkNullMod.java
│   ├── command/DarkNullCommands.java
│   ├── engine/TrickCatalog.java
│   ├── engine/TrickDefinition.java
│   ├── engine/TrickEngine.java
│   ├── engine/TrickSession.java
│   ├── tutorial/TutorialManager.java
│   └── world/TrickWorldBuilder.java
└── src/main/resources/
    ├── fabric.mod.json
    ├── data/darknull06/dimension/darknull_void.json
    ├── data/darknull06/dimension_type/darknull_void.json
    └── assets/darknull06/lang/{ar_sa,en_us}.json
```

## Known limitations / next engineering tasks

1. The reverse-gravity illusion needs a tested client entrypoint, input/camera handling, and movement/collision mixins before it can be called true inverted gravity.
2. The mirror is an armor stand wearing copied player equipment and a player-head item. A custom client renderer would be needed for a faithful full player-model reflection, including skin and animation.
3. The room transitions are server teleports masked by short blindness, particles, and sound. A client-side transition renderer could make them smoother and less noticeable.
4. The tutorial is a timed chat sequence, not a custom full-screen GUI.
5. Build and in-game testing on Minecraft 1.20.1 Fabric is still required before calling this a stable release.

## Credits

**DARKNULL-06 — Created by djawad1.** Keep the author credit in mod metadata, tutorial, source, documentation, and release notes.
