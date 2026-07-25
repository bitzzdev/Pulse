# Changelog

## [Unreleased]
- EntityOptimizer: now owns `options.entityDistanceScaling()` save/apply/restore
- CloudOptimizer: now owns `options.cloudStatus()` save/apply/restore
- WeatherOptimizer: now owns `level.setRainLevel()`/`setThunderLevel()` save/apply/restore
- PulseClient: stripped of all direct optimization logic (pure orchestration)
- ParticleOptimizer: `ParticleStatus` enum does not exist in 26.2 Mojmap; module kept as internal-parameter-only
- Mixin infrastructure: 5 mixins added (`ParticleEngineMixin`, `EntityRenderDispatcherMixin`, `SectionRenderDispatcherMixin`, `GuiMixin`, `ChatComponentMixin`), all with `require = 0` for version compatibility
- Mixin config: `pulse.mixins.json` updated with `"defaultRequire": 0` and 5 client mixin entries
- ARCHITECTURE.md: mixin table added, six-module hook strategy documented
- Build via GitHub Actions only (no local `gradlew build`)

## 0.1.0-alpha - Pre-release

### Added
- Project scaffold with Fabric Loom build system (see `gradle.properties` for version pins)
- `PerformanceEngine` — frametime/FPS tracking with rolling windows (1s/5s/15s/60s)
- `PressureEngine` — weighted normalisation of metrics → 0–100 pressure score
- `AdaptiveManager` — stage transitions (Normal→Emergency) with recovery delay
- ModuleManager with 9 modules (3 wired to Options API, 6 internal-parameter-only)
- `ConfigManager` — ModMenu integration (Cloth Config)
- `PulseConfig` — JSON save/load
- `HudOverlay` — debug overlay (FPS, pressure, stage, modules)
- `CompatibilityManager` — mod scanner (Sodium, Iris, Lithium, etc.)
- `BudgetSystem` — frametime tracking with savings estimation
- Project documentation: README, ARCHITECTURE, DEVELOPMENT, CONFIGURATION, COMPATIBILITY, PERFORMANCE, ROADMAP

### Known Issues
- Particle, Animation, Transparency, UI, ChunkUpload, and Shadow modules have no public API hooks — they track internal parameters only
- ShadowOptimizer's Iris/OptiFine reflection is untested
- No benchmark data for expected FPS gains
- Recovery timing uses fixed ticks (not smoothed interpolation)
