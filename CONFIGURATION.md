# Pulse Configuration Reference

Configuration is managed through the ModMenu GUI (`config/pulse.json` is auto-generated on first launch).

## General Settings

| Option | Type | Default | Description |
|---|---|---|---|
| `targetFps` | int | `60` | Target framerate. Higher = Pulse reacts earlier to drops. |
| `pressureSensitivity` | float | `1.0` | Multiplier on the pressure score. >1.0 escalates faster, <1.0 is slower. |
| `recoverySpeed` | float | `1.0` | Multiplier on recovery tick count. >1.0 recovers faster. |
| `maxOptimizationStage` | enum | `EMERGENCY` | Capped stage. `NORMAL` effectively disables optimizations. |
| `debugMode` | boolean | `false` | Shows the on-screen debug overlay (FPS, pressure, stage, modules). |

## Module Toggles

Every module type has an `enabled` toggle (default: `true`). Modules with a public API hook will apply game-state changes when enabled. Modules without one track internal parameters only (ready for future mixin targets).

| Module | Game Hook | Effect |
|---|---|---|
| `ENTITIES` | `options.entityDistanceScaling()` | Reduces entity render distance under pressure |
| `CLOUDS` | `options.cloudStatus()` | Disables clouds at Moderate+ |
| `WEATHER` | `level.setRainLevel()/setThunderLevel()` | Damps rain/thunder intensity |
| `PARTICLES` | None (26.2 Mojmap has no `ParticleStatus` enum) | Internal params only |
| `ANIMATIONS` | None | Internal params only |
| `TRANSPARENCY` | None | Internal params only |
| `UI` | None | Internal params only |
| `CHUNKS` | None | Internal params only |
| `SHADOWS` | Iris/OptiFine reflection | Adjusts shadow distance/resolution if shaders active |

## JSON Format (`config/pulse.json`)

```json
{
  "targetFps": 60,
  "pressureSensitivity": 1.0,
  "recoverySpeed": 1.0,
  "maxOptimizationStage": "EMERGENCY",
  "moduleEnabled": {
    "PARTICLES": true,
    "ENTITIES": true,
    "CHUNKS": true,
    "CLOUDS": true,
    "SHADOWS": true,
    "TRANSPARENCY": true,
    "WEATHER": true,
    "ANIMATIONS": true,
    "UI": true,
    "RENDER_DISTANCE": true
  },
  "debugMode": false
}
```
