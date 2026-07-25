# Pulse — Adaptive Performance Manager for Minecraft

![Build](https://img.shields.io/github/actions/workflow/status/bitzCognautic/Pulse/build.yml?branch=main&label=build)
![License](https://img.shields.io/github/license/bitzCognautic/Pulse)
![Fabric](https://img.shields.io/badge/Fabric%20Loader-0.19.3%2B-orange)

Pulse continuously monitors framerate performance and automatically adjusts cosmetic settings to maintain smooth gameplay. When performance recovers, changes are gradually reversed.

**Status: Pre-alpha.** The core engine works. 3 of 9 modules are wired to Minecraft's Options API; the rest track internal parameters only (see below).

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

## Internal-Parameter-Only Modules

These modules register and track internal state every tick, but have **no game effect** in the current Minecraft version (26.2 Mojmap). They await mixin targets or Fabric events that don't exist yet in the rewritten rendering pipeline:

- ParticleOptimizer
- AnimationOptimizer
- TransparencyOptimizer
- UIOptimizer
- ChunkUploadOptimizer
- ShadowOptimizer (has Iris/OptiFine reflection path, untested)

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
