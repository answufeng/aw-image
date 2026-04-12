# Contributing to aw-image

Thank you for your interest in contributing to aw-image! This document provides guidelines for contributions.

## How to Contribute

### Bug Reports

1. Check if the issue already exists in [GitHub Issues](https://github.com/answufeng/aw-image/issues)
2. If not, create a new issue with:
   - Clear description of the problem
   - Steps to reproduce
   - Expected vs actual behavior
   - Android version and device info
   - Library version

### Feature Requests

1. Open a [GitHub Issue](https://github.com/answufeng/aw-image/issues) with the `enhancement` label
2. Describe the use case and expected API design
3. Explain why this feature is valuable for the library

### Pull Requests

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass: `./gradlew :aw-image:testDebugUnitTest`
6. Ensure the build passes: `./gradlew :aw-image:compileDebugKotlin`
7. Commit with a clear message
8. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Add KDoc to all public APIs
- Use `private set` for mutable properties in DSL classes
- Prefer immutable data structures

### Commit Messages

- Use the present tense ("Add feature" not "Added feature")
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
- Limit the first line to 72 characters

## Development Setup

```bash
# Clone the repo
git clone https://github.com/answufeng/aw-image.git

# Build the library
./gradlew :aw-image:assembleDebug

# Run unit tests
./gradlew :aw-image:testDebugUnitTest

# Run Android instrumented tests (requires device/emulator)
./gradlew :aw-image:connectedDebugAndroidTest

# Build and install the demo app
./gradlew :demo:installDebug
```

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
