# Pulse Roadmap

## Current Status (Pre-alpha)

### Working
- PerformanceEngine (frametime/FPS tracking, rolling windows)
- PressureEngine (weighted normalisation → 0–100 score)
- AdaptiveManager (stage mapping, hysteresis, recovery timers)
- PulseClient optimizations via Options API:
  - Entity distance scaling
  - Cloud status (off at Moderate+)
  - Rain/thunder intensity reduction
- Debug overlay (FPS, pressure, stage, active modules)
- ModMenu configuration GUI (Cloth Config)
- JSON config save/load
- Compatibility mod scanner (no actual conflicts yet)

### Framework Only (No Game Hooks)
- ParticleOptimizer
- EntityOptimizer
- CloudOptimizer
- WeatherOptimizer
- AnimationOptimizer
- ShadowOptimizer (Iris/OptiFine reflection exists but untested)
- TransparencyOptimizer
- UIOptimizer
- ChunkUploadOptimizer

## Planned Work

### Short Term (Module Wiring)
- Research Fabric API 26.2 `LevelExtractionEvents` / `LevelRenderEvents` for correct hook points
- Wire ParticleOptimizer into particle extraction/render pipeline
- Wire remaining modules using LevelRenderEvents or lightweight mixins
- Connect module savings to BudgetSystem so the overlay shows real impact

### Medium Term
- Adaptive render distance (non-invasive, via Options API)
- Per-module config options (thresholds per module)
- Better recovery: restore individual settings progressively
- Performance regression test harness

### Long Term
- Dynamic resolution scaling (requires Iris/Sodium rendering API)
- Public API for third-party mods to register Pulse-aware modules
- Multiplayer awareness for server-coordinated optimization
