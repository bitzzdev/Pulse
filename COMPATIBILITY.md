# Pulse Compatibility

Pulse scans the loaded mod list on startup and logs detected optimization mods. Since Pulse's module system is not yet wired into the game, there are no actual conflicts — the compatibility system is a framework for future use.

## Detected Mods

The scanner checks for: Sodium, ImmediatelyFast, EntityCulling, FerriteCore, Lithium, Noisium, Iris, ModernFix, C2ME, ForgetMeChunk, Dynamic FPS, Enhanced Block Entities, Particles Enhanced.

## Current Behaviour

- Detected mods are logged.
- The scanner marks certain module types as `disabled` or `reduced` based on detected mods (e.g. Sodium → Shadow+Cloud modules disabled).
- **These flags have no effect today** — no module actually touches game state. This is prep work for when modules are wired.

## Manual Override

Set `moduleEnabled` entries in `config/pulse.json` to `false` to hide modules from the active list. The JSON file is editable while the game is closed.

## Reporting Issues

If Pulse causes visual glitches or crashes, [open an issue](https://github.com/bitzCognautic/Pulse/issues) with your mod list and Minecraft version.
