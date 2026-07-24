# Pulse — Adaptive Performance Manager for Minecraft

![Build](https://img.shields.io/github/actions/workflow/status/yourorg/Pulse/build.yml?branch=main)
![License](https://img.shields.io/github/license/yourorg/Pulse)
![Minecraft](https://img.shields.io/badge/Minecraft-26.2-blue)
![Fabric](https://img.shields.io/badge/Fabric-0.19.3%2B-orange)

Pulse continuously monitors rendering performance and intelligently adjusts cosmetic rendering features to maintain smooth gameplay. When performance recovers, every optimization is gradually reversed.

## Features

- **Adaptive Quality Manager** — Dynamically scales cosmetic features based on real-time frametime measurements
- **Particle System Optimizer** — Reduces particle counts during performance pressure, restores when idle
- **Entity Render Distance Controller** — Adjusts entity tracking and rendering distances based on available budget
- **Animation Throttler** — Selectively reduces animation rates (item bob, fluid, etc.) during load
- **Lighting Culler** — Optimizes light update scheduling under heavy load
- **Sound Distance Culler** — Prioritizes nearby sounds and defers distant ones
- **Biome Transition LOD** — Reduces biome color recalculation frequency when frames drop
- **Weather Intensity Manager** — Scales rain/snow particle density and cloud opacity dynamically
- **HUD Overlay** — Real-time visualization of pulse state, budget usage, and active modules
- **Performance Budget System** — Define a target frametime budget; Pulse works to stay within it
- **Compatibility System** — Automatically detects other optimization mods and avoids conflicts
- **ModMenu Integration** — Full configuration GUI
- **Import/Export** — Share your Pulse configuration as JSON

## Installation

1. Download the latest Pulse JAR from the [Releases](https://github.com/yourorg/Pulse/releases) page.
2. Place the JAR into your Minecraft instance's `mods/` folder.
3. Ensure Fabric API is also installed.
4. Launch Minecraft and configure Pulse via ModMenu (optional).

## Requirements

- **Minecraft 26.2** (1.21.5+)
- **Fabric Loader 0.19.3+**
- **Fabric API**
- **Java 25**

## Configuration

Pulse is configured through ModMenu or by editing `config/pulse.json` directly. See [CONFIGURATION.md](CONFIGURATION.md) for a full reference of every option.

## Compatibility

Pulse is designed to coexist with other optimization mods. It automatically detects installed mods and adjusts its behaviour to avoid redundant work. See [COMPATIBILITY.md](COMPATIBILITY.md) for details.

## Building from Source

```
./gradlew build
```

The compiled JAR will be in `build/libs/`.

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.
