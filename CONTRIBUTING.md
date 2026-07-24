# Contributing to Pulse

Thank you for your interest in contributing! Pulse is a community-driven project and we welcome all forms of contribution.

## How to Contribute

### Reporting Issues

- Search existing issues before opening a new one.
- Use the bug report or feature request templates.
- Include Minecraft version, Fabric Loader version, and a log file if reporting a crash.
- Attach screenshots or video for visual issues.

### Submitting Pull Requests

1. Fork the repository.
2. Create a feature branch from `main`.
3. Make your changes following the code style guidelines.
4. Write tests where applicable.
5. Ensure the project builds with `./gradlew build`.
6. Open a pull request against `main` and fill out the PR template.

## Code Style

- **Java 25** language features are encouraged (pattern matching, records, sealed classes, etc.).
- **No comments in code.** Write self-documenting code with clear identifiers. Comments rot; code does not.
- **JavaDoc is required for all public API** — classes, methods, and fields that form the public contract of a module must have a JavaDoc comment.
- Follow the existing formatting: 4-space indentation, no tabs, braces on the same line.
- Keep methods small and focused. A method should do one thing.
- Use meaningful names. Avoid abbreviations.

## PR Process

1. A maintainer will review your PR within a few days.
2. Address review feedback or explain why a suggestion is out of scope.
3. Once approved, a maintainer will squash-merge your PR.
4. Your commit will appear on `main` and be included in the next release.

## Development Setup

See [DEVELOPMENT.md](DEVELOPMENT.md) for detailed setup instructions, build commands, and profiling guidance.
