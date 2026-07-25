# Pulse Architecture

## Overview

Pulse is a layered performance monitor that sits between Minecraft's client tick loop and game settings. It observes framerate metrics, computes a pressure score, maps it to optimization stages, and applies cosmetic setting changes.

```
Client Tick Loop
       │
       ▼
┌──────────────────────┐
│  PerformanceEngine    │ ← frametime, FPS, spikes, entity count, velocity
│  (rolling windows)    │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│  PressureEngine       │ ← normalizes metrics → 0–100 pressure score
│  (weighted sum)       │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│  AdaptiveManager      │ ← maps score → OptimizationStage, handles recovery
│  (hysteresis + timer) │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│  PulseClient          │ ← applies real changes: entityDist, clouds, weather
│  (END_CLIENT_TICK)    │
└──────────────────────┘
```

## Data Flow

### Per Tick (END_CLIENT_TICK)

1. **PerformanceEngine.update()** — captures current FPS, frametime, spike detection, player velocity, entity count, GC events
2. **PressureEngine.update()** — normalizes each metric (0–1), multiplies by weights (total 1.0), sums to a 0–100 pressure score with rate-limiting and hysteresis
3. **AdaptiveManager.update()** — translates pressure score into `OptimizationStage`, manages recovery delays (wait N ticks before stepping down)
4. **ModuleManager.update(stage)** — calls `update()` on every registered module (none actually change game state yet)
5. **PulseClient.applyOptimizations()** — the only place real game changes happen:
   - `options.entityDistanceScaling().set()` — reduces entity render distance
   - `options.cloudStatus().set()` — disables clouds at Moderate+
   - `level.setRainLevel()` / `level.setThunderLevel()` — damps weather
   - Saves originals on first pressure; restores on return to Normal

## Package Structure

```
io.github.pulse/
  Pulse.java                     — Fabric ModInitializer, wires all singletons
  api/
    OptimizationStage.java       — NORMAL / MINOR / MODERATE / HEAVY / EMERGENCY
    ModuleType.java              — enum: PARTICLES, ENTITIES, CHUNKS, CLOUDS, ...
    AdaptiveModule.java          — interface for module framework
    PulseAPI.java                — static accessors for engine state
  performance/
    PerformanceEngine.java       — rolling frametime stats, FPS, entity tracking
    RollingStatistics.java       — sliding-window statistics
  pressure/
    PressureEngine.java          — weighted metric normalisation → 0–100 score
  adaptive/
    AdaptiveManager.java         — stage mapping with hysteresis and recovery delay
  modules/
    ModuleManager.java           — registers/updates all modules
    ParticleOptimizer.java       — stub (no game hook)
    EntityOptimizer.java         — stub (no game hook)
    CloudOptimizer.java          — stub (no game hook)
    WeatherOptimizer.java        — stub (rain/thunder done in PulseClient directly)
    AnimationOptimizer.java      — stub
    ShadowOptimizer.java         — stub with Iris/OptiFine reflection (untested)
    TransparencyOptimizer.java   — stub
    UIOptimizer.java             — stub
    ChunkUploadOptimizer.java    — stub
  budget/
    BudgetSystem.java            — tracks frametime vs target, estimates savings
  config/
    ConfigManager.java           — ModMenu integration + JSON save/load
    PulseConfig.java             — config POJO (targetFps, sensitivity, etc.)
  overlay/
    HudOverlay.java              — debug overlay registered with HudElementRegistry
  compat/
    CompatibilityManager.java    — mod list scanner, marks stubs as disabled/reduced
  client/
    PulseClient.java             — ClientModInitializer, applies real optimizations
  util/
    Constants.java               — thresholds, window sizes, recovery times
```

## Module System (Framework Only)

`AdaptiveModule` is an interface with `initialize()`, `update(stage)`, `enable()`, `disable()`, `recover()`, `shutdown()`. All 9 modules implement it. Their `update()` is called every tick with the current stage. However, none of them hook into Minecraft's rendering systems because:

- **The current Mojmap version rewrote rendering** — old mixin targets (`LevelRenderer`, `EntityRenderDispatcher`, `ParticleEngine`) were renamed/restructured
- **No Fabric events exist** for per-entity or per-particle culling in the current pipeline
- The module framework is structural scaffolding awaiting correct Fabric event hooks

Real optimizations bypass the module system entirely and directly change `Options` and `ClientLevel` state in `PulseClient`.
