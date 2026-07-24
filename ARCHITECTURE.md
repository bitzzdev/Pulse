# Pulse Architecture

## High-Level Overview

Pulse is a layered optimization engine that sits between Minecraft's render loop and the cosmetic rendering systems. It observes frametime metrics, predicts performance pressure, applies knobs on rendering modules, and recovers when headroom becomes available.

```
+---------------------------------------------------------------+
|                        Minecraft Render Loop                   |
+---------------------------------------------------------------+
                               |
                               v
+---------------------------------------------------------------+
|  Pulse Engine                                                  |
|  +-------------------+  +------------------+  +--------------+ |
|  | Metrics Collector |  | Performance      |  | Budget       | |
|  | (frametimes, GPU  |  | Predictor        |  | Scheduler   | |
|  |  stats, memory)   |  | (moving avg,     |  | (priority    | |
|  +-------------------+  |  trend analysis)  |  |  queue)      | |
|         |               +------------------+  +--------------+ |
|         v                        v                  v          |
|  +--------------------------------------------------------+   |
|  |                Module Manager                            |   |
|  |  +----------+ +----------+ +----------+ +----------+   |   |
|  |  | Particle | | Entity   | | Animation| | Lighting |   |   |
|  |  | Optimizer| | Culler   | |Throttler | | Culler   |   |   |
|  |  +----------+ +----------+ +----------+ +----------+   |   |
|  |  +----------+ +----------+ +----------+ +----------+   |   |
|  |  | Sound    | | Biome LOD| | Weather  | | (future) |   |   |
|  |  | Manager  | |          | | Manager  | |          |   |   |
|  |  +----------+ +----------+ +----------+ +----------+   |   |
|  +--------------------------------------------------------+   |
+---------------------------------------------------------------+
                               |
                               v
+---------------------------------------------------------------+
|                    Minecraft Rendering Systems                  |
|  (Particles, Entities, Animations, Lighting, Sound, Biomes,   |
|   Weather, ...)                                                |
+---------------------------------------------------------------+
```

## Data Flow: Observe → Predict → Optimize → Recover

### 1. Observe
The `MetricsCollector` samples frametime deltas every rendered frame. It maintains a sliding window of the last N samples and computes aggregate statistics (p50, p95, max, variance). Optional GPU metrics are collected when available.

### 2. Predict
The `PerformancePredictor` feeds metrics through a weighted moving average and a simple trend line. If the trend projects that the frametime budget will be exceeded within the next few frames, a pressure event is emitted.

### 3. Optimize
When pressure is detected, the `BudgetScheduler` activates modules in priority order. Each module reduces the visual cost it controls by one step. The scheduler re-evaluates every frame until the budget is met.

### 4. Recover
When frametime stabilises below threshold for a configurable hold period, modules are restored one step at a time in reverse priority order (most impactful first). Recovery is deliberately gradual to avoid oscillation.

## Package Structure

```
io/github/yourorg/pulse/
  PulseMod.java                  — Fabric mod entrypoint
  config/
    PulseConfig.java             — Root config holder (AutoConfig)
    ConfigSerializer.java        — JSON import/export
    PulseConfigScreen.java       — ModMenu integration
  engine/
    PulseEngine.java             — Core orchestrator
    MetricsCollector.java        — Frametime sampling & aggregation
    PerformancePredictor.java    — Trend analysis & pressure detection
    BudgetScheduler.java         — Priority-based module activation
  module/
    Module.java                  — Module interface
    ModuleRegistry.java          — Module registration & lifecycle
    particle/
      ParticleModule.java        — Particle count scaling
    entity/
      EntityModule.java          — Entity render distance control
    animation/
      AnimationModule.java       — Animation rate throttling
    lighting/
      LightingModule.java        — Light update scheduling
    sound/
      SoundModule.java           — Sound distance culling
    biome/
      BiomeModule.java           — Biome transition LOD
    weather/
      WeatherModule.java         — Weather intensity scaling
  hud/
    PulseHudOverlay.java         — In-game HUD overlay
  compat/
    CompatibilityChecker.java    — Mod detection & conflict resolution
    SodiumCompat.java            — Sodium-specific integration
    LithiumCompat.java           — Lithium-specific integration
```

## Module System

Every optimisation is implemented as a `Module`. Each module:

- Exposes a set of discrete levels (e.g. OFF, LOW, MEDIUM, HIGH, ULTRA).
- Implements `apply(int level)` which sets the rendering parameter to the given level.
- Reports its estimated cost impact via `costImpact()`.
- Registers with the `ModuleRegistry` at initialisation.

Modules are independent and can be disabled individually in the config.

## Performance Budget System

The user defines a target frametime budget (e.g. 16.67 ms for 60 FPS). Pulse maintains a headroom margin (default 10%). The budget scheduler calculates the gap between current frametime and the target minus headroom, then activates module levels until the gap is closed.

Module costs are estimated via pre-benchmarked weight values and are refined at runtime through observation.

## Compatibility System

On initialisation, `CompatibilityChecker` scans the loaded mod list. If known mods are detected (Sodium, Lithium, Phosphor, etc.), Pulse:

- Disables modules that would duplicate existing functionality.
- Adjusts default levels to avoid interference.
- Logs detected mods and applied compatibility rules.

This runs before any module is activated, ensuring safe default behaviour.
