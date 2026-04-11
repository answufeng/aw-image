# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-04-11

### Added
- Initial release of aw-image
- Zero-config image loading based on Coil
- DSL API: `loadImage`, `loadCircle`, `loadRounded`, `loadBlur`
- Image preloading: single, batch, get Drawable
- Built-in transformations: Grayscale, ColorFilter, Border (with circle support), Blur
- Cache management: clear memory/disk cache
- GIF support via coil-gif
- Configurable memory/disk cache size
