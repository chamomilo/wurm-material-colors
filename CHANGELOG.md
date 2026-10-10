# Changelog

## 0.3.23 — 2026-10-10

- Embed canonical Chamomilo Updater 1.2.7 and its complete Chamomilo UI 0.4.10
  dependency, with exact version, entry and byte verification.
- Rewrite the GitHub README and generate its plain-text README.txt for releases.
- Remove Markdown documentation and the project screenshot from installable
  ZIPs. Keep the screenshot in the GitHub README and verify the five-file ZIP.
- Remove research notes, imported release records, old ZIPs and the one-off mask
  audit script from the project folder.

## 0.3.22 — 2026-10-10

- Embed the complete canonical updater 1.2.6 with no Off choice. The updater
  is the only language selector for capable mods and applies a concrete shared
  language at startup and after every choice. Retain exact module/hash checks.
## 0.3.21

- Add Seryll and Adamantine to the existing reflective material selection for
  plate/chain armour, helmets, weapons, tools, shields and other masked items.
- Retain both stock material hues: red/copper Seryll and blue Adamantine. Worn
  plate/chain uses the same rough armour profile as Gold/Silver; separate masked
  items use the same sharper item profile and configurable reflection strength.
- Extend offscreen GPU checks to all four reflective materials in stock forward
  shaders and the modern material-mask shader, including instanced drawing.

## 0.3.20

- Fix Gold/Silver metal disappearing after a weapon, tool, shield or helmet is
  equipped or drawn in a paperdoll. Forward shaders receive normal material
  opacity; the deferred reflection shader retains its private marker.
- Preserve shared primitive colours across render passes, including failures.
- Add a GPU regression probe covering all four stock forward masked shaders,
  RGB preservation, transparent texels and unaffected wooden handles.
- Add a read-only equipment/pile mask inventory. Halberd, Scythe and Open Helmet
  already have intact stock masks; their equipped visibility bug did not require
  replacement textures or masks.
- Move the working repository to `C:/projects/material_colors` and embed the
  current complete canonical updater module, with version and byte verification.

## 0.3.19

- Replace the private updater source copy with the complete canonical shared
  Chamomilo updater 1.1.0 module.
- Remove obsolete updater sources, resources, tests, UI documentation and stale
  compiled output from the product repository.
- Pin and verify the updater version, SHA-256, exact JAR entries and release ZIP
  metadata against the canonical reference build.
