# Development Guide

## Setup

### Prerequisites

- **Java 25** (JDK 25+)
- **Git**
- A Minecraft development environment (Fabric Loom handles this)

### Clone and Import

```bash
git clone https://github.com/yourorg/Pulse.git
cd Pulse
```

Open the project in your IDE (IntelliJ IDEA recommended). Gradle import should happen automatically. If not, run:

```bash
./gradlew idea
```

### First Build

```bash
./gradlew build
```

This downloads dependencies, compiles the mod, runs checks, and produces a JAR in `build/libs/`.

## Build Commands

| Command | Description |
|---|---|
| `./gradlew build` | Full build: compile, test, lint |
| `./gradlew runClient` | Launch Minecraft with the mod |
| `./gradlew test` | Run unit tests only |
| `./gradlew check` | Run all verification tasks |
| `./gradlew clean` | Clean build outputs |
| `./gradlew :publish` | Publish to local Maven (for testing) |

## Testing Approach

- **Unit tests** cover core engine logic: metrics collection, prediction, scheduling.
- **Module tests** verify level transitions produce expected value changes.
- **Integration tests** run inside a Minecraft environment to verify modules interact correctly with game state.
- **Performance regression tests** measure frametime impact of each module.

Run tests with:

```bash
./gradlew test
```

## Profiling and Performance Analysis

Pulse is itself a performance-sensitive mod. When contributing, ensure your changes do not introduce measurable overhead.

### Tools

- **JMC (JDK Mission Control)** — Flight Recorder recordings for method-level profiling.
- **Spark** — In-game profiler (install Spark mod alongside Pulse).
- **Minecraft's built-in F3 debug** — Basic frametime monitoring.

### Methodology

1. Establish a baseline: run a standard scene without Pulse for 60 seconds.
2. Run the same scene with Pulse (default config) and record the overhead.
3. Run with your module changes and compare.
4. Overhead should not exceed 0.5 ms (≈3% at 60 FPS).

Create a profiling profile in JMC that captures:
- CPU sampling every 10 ms
- Allocation profiling
- Thread contention

## Adding a New Module

1. Create a new package under `src/main/java/io/github/yourorg/pulse/module/<name>/`.
2. Implement the `Module` interface.
3. Define config options in `PulseConfig.java`.
4. Add a default level entry in `ModuleRegistry`.
5. Register compatibility rules if needed.
6. Add configuration documentation to `CONFIGURATION.md`.
7. Write unit tests for level transitions.

See `docs/templates/module-template.md` for a ready-to-use implementation template.
