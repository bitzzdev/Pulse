# Module Implementation Template

Use this template to create new Pulse optimisation modules.

## Step 1: Create the Package

Create a new package under `src/main/java/io/github/yourorg/pulse/module/<name>/`.

## Step 2: Implement the Module Interface

```java
package io.github.yourorg.pulse.module.<name>;

import io.github.yourorg.pulse.module.Module;

public class <Name>Module implements Module {
    private int currentLevel;
    private final <Name>Config config;

    public <Name>Module(<Name>Config config) {
        this.config = config;
        this.currentLevel = config.maxLevel();
    }

    @Override
    public String id() {
        return "<name>";
    }

    @Override
    public String displayName() {
        return "<Display Name>";
    }

    @Override
    public int levelCount() {
        return config.maxLevel();
    }

    @Override
    public int currentLevel() {
        return currentLevel;
    }

    @Override
    public void applyLevel(int level) {
        this.currentLevel = level;
        // Apply the level to the relevant game state
    }

    @Override
    public float costImpact() {
        // Return a normalised cost multiplier (0.0 = no cost, 1.0 = max cost)
        return (float) currentLevel / config.maxLevel();
    }
}
```

## Step 3: Add Config Options

In `io.github.yourorg.pulse.config.PulseConfig`, add a nested config class:

```java
public static class <Name>Config {
    @ConfigEntry(name = "enabled")
    public boolean enabled = true;

    @ConfigEntry(name = "maxLevel")
    public int maxLevel = <default>;

    @ConfigEntry(name = "minLevel")
    public int minLevel = 1;
}
```

## Step 4: Register the Module

In `io.github.yourorg.pulse.module.ModuleRegistry`, add:

```java
registry.register(new <Name>Module(config.<name>));
```

## Step 5: Add Compatibility Rules

If the new module conflicts with existing mods, add rules in `io.github.yourorg.pulse.compat.CompatibilityChecker`.

## Step 6: Document

Add configuration entries to `CONFIGURATION.md` and if the module is notable, add it to `README.md`.

## Guidelines

- Each module must be **independently disableable**.
- Level transitions must be **instant** and **safe to call every frame**.
- Do not allocate memory during `applyLevel()` — allocate in the constructor.
- Use the cost impact value to help the BudgetScheduler decide activation order.
- Test with both vanilla and Sodium rendering pipelines.
