# Pulse Architecture

## Overview

Pulse is a layered performance monitor that sits between Minecraft's client tick loop and game settings. It observes framerate metrics, computes a pressure score, maps it to optimization stages, and applies cosmetic setting changes via named modules.

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
│  ModuleManager        │ ← calls update(stage) on every module
│  (each module owns    │
│   its own apply())    │
└──────────────────────┘
```

## Data Flow

### Per Tick (END_CLIENT_TICK)

1. **PerformanceEngine.update()** — captures current FPS, frametime, spike detection, player velocity, entity count, GC events
2. **PressureEngine.update()** — normalizes each metric (0–1), multiplies by weights (total 1.0), sums to a 0–100 pressure score with rate-limiting and hysteresis
3. **AdaptiveManager.update()** — translates pressure score into `OptimizationStage`, manages recovery delays (wait N ticks before stepping down)
4. **ModuleManager.update(stage)** — calls `update()` on every registered module; each module that has a game-hook applies its own changes:
   - EntityOptimizer → `options.entityDistanceScaling().set()` + `EntityRenderDispatcher.shouldRender()` mixin
   - CloudOptimizer → `options.cloudStatus().set()`
   - WeatherOptimizer → visual-only filtering (particle count, distance culling, thunder flash gating)
   - Others (Particle, Animation, Transparency, UI, ChunkUpload, Shadow) → mixin hooks or internal parameters

**PulseClient no longer applies any optimizations directly.** All game-state changes are owned by individual modules, each with its own save/restore lifecycle.

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
    EntityOptimizer.java         — entityDistanceScaling saved/applied/restored
    CloudOptimizer.java          — cloudStatus saved/applied/restored
    WeatherOptimizer.java        — weather particle count, distance, and thunder flash filtering
    ParticleOptimizer.java       — internal parameters only
    AnimationOptimizer.java      — internal parameters only
    TransparencyOptimizer.java   — internal parameters only
    UIOptimizer.java             — internal parameters only
    ChunkUploadOptimizer.java    — internal parameters only
    ShadowOptimizer.java         — Iris/OptiFine reflection (if shader mod present)
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
    PulseClient.java             — ClientModInitializer, pure orchestration
  util/
    Constants.java               — thresholds, window sizes, recovery times
```

## Module System

`AdaptiveModule` is an interface with `initialize()`, `update(stage)`, `enable()`, `disable()`, `recover()`, `shutdown()`. All 9 modules implement it. Their `update()` is called every tick with the current stage.

### Mixin infrastructure (require = 0)

Each mixin uses `require = 0` so the hook silently no-ops if the target method is renamed/restructured in a future Mojmap version. Modules remain functional at the internal-parameter level even when no mixin matches.

| Mixin | Target | Module |
|-------|--------|--------|
| `ParticleEngineMixin` | `ParticleEngine#add(Particle)` | ParticleOptimizer |
| `EntityRenderDispatcherMixin` | `EntityRenderDispatcher#shouldRender(Entity, Frustum, double, double, double)` | EntityOptimizer / AnimationOptimizer |
| `SectionRenderDispatcherMixin` | `SectionRenderDispatcher#rebuildSectionSync(RenderSection, RenderRegionCache)` | ChunkUploadOptimizer |
| `TransparencyMixin` | `RenderSection#resortTransparency()` (0 params) | TransparencyOptimizer |
| `ChatComponentMixin` | `ChatComponent#tick()` (0 params) | UIOptimizer |

Five modules wire into game code via mixins:

- **ParticleOptimizer** — mixin into `ParticleEngine#add` for particle cancellation
- **EntityOptimizer** — mixin into `EntityRenderDispatcher#shouldRender` + Options API for `entityDistanceScaling`
- **AnimationOptimizer** — mixin into `EntityRenderDispatcher#shouldRender` (same hook, different distance thresholds)
- **ChunkUploadOptimizer** — mixin into `SectionRenderDispatcher#rebuildSectionSync` for upload queue prioritization
- **TransparencyOptimizer** — mixin into `RenderSection#resortTransparency` for sort frequency reduction
- **UIOptimizer** — mixin into `ChatComponent#tick` for chat update frequency reduction

Three modules use direct APIs:
- **CloudOptimizer** — reads/writes `options.cloudStatus()`
- **WeatherOptimizer** — visual-only filtering (no direct game-state manipulation)
- **ShadowOptimizer** — Iris/OptiFine reflection with internal distance/resolution factors
