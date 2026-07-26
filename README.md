# Pulse — Adaptive Performance Manager for Minecraft

![Build](https://img.shields.io/github/actions/workflow/status/bitzCognautic/Pulse/build.yml?branch=main&label=build)
![License](https://img.shields.io/github/license/bitzCognautic/Pulse)
![Fabric](https://img.shields.io/badge/Fabric%20Loader-0.19.3%2B-orange)

Pulse continuously monitors framerate performance and automatically adjusts cosmetic settings to maintain smooth gameplay. When performance recovers, changes are gradually reversed.

**Status: Pre-alpha.** The core engine works. All 9 modules have mixin hooks or direct API access.

## What Works

- **FPS monitoring** — Frametime, spikes, entity count, player velocity tracked every tick
- **Pressure scoring** — Combines metrics into a 0–100 pressure score with configurable sensitivity
- **Stage transitions** — 5 stages (Normal → Minor → Moderate → Heavy → Emergency) with hysteresis and gradual recovery
- **Entity distance scaling** — Reduces entity render radius under pressure (EntityOptimizer)
- **Cloud reduction** — Disables clouds at Moderate+ stage (CloudOptimizer; bypassed when Sodium is present)
- **Weather dampening** — Reduces rain/thunder intensity proportionally to pressure (WeatherOptimizer)
- **ModMenu GUI** — Full configuration screen with Cloth Config
- **Debug overlay** — Shows FPS, pressure score, stage, active modules, estimated savings
- **Compatibility scanner** — Detects Sodium, Iris, FerriteCore, etc.; disables overlapping modules to avoid conflicts

## Mixin-Hooked Modules

These modules are connected to game code via mixins and actively modify rendering behavior:

- **ParticleOptimizer** — `ParticleEngine.add()` cancellation for distant/excess particles
- **EntityOptimizer** — `EntityRenderDispatcher.shouldRender()` override for distance-based entity culling  
- **AnimationOptimizer** — `EntityRenderDispatcher.shouldRender()` override for distance-based animation culling (same hook as EntityOptimizer, different thresholds)
- **ChunkUploadOptimizer** — `SectionRenderDispatcher.rebuildSectionSync()` interception for upload queue prioritization
- **TransparencyOptimizer** — `RenderSection.resortTransparency()` frequency reduction
- **UIOptimizer** — `ChatComponent.tick()` frequency reduction

## Direct-API Modules

These modules modify Minecraft settings directly via vanilla Options API or reflection:

- **CloudOptimizer** — Directly sets `options.cloudStatus()` to disable clouds
- **WeatherOptimizer** — Directly calls `level.setRainLevel()` and `level.setThunderLevel()`
- **ShadowOptimizer** — Uses reflection to call Iris/OptiFine shadow distance/resolution APIs
- **EntityOptimizer (partial)** — Also sets `options.entityDistanceScaling()` via Options API

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
