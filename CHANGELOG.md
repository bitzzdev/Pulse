# Changelog

All notable changes to Pulse are documented in this file.

## [0.1.0-alpha] - 2026-07-24

### Added
- Initial project structure with Fabric Loom build system
- PulseEngine orchestrator with observe → predict → optimise → recover loop
- MetricsCollector: frametime sampling with configurable sliding window
- PerformancePredictor: weighted moving average and trend analysis
- BudgetScheduler: priority-queue-based module activation
- Module interface (`io.github.yourorg.pulse.module.Module`)
- ModuleRegistry for lifecycle management
- ParticleOptimizer: particle count scaling over 5 levels
- EntityRenderDistanceController: entity tracking distance over 4 levels
- JSON configuration file (`config/pulse.json`) with auto-generation
- Core configuration options: budget, headroom, recovery timing
- Project documentation: README, ARCHITECTURE, DEVELOPMENT, CONFIGURATION, COMPATIBILITY, PERFORMANCE
- MIT License

### Known Issues
- ModMenu GUI is not yet implemented; edit `config/pulse.json` directly
- HUD overlay is not yet implemented
- Compatibility system is stubbed but not active
