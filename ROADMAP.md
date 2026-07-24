# Pulse Roadmap

## v0.1.0-alpha — Core Engine + Basic Modules

- [x] Project scaffold and build system
- [x] Metrics collection (frametime sampling, sliding window)
- [x] Performance prediction (moving average + trend)
- [x] Budget scheduler with priority queue
- [x] Module interface and registry
- [x] Particle Optimizer module
- [x] Entity Render Distance Controller module
- [x] Basic configuration file (JSON)
- [ ] ModMenu integration
- [ ] HUD overlay (basic)
- [ ] Compatibility system
- [ ] Documentation

## v0.5.0-beta — All Modules + HUD + Config

- [ ] Animation Throttler module
- [ ] Lighting Culler module
- [ ] Sound Distance Culler module
- [ ] Biome Transition LOD module
- [ ] Weather Intensity Manager module
- [ ] Full ModMenu config GUI
- [ ] HUD overlay (all features: budget gauge, module states, FPS)
- [ ] Configuration import/export
- [ ] Sodium compatibility integration
- [ ] Lithium compatibility integration
- [ ] Performance regression test suite
- [ ] Public beta release

## v1.0.0 — Stable Release

- [ ] All modules stable and tested
- [ ] Performance benchmarks published
- [ ] API freeze for module system
- [ ] Full documentation complete
- [ ] Profiling guide and methodology
- [ ] Community feedback integrated
- [ ] Release on Modrinth and CurseForge

## Future

- **Adaptive Render Distance** — Dynamically adjust view distance based on frametime budget, integrated with Sodium's render distance slider.
- **Dynamic Resolution** — Scale render resolution down during heavy load and back up when recovered (requires Iris or Sodium compatibility).
- **ML Prediction** — Use a lightweight on-device ML model to predict frametime drops before they occur, based on patterns in entity density, chunk updates, and particle counts.
- **Plugin API** — Allow third-party mods to register custom Pulse modules through a public API, enabling other mod developers to make their features Pulse-aware.
- **Multiplayer Awareness** — Coordinate Pulse state across a server to balance resource usage on shared worlds.
