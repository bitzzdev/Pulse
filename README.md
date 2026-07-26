# Pulse — Adaptive Performance Manager for Minecraft

![Build](https://img.shields.io/github/actions/workflow/status/bitzCognautic/Pulse/build.yml?branch=main&label=build)
![License](https://img.shields.io/github/license/bitzCognautic/Pulse)
![Fabric](https://img.shields.io/badge/Fabric%20Loader-0.19.3%2B-orange)

Pulse continuously monitors framerate performance and automatically adjusts cosmetic settings to maintain smooth gameplay. When performance recovers, changes are gradually reversed.

**Status: Alpha (0.2.0).** All 9 modules have active mixin hooks or direct API access.

## What Works

- **FPS monitoring** — Frametime, spikes, entity count, player velocity tracked every tick
- **Pressure scoring** — Combines metrics into a 0–100 pressure score with configurable sensitivity
- **Stage transitions** — 5 stages (Normal → Minor → Moderate → Heavy → Emergency) with hysteresis and gradual recovery
- **Entity distance scaling** — Reduces entity render radius under pressure (EntityOptimizer)
- **Animation culling** — Skips entity animations beyond distance threshold (AnimationOptimizer)
- **Cloud reduction** — Reduces cloud rendering at Moderate+; disables at Emergency (CloudOptimizer)
- **Weather particle filtering** — Caps rain particle count, culls distant rain, gates thunder flashes (WeatherOptimizer)
- **Particle throttling** — Caps total particles, reduces spawn probability, skips distant updates (ParticleOptimizer)
- **Chunk upload prioritization** — Queues rebuild tasks by distance to camera (ChunkUploadOptimizer)
- **Transparency sort reduction** — Reduces frequency of translucent layer re-sorting (TransparencyOptimizer)
- **UI update throttling** — Reduces chat tick frequency when under pressure (UIOptimizer)
- **Shadow resolution scaling** — Lowers Iris/OptiFine shadow distance and resolution (ShadowOptimizer)
- **ModMenu GUI** — Full configuration screen with Cloth Config
- **Debug overlay** — Shows FPS, pressure score, stage, active modules, estimated savings
- **Compatibility scanner** — Detects Sodium, Iris, FerriteCore, etc.; disables overlapping modules to avoid conflicts

## Mixin-Hooked Modules

All 9 modules are wired to game code:

| Module | Hook | Mechanism |
|--------|------|-----------|
| ParticleOptimizer | `ParticleEngine.add(Particle)` | Mixin (require=0) |
| EntityOptimizer | `EntityRenderDispatcher.shouldRender()` + `options.entityDistanceScaling()` | Mixin + Options API |
| AnimationOptimizer | `EntityRenderDispatcher.shouldRender()` | Mixin (require=0, same hook, different thresholds) |
| ChunkUploadOptimizer | `SectionRenderDispatcher.rebuildSectionSync()` | Mixin (require=0) |
| TransparencyOptimizer | `RenderSection.resortTransparency()` | Mixin (require=0) |
| UIOptimizer | `ChatComponent.tick()` | Mixin (require=0) |
| CloudOptimizer | `options.cloudStatus()` | Options API |
| WeatherOptimizer | Particle count / distance / thunder flash filtering | Internal param hooks |
| ShadowOptimizer | Iris/OptiFine shadow distance/resolution | Reflection |

See [ARCHITECTURE.md](ARCHITECTURE.md) for details on the module system.

## Installation

1. Download the Pulse JAR from the [Releases](https://github.com/bitzCognautic/Pulse/releases) page.
2. Place the JAR into `.minecraft/mods/`.
3. Ensure **Fabric API** is also installed.
4. Launch Minecraft. Configure Pulse via ModMenu (optional).

## Requirements

See `gradle.properties` for exact version pins. Current values:
- **Minecraft** (Mojmap) + **Fabric Loader** + **Fabric API**
- **Java 25**

## Building from Source

```
./gradlew build
```

The compiled JAR will be in `build/libs/`.

## License

MIT — see [LICENSE](LICENSE).
