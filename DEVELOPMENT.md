# Development Guide

## Prerequisites

- **Java 25** (JDK 25+)
- **Git**

## Setup

```bash
git clone https://github.com/bitzCognautic/Pulse.git
cd Pulse
```

Open in IntelliJ IDEA. The Gradle import handles dependencies.

## Build Commands

| Command | Description |
|---|---|
| `./gradlew build` | Full build |
| `./gradlew runClient` | Launch Minecraft with the mod |
| `./gradlew clean` | Clean build outputs |

Testing and CI are run via GitHub Actions (`build.yml`).

## Project Layout

- `Pulse.java` — `ModInitializer`, wires singletons
- `PulseClient.java` — `ClientModInitializer`, applies optimizations per tick
- `performance/` — frametime tracking, rolling statistics
- `pressure/` — weighted normalisation → pressure score
- `adaptive/` — stage mapping and recovery timing
- `modules/` — 9 module stubs (none wired yet)
- `config/` — ModMenu GUI + JSON persistence
- `overlay/` — debug HUD
- `compat/` — mod scanner

## Adding a New Optimization

1. Implement the change in `PulseClient.applyOptimizations()` (if using Options API).
2. Or, wire into a Fabric event (`LevelRenderEvents`, etc.).
3. Add toggles in `ConfigManager.buildScreen()` if user-controllable.
4. Update `README.md` and `ARCHITECTURE.md`.

## Notes on Minecraft 26.2

- The rendering system was rewritten (two-phase extraction + submit nodes).
- `WorldRenderEvents` was removed; use `LevelRenderEvents` / `LevelExtractionEvents`.
- `GraphicsStatus` / `graphicsMode()` were removed.
- Old mixin targets (`EntityRenderDispatcher.render`, `ParticleEngine.particles`, etc.) were renamed or restructured.
- Fabric API 0.155.2+26.2 is required.
