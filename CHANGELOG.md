# Changelog

## 0.5.1 — 2026-10-13

- Ship the current canonical Chamomilo Updater 1.5.0 with the shared Mods Registry Settings editor and all 18 Material Colors controls.
- Keep resource-pack defaults tied to the product version and verify live settings and saved overrides through the shared provider.
- Synchronize migrated saved resource-pack paths with the Settings editor while preserving custom packs.
- Replace the illustrated guide, add the wood gallery and document settings persistence.
- Remove temporary asset-research scripts, duplicate previews, obsolete updater checks and unused resource mapping output.
- Package only the descriptor, two JARs and README.txt; retain gallery images and technical model lists in the repository.

## 0.5.0 — 2026-10-13

- Apply all 26 stock wood materials consistently to world items, equipped items, piles and vehicles.
- Restore reviewed wood masks on unmasked furniture, containers, parts, unfinished models and audited replacement packs, including PMK wagons.
- Preserve stock and custom masks, fabric, metal components, stored equipment and painted details.
- Use a shared neutral plank atlas before applying the species tint; retain the existing metal palette and reflections.
- Include an asset audit and offline geometry/colour verification; house structures still have no retained wood-species data.
- Upgrade saved default resource-pack paths to the current version while preserving custom paths.
- Embed the current canonical Chamomilo Updater 1.5.0 and Chamomilo UI 0.4.11.

## 0.4.0 вЂ” 2026-10-13

- Add the shared registry Settings editor with every user-configurable property, hover help and immediate application where supported.
- Warn immediately after a saved startup-only change and offer the standard Wurm quit confirmation after settings are saved.

## 0.3.28 вЂ” 2026-10-12

- Embed Chamomilo Updater 1.4.1 and Chamomilo UI 0.4.11, including automatic
  selection of the newest compatible updater among active installed mods.
- Rewrite the illustrated guide with all 14 metal finishes and a cave-lighting
  comparison. Provide a concise installation and settings guide in README.txt.
- Package only the descriptor, two mod JARs and README.txt. Include the GPL
  licence inside the main JAR; keep gallery images in the source repository.

## 0.3.27 вЂ” 2026-10-12

- Compensate for absent solar reflections underground by multiplying premium
  metal's local lamp/torch reflections and glints by 2.5. Keep the overall
  reflection strength at 30% and ordinary lighting intact.
- Light the bounded reflected wall sample with each real local source instead
  of using only the cave's ambient light. Preserve metal hue and the softer
  armour profile; absent lamps, unlit sampled walls and background cannot glow.
- Refresh compensation when the player enters or leaves the cave layer. Add
  `caveLocalReflectionBoost` (0вЂ“4, default 2.50); surface lights are unaffected.
- Verify ordinary, batched and instanced local-light shaders on the GPU.
- Embed the current canonical Updater 1.3.3 and Chamomilo UI 0.4.11.

## 0.3.26 вЂ” 2026-10-12

- Darken lead's displayed finish by 15% while retaining its reference hue, so
  lead items and worn armour are visibly darker than iron under the same light.
- Add Glimmersteel to premium environment reflections across held items,
  helmets, shields, dropped equipment and worn Plate/Chain armour. Use the
  existing item and rough-armour reflection profiles with full configured strength.
- Reduce the default reflection strength from 80% to 30% for all five premium
  metals, both in the shipped configuration and when the setting is omitted.
- Extend reflection eligibility and opacity checks to all five premium metals,
  including Glimmersteel in ordinary and instanced GPU rendering.

## 0.3.25 вЂ” 2026-10-12

- Convert High Res Icons' linear-light metal multipliers to Wurm's encoded
  texture colour space before passing them to world and worn-armour shaders.
  Copper now retains the reference's orange tone instead of losing too much
  green and blue; the same conversion applies consistently to all 14 metals.
- Keep the original reference palette, material masks, opacity and the four
  existing premium reflection profiles intact.
- Verify actual forward-shader and worn-compositor pixels against the icons'
  independent linear-light calculation, including a copper regression sample.

## 0.3.24 вЂ” 2026-10-11

- Use one palette from High Res Icons for all 14 metals across held equipment,
  worn armour, helmets, shields, dropped items and supported equipment piles.
  Include iron even when the stock item-colour lookup returns no colour.
- Retain reflections only for Adamantine, Seryll, Gold and Silver; all other
  metals receive colour without an environment-reflection marker.
- Compose worn Plate/Chain from neutral stock textures so material-specific
  Gold, Copper, Steel, Iron and fantasy layers are not tinted twice. Remove
  the extra precoloured Adamantine/Glimmersteel texture from equipped coifs.
- Resolve the two differently named neutral Chain arm masks in both texture
  preloading and composition, so colour and premium reflections cover the arms.
- Restore absent per-mesh masks for equipment piles using their matching stock
  diffuse atlas, including multi-texture helmet and metal-shield piles.
- Extend unit and offscreen GPU checks to all 14 palettes, ordinary and instanced
  world rendering, worn composition, cutouts and unmasked handles.
- Embed the complete canonical Chamomilo Updater 1.3.1 and verify its exact
  entries and bytes in the mod JAR and installable ZIP.

## 0.3.23 вЂ” 2026-10-10

- Embed canonical Chamomilo Updater 1.2.7 and its complete Chamomilo UI 0.4.10
  dependency, with exact version, entry and byte verification.
- Rewrite the GitHub README and generate its plain-text README.txt for releases.
- Remove Markdown documentation and the project screenshot from installable
  ZIPs. Keep the screenshot in the GitHub README and verify the five-file ZIP.
- Remove research notes, imported release records, old ZIPs and the one-off mask
  audit script from the project folder.

## 0.3.22 вЂ” 2026-10-10

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
- Embed the complete shared updater module, with version and byte verification.

## 0.3.19

- Replace the private updater source copy with the complete canonical shared
  Chamomilo updater 1.1.0 module.
- Remove obsolete updater sources, resources, tests, UI documentation and stale
  compiled output from the product repository.
- Pin and verify the updater version, SHA-256, exact JAR entries and release ZIP
  metadata against the canonical reference build.
