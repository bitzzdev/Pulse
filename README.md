# Pulse — Adaptive Performance Manager for Minecraft

![Minecraft](https://img.shields.io/badge/Minecraft-26.2-blueviolet)
![Fabric](https://img.shields.io/badge/Fabric-0.19.3%2B-orange)
![License](https://img.shields.io/badge/License-MIT-green)

Pulse continuously monitors framerate performance and automatically adjusts cosmetic settings to maintain smooth gameplay. When performance recovers, changes are gradually reversed.

**Status: Pre-alpha.** The core engine (FPS monitoring, pressure scoring, stage transitions) works. Real game optimizations are limited — see below.

## What Works Now

- **FPS monitoring** — Tracks frametime, spikes, entity count, player velocity every tick
- **Pressure scoring** — Combines metrics into a 0–100 pressure score with configurable sensitivity
- **Stage transitions** — 5 stages (Normal → Minor → Moderate → Heavy → Emergency) with hysteresis and gradual recovery
- **Entity distance scaling** — Reduces entity render radius under pressure via `options.entityDistanceScaling()`
- **Cloud reduction** — Disables clouds at Moderate+ stage via `options.cloudStatus()`
- **Weather dampening** — Reduces rain/thunder intensity proportionally to pressure
- **ModMenu GUI** — Full configuration screen with Cloth Config
- **Debug overlay** — Shows FPS, pressure score, stage, active modules, estimated savings
- **Compatibility scanner** — Detects Sodium, Iris, Lithium, etc.; disables stubs to avoid false conflicts
- **PerformanceEngine** — Rolling window frametime statistics (1s/5s/15s/60s)
- **AdaptiveManager** — Recovery cooldowns prevent oscillation (staged recovery timing)

## What's a Framework Only (Planned)

These module classes are registered and their `update()` is called every tick, but **none are wired into Minecraft's rendering pipeline** yet. They have no effect on the game. Pulse's actual optimizations happen directly in the tick loop, not through the module system.

- ParticleOptimizer
- EntityOptimizer
- CloudOptimizer
- WeatherOptimizer (rain/thunder reduction works via direct API; particle-level control does not)
- AnimationOptimizer
- ShadowOptimizer
- TransparencyOptimizer
- UIOptimizer
- ChunkUploadOptimizer

See [ROADMAP.md](ROADMAP.md) for future plans.

## Installation

1. Download the Pulse JAR from the [Releases](https://github.com/bitzCognautic/Pulse/releases) page.
2. Place the JAR into `.minecraft/mods/`.
3. Ensure **Fabric API** is also installed.
4. Launch Minecraft. Configure Pulse via ModMenu (optional).

## Requirements

- **Minecraft 26.2** (Mojmap)
- **Fabric Loader 0.19.3+**
- **Fabric API 0.155.2+**
- **Java 25**

## Building from Source

```
./gradlew build
```

The compiled JAR will be in `build/libs/`.

## License

MIT — see [LICENSE](LICENSE).
