# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2026-07-22

### Changed

- Merged with the Shadows Redropped fork by evanbones!
    - Greatly improved performance through batched rendering!
    - Simplified config screen using YACL integration
    - EMI and ImmediatelyFast batched rendering support
- Standard 16x16 item slots are now detected automatically without config

### Removed

- "Slot Bottom-right Colors" config option
- "Outside Shadows" config option
- "Force Depth Refresh" config option

### Fixed

- Shadows overlapping incorrectly
- Hotbar slot detection
- General compatibility fixes

## [Shadows Redropped fork]

## [2.2.2+Redropped] - 2026-07-21

### Fixed

- Fixed incompatibility with Raised.

## [2.2.1+Redropped] - 2026-07-20

### Fixed

- Properly fixed rendering states.

## [2.2.0+Redropped] - 2026-07-19

### Fixed

- Fixed certain tooltips not rendering text (EMI++, Quark).
- Code cleanups.

## [2.1.6+Redropped] - 2026-07-19

### Fixed

- Fixed issues with mods that cancel item rendering.

## [2.1.5+Redropped] - 2026-07-19

### Fixed

- Fixed issues with Immediately Fast.

## [2.1.4+Redropped] - 2026-07-19

### Fixed

- Fixed issues with Cobblemon.

## [2.1.3+Redropped] - 2026-07-19

### Fixed

- Fixed issues with EnhancedTooltips.

## [2.1.2+Redropped] - 2026-07-17

### Fixed

- Fixed Fabric crash.

## [2.1.1+Redropped] - 2026-07-17

### Fixed

- Fixed tooltip rendering sometimes breaking (Quark, EMI).
- Fixed issues with hotbar cropping.

## [2.1.0+Redropped] - 2026-07-15

### Fixed

- Improved edge-case shadows.
- Fixed possible crash with certain modded items.

## [2.0.3+Redropped] - 2026-07-14

### Fixed

- Fixed issues with certain tooltip mods.

## [2.0.2+Redropped] - 2026-07-12

### Fixed

- Fixed issues with Continuity.

## [2.0.1+Redropped] - 2026-07-12

### Fixed

- Fixed issues with Supplementaries.

## [2.0.0+Redropped] - 2026-07-12

### Changed

- Rewrote mod on 1.21 to use batched rendering.

## [Shadow Drop 1.X]

## [1.4.0] - 2026-07-12

### Added

- Custom config screen, accessible through the mods list
  - Allows for previewing of specific items and control over certain contexts to show shadows in
  - Provides warnings for certain config settings and known mod compatibility issues
  - Feedback is appreciated!
- Context for GUIs with and without slots, allowing for control over items in places like the recipe book or JEI, without removing shadows in other places like the advancements screen
  - This context is disabled by default for better performance, so the default mod config should be more modpack-friendly
- Direct support for JEI, EMI, and REI screens

### Changed

- Increased shadow offset range, so shadows can now be to the left/above items if desired
- Real slots are now checked before pixel color, so real slots are now more performant

### Fixed

- Shadows should no longer attempt to render in-world, which fixes issues with some mods (e.g. storage drawers)
- Shadow contexts in some output slots of GUIs

## [1.3.0] - 2026-04-13

### Added

- `offsetItems` config
- Experimental `forceDepthRefresh` config

### Removed

- Redundant `shadowZOffset` configs

### Fixed

- Depth scaling of shadows, should improve compatibility with more modded GUIs
- Items are now offset towards the camera to ensure space behind them for shadows, should also improve mod compatibility (toggleable with `offsetItems` config)
- Items configured as transparent now fully skip shadow rendering
- Some small optimisations

## [1.2.0] - 2026-04-11

### Added

- `uncropUnderCursor` config

### Changed

- Split `shadowZOffset` into `shadowZaOffset` and `shadowZbOffset` configs

### Fixed

- Adjusted how shadows are offset in the Z axis to improve compatibility with mods that scale or rotate items in GUIs

## [1.1.1] - 2026-03-31

### Fixed

- Reimplemented slot cache refresh when GUI changes

## [1.1.0] - 2026-03-28

### Added

- `shadowColor` config
- `shadowZOffset` config
- Expanded shadow and crop contexts

### Fixed

- Rewrite to hopefully improve compatibility with more modded GUIs
- Now compatible with JEI item list
- Still requires `use-batched-renderer` to be set to `false` for EMI

## [1.0.1] - 2026-03-20

### Fixed

- Mixin running on servers, should now just be running on client

## [1.0.0] - 2026-03-20

- First release