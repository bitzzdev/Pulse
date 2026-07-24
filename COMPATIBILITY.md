# Pulse Compatibility

Pulse is designed to coexist with other optimisation and visual enhancement mods. It actively detects installed mods and adjusts its behaviour to avoid redundant or conflicting operations.

## Known Compatible Mods

| Mod | Status | Notes |
|---|---|---|
| **Sodium** | ✅ Verified | Pulse disables its Lighting Culler module when Sodium is present; Sodium's light engine is more efficient. |
| **Lithium** | ✅ Verified | No overlap. Pulse's entity and AI-related settings complement Lithium's server-side optimisations. |
| **Phosphor** | ✅ Verified | Pulse's Lighting Culler defers to Phosphor's light engine. |
| **Starlight** | ✅ Verified | Same as Phosphor; Lighting Culler is disabled. |
| **Iris** | ✅ Verified | Pulse's weather and biome modules work correctly with Iris shaderpacks. Particle optimisations apply before shader processing. |
| **Sodium Extra** | ✅ Verified | Pulse respects Sodium Extra's animation toggles and does not duplicate them. |
| **Entity Culling** | ✅ Verified | Complementary. Entity Culling skips rendering of occluded entities; Pulse adjusts tracking distance. Together they provide layered benefit. |
| **FerriteCore** | ✅ Verified | No interaction; memory optimisations are independent from Pulse's frametime-based adjustments. |
| **ModernFix** | ✅ Verified | Pulse works alongside ModernFix's dynamic resource loading. |
| **BetterFPS** | ✅ Verified | BetterFPS's algorithm tweaks and Pulse's adaptive scaling are compatible. |
| **OptiFabric** | ⚠️ Partial | OptiFine features that Pulse mirrors (particle settings, animations) may double-control. Disable duplicate modules in OptiFine's Video Settings. |

## Automatic Detection Behaviour

On startup, `CompatibilityChecker` scans the mod list. Detected mods trigger the following rules:

### Sodium Detection
- `LightingModule` is disabled.
- `AnimationModule` skips fluid animation throttling (Sodium handles this).
- HUD overlay is shifted to avoid overlap with Sodium's FPS counter.

### Iris Detection
- Weather intensity changes are smoothed over multiple frames to avoid visual flicker with shader fog.
- Maximum particle reduction level is capped at 3 (of 5) to avoid compatibility issues with shader particle passes.

### Entity Culling Detection
- Entity module's minimum distance floor is raised slightly to avoid Entity Culling doing unnecessary work on already-culled entities.

### Lithium Detection
- No modules are disabled, but recovery step frames are reduced (faster recovery) because Lithium reduces server tick overhead, giving the client more headroom.

## Conflicts and Resolutions

### Manual Override

If Pulse misdetects a mod or you want to override its behaviour, set the relevant options in `config/pulse.json`:

```json
{
  "compat": {
    "autoDetect": true,
    "forceDisableModules": ["lighting"],
    "sodiumIntegration": "enabled"
  }
}
```

Set `autoDetect` to `false` to disable automatic compatibility handling entirely. In this mode all Pulse modules are active regardless of installed mods.

### Reporting Conflicts

If you encounter a conflict with a mod not listed above, please [open an issue](https://github.com/yourorg/Pulse/issues/new) with:
- Mod name and version
- Minecraft version
- Pulse version
- Description of the conflict (visual glitch, crash, performance degradation)

## Sodium Integration Details

When Sodium is detected, Pulse operates in a compatibility mode:

- **Lighting Module**: Fully disabled. Sodium's block face culling and light pipeline are superior.
- **Animation Module**: Fluid animation throttling is handed off to Sodium's `animation.fps` setting. Pulse still throttles item bob, chest, and portal animations.
- **Weather Module**: Cloud opacity scaling works as normal; Sodium renders clouds in its own pipeline.
- **Particle Module**: Fully functional. Sodium accelerates particle rendering, and Pulse adjusts particle counts independently.

The user can force-enable or force-disable Sodium integration via `compat.sodiumIntegration` in the config.
