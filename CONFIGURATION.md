# Pulse Configuration Reference

Configuration is managed via `config/pulse.json` (auto-generated on first launch) or through the ModMenu GUI.

## Core Settings

| Option | Type | Default | Description |
|---|---|---|---|
| `enabled` | boolean | `true` | Master toggle for Pulse |
| `budgetMs` | float | `16.67` | Target frametime budget in milliseconds (≈60 FPS) |
| `headroomMargin` | float | `0.10` | Fraction of budget reserved as headroom (0.10 = 10%) |
| `holdFramesBeforeRecovery` | int | `60` | Frames to wait below threshold before starting recovery |
| `recoveryStepFrames` | int | `20` | Frames between each recovery level step |
| `debugLogging` | boolean | `false` | Enable verbose Pulse engine logging |
| `profileMode` | boolean | `false` | Enable profiling-friendly mode (lower overhead, less adaptivity) |

## Module Settings

### Particle Optimizer

| Option | Type | Default | Description |
|---|---|---|---|
| `particle.enabled` | boolean | `true` | Enable particle count scaling |
| `particle.maxLevel` | int | `4` | Maximum quality level (1–5). 5 = vanilla, 1 = minimal |
| `particle.minLevel` | int | `1` | Minimum quality level during pressure |
| `particle.baseMultiplier` | float | `1.0` | Global particle count multiplier at max quality |

### Entity Render Distance Controller

| Option | Type | Default | Description |
|---|---|---|---|
| `entity.enabled` | boolean | `true` | Enable entity distance control |
| `entity.maxLevel` | int | `4` | Maximum tracking distance level |
| `entity.minLevel` | int | `1` | Minimum tracking distance level during pressure |
| `entity.blockDistance` | int | `64` | Maximum entity tracking range at level 4 (blocks) |

### Animation Throttler

| Option | Type | Default | Description |
|---|---|---|---|
| `animation.enabled` | boolean | `true` | Enable animation rate throttling |
| `animation.maxLevel` | int | `4` | Maximum animation quality level |
| `animation.minLevel` | int | `1` | Minimum animation quality level |
| `animation.throttleItemBob` | boolean | `true` | Throttle item bobbing animation |
| `animation.throttleFluid` | boolean | `true` | Throttle fluid surface animation |
| `animation.throttleChest` | boolean | `false` | Throttle chest opening animation |
| `animation.throttlePortal` | boolean | `true` | Throttle portal overlay animation |

### Lighting Culler

| Option | Type | Default | Description |
|---|---|---|---|
| `lighting.enabled` | boolean | `true` | Enable light update optimization |
| `lighting.maxLevel` | int | `3` | Maximum lighting quality (3 = vanilla) |
| `lighting.minLevel` | int | `1` | Minimum lighting quality during pressure |
| `lighting.deferUpdates` | boolean | `true` | Defer non-critical light updates during pressure |

### Sound Distance Culler

| Option | Type | Default | Description |
|---|---|---|---|
| `sound.enabled` | boolean | `true` | Enable sound distance culling |
| `sound.maxLevel` | int | `3` | Maximum sound range level |
| `sound.minLevel` | int | `1` | Minimum sound range level |
| `sound.farDistance` | int | `64` | Sound audible distance at level 3 (blocks) |
| `sound.cullBelowDb` | float | `-30.0` | Volume threshold (dB) below which sounds are culled |

### Biome Transition LOD

| Option | Type | Default | Description |
|---|---|---|---|
| `biome.enabled` | boolean | `true` | Enable biome LOD |
| `biome.maxLevel` | int | `3` | Maximum biome update frequency (3 = every frame) |
| `biome.minLevel` | int | `1` | Minimum update frequency during pressure |
| `biome.updateIntervalMs` | int | `50` | Milliseconds between biome colour updates at max level |

### Weather Intensity Manager

| Option | Type | Default | Description |
|---|---|---|---|
| `weather.enabled` | boolean | `true` | Enable weather intensity scaling |
| `weather.maxLevel` | int | `3` | Maximum weather intensity (3 = vanilla) |
| `weather.minLevel` | int | `1` | Minimum weather intensity during pressure |
| `weather.cloudOpacityScale` | float | `1.0` | Cloud opacity multiplier at max level |

## HUD Settings

| Option | Type | Default | Description |
|---|---|---|---|
| `hud.enabled` | boolean | `true` | Enable the Pulse HUD overlay |
| `hud.showBudget` | boolean | `true` | Show frametime budget gauge |
| `hud.showModules` | boolean | `true` | Show per-module activation level |
| `hud.showFps` | boolean | `true` | Show current FPS |
| `hud.position` | string | `"top_right"` | HUD position: `top_left`, `top_right`, `bottom_left`, `bottom_right` |
| `hud.scale` | float | `1.0` | HUD text scale multiplier |

## Compatibility Settings

| Option | Type | Default | Description |
|---|---|---|---|
| `compat.autoDetect` | boolean | `true` | Automatically detect and adjust for other mods |
| `compat.forceDisableModules` | list | `[]` | Force-disable specific module IDs (e.g. `["lighting"]`) |
| `compat.sodiumIntegration` | string | `"auto"` | Sodium integration mode: `auto`, `enabled`, `disabled` |

## Import/Export

Pulse supports importing and exporting configuration as JSON files.

### Export

```json
{
  "version": 1,
  "enabled": true,
  "budgetMs": 16.67,
  "modules": {
    "particle": { "enabled": true, "maxLevel": 4 },
    "entity": { "enabled": true, "maxLevel": 4 },
    "animation": { "enabled": true, "maxLevel": 4 },
    "lighting": { "enabled": true, "maxLevel": 3 },
    "sound": { "enabled": true, "maxLevel": 3 },
    "biome": { "enabled": true, "maxLevel": 3 },
    "weather": { "enabled": true, "maxLevel": 3 }
  }
}
```

### Import

From the ModMenu GUI, select **Import Config** and choose a JSON file. The config is validated and applied immediately. Export via **Export Config** saves the current config to a timestamped file in the game directory.
