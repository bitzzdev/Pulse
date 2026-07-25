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
   - EntityOptimizer → `options.entityDistanceScaling().set()`
   - CloudOptimizer → `options.cloudStatus().set()`
   - WeatherOptimizer → `level.setRainLevel()` / `level.setThunderLevel()`
   - Others (Particle, Animation, Transparency, UI, ChunkUpload, Shadow) → internal parameters only (no public 26.2 API available)

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
    WeatherOptimizer.java        — rain/thunder level saved/applied/restored
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

All untargeted modules now have companion mixin classes in `io.github.pulse.mixin/`. Each `@Inject` uses `require = 0` so the hook silently no-ops if the target method was renamed/restructured in a future Mojmap version. The modules remain functional at the internal-parameter level even when no mixin matches.

| Mixin | Target | Module |
|-------|--------|--------|
| `ParticleEngineMixin` | `ParticleEngine#add(Particle)` | ParticleOptimizer |
| `EntityRenderDispatcherMixin` | `EntityRenderDispatcher#shouldRender(Entity, ...)` | AnimationOptimizer |
| `SectionRenderDispatcherMixin` | `SectionRenderDispatcher#scheduleCompile` | ChunkUploadOptimizer |
| `GuiMixin` | `Hud#renderChat(GuiGraphics, DeltaTracker)`, `renderTooltip`, `renderEffects` | UIOptimizer |
| `ChatComponentMixin` | `ChatComponent#tick` | UIOptimizer |

Three modules wire directly into Minecraft's public API:
- **EntityOptimizer** — reads/writes `options.entityDistanceScaling()`
- **CloudOptimizer** — reads/writes `options.cloudStatus()`
- **WeatherOptimizer** — reads/writes `level.setRainLevel()` / `level.setThunderLevel()`

Six modules use mixin hooks (`require = 0`) backed by internal parameter fallbacks:
- **ParticleOptimizer** — mixin into `ParticleEngine#add` + internal spawn probability/distance
- **AnimationOptimizer** — mixin into `EntityRenderDispatcher#shouldRender` + internal distance thresholds
- **ChunkUploadOptimizer** — mixin into `SectionRenderDispatcher#scheduleCompile` + internal queue logic
- **UIOptimizer** — mixins into `Gui` and `ChatComponent` + internal update intervals
- **TransparencyOptimizer** — internal parameters only (no viable hooks for sort-interval control)
- **ShadowOptimizer** — Iris/OptiFine reflection with internal distance/resolution factors

The current Mojmap version rewrote rendering (old mixin targets were renamed/restructured) and no Fabric events exist for per-entity or per-particle culling. Mixins target the most stable Mojmap method names and silently degrade to internal-parameter-only mode when a target is missing.
