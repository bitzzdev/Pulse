# Changelog

## 0.1.0-alpha - Pre-release

### Added
- Project scaffold with Fabric Loom build system (see `gradle.properties` for version pins)
- `PerformanceEngine` — frametime/FPS tracking with rolling windows (1s/5s/15s/60s)
- `PressureEngine` — weighted normalisation of metrics → 0–100 pressure score
- `AdaptiveManager` — stage transitions (Normal→Emergency) with recovery delay
- `PulseClient` — per-tick optimizations via Options/ClientLevel API:
  - Entity distance scaling
  - Cloud status (off at Moderate+)
  - Rain/thunder intensity reduction
- `ModuleManager` with 9 stub modules (framework only, no game hooks)
- `ConfigManager` — ModMenu integration (Cloth Config)
- `PulseConfig` — JSON save/load
- `HudOverlay` — debug overlay (FPS, pressure, stage, modules)
- `CompatibilityManager` — mod scanner (Sodium, Iris, Lithium, etc.)
- `BudgetSystem` — frametime tracking with savings estimation
- Project documentation: README, ARCHITECTURE, DEVELOPMENT, CONFIGURATION, COMPATIBILITY, PERFORMANCE, ROADMAP

### Known Issues
- All 9 module classes are stubs — they register, update, but have no effect on rendering
- ShadowOptimizer's Iris/OptiFine reflection is untested
- Cloud optimization (`options.cloudStatus()`) compiles but CI build has not confirmed it works
- No benchmark data for expected FPS gains
- Recovery timing uses fixed ticks (not smoothed interpolation)
