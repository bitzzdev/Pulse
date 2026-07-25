# Pulse Performance

## Overhead

Pulse runs entirely in the client tick loop. Its overhead is minimal:

| Component | Typical |
|---|---|
| PerformanceEngine.update() | < 0.01 ms |
| PressureEngine.update() | < 0.01 ms |
| AdaptiveManager.update() | < 0.01 ms |
| ModuleManager.update() | < 0.01 ms |
| **Total per tick** | **< 0.04 ms** |

At 60 FPS (16.67 ms budget), Pulse consumes < 0.3% of frametime.

## Expected Savings

**No formal benchmarks exist yet.** The mod is pre-alpha. Real-world savings depend on scene complexity.

| Optimization | Expected Gain | Status |
|---|---|---|
| Entity distance scaling | 5–15% in dense entity scenes | Active (via EntityOptimizer) |
| Cloud disabling | 2–5% | Active (via CloudOptimizer) |
| Rain/thunder reduction | 1–3% | Active (via WeatherOptimizer) |
| Particle reduction | TBD | Internal params only |
| Animation / Transparency / UI | TBD | Internal params only |
| Chunk upload throttling | TBD | Internal params only |
| Shadow resolution (shader mods) | TBD | Iris reflection |

## Profiling

Run a JFR recording alongside Pulse:

```
./gradlew runClient
```

Then attach JDK Mission Control to the process.
