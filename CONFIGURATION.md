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

Every module type has an `enabled` toggle (default: `true`). **Note:** Module toggles exist in the config GUI but have no effect yet — no module is wired to the game's rendering pipeline. Toggling them does nothing until module wiring is implemented.

Available modules:
- `PARTICLES`, `ENTITIES`, `CHUNKS`, `CLOUDS`, `SHADOWS`, `TRANSPARENCY`, `WEATHER`, `ANIMATIONS`, `UI`, `RENDER_DISTANCE`

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
