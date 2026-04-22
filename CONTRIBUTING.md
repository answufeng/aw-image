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
4. Extend the `demo` app if the change is user-visible (or document manual QA in the PR)
5. Ensure the build passes: `./gradlew :aw-image:assembleRelease :aw-image:lintRelease :aw-image:ktlintCheck :demo:assembleRelease`
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

# Build the library and demo (release smoke / R8)
./gradlew :aw-image:assembleRelease :demo:assembleRelease

# Lint & style
./gradlew :aw-image:lintRelease :aw-image:ktlintCheck

# Install demo for manual QA
./gradlew :demo:installDebug
```

## Pre-release checks (R8)

Keep consumer rules aligned with Coil/OkHttp/Kotlin metadata. After tightening ProGuard, use `-printusage` on a host or sample app and regression-test list scrolling and cache behavior.

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
