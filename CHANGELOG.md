# Changelog

## [0.2.0-alpha] - 2026-07-26

### Fixed
- WeatherOptimizer: removed `level.setRainLevel()`/`setThunderLevel()` manipulation that
  corrupted server weather state, broke sky color, and caused permanent rain / invisible rain
  — now filters weather effects via particle count, render distance, and thunder flash checks only
- CloudOptimizer: fixed one-time `!settingsSaved` guard that could lock in `CloudStatus.OFF`
  as the saved preference if clouds were already disabled when pressure first hit
- CloudOptimizer: EMERGENCY recovery now calls `applyCloudState(true)` to restore the game
  option after force-disabling clouds, instead of leaving it permanently OFF
- WeatherOptimizer: removed `settingsSaved` one-time guard so weather state is re-saved
  whenever the server sends a new rain/thunder level

### Added
- Mixin infrastructure: 5 mixins added, all with `require = 0`:
  - `ParticleEngineMixin` → `ParticleEngine#add(Particle)` ✓ stable
  - `EntityRenderDispatcherMixin` → `EntityRenderDispatcher#shouldRender(Entity, Frustum, ...)` ✓ stable
  - `SectionRenderDispatcherMixin` → `SectionRenderDispatcher#rebuildSectionSync(RenderSection, RenderRegionCache)` ✓ stable
  - `TransparencyMixin` → `RenderSection#resortTransparency()` (0 params) ✓ stable
  - `UIOptimizerMixin` → `ChatComponent#tick()` (0 params) ✓ stable
- All 9 modules now have mixin hooks or direct API access
- Full adaptive lifecycle (save → apply → restore) for EntityOptimizer, CloudOptimizer

### Changed
- WeatherOptimizer: downgraded from "direct setRainLevel/setThunderLevel" to visual-only
  filtering (particle count, distance culling, thunder flash gating)
- README.md, ARCHITECTURE.md, PERFORMANCE.md updated to reflect current module hooks

### Removed
- All `GuiGraphics`/`Hud` mixin references (classes removed in 26.2 Mojmap)

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
