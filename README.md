# Material Colors 0.3.19

![Material Colors in Wurm Unlimited](docs/material-colors.png)

Small Wurm Unlimited client mod that improves material colours for worn plate
and chain armour, weapons, and tools. Gold and Silver items that use Wurm's
material mask also receive 80% environment reflection in the modern renderer.

The mod keeps the vanilla models, textures, masks, and material hues. It makes
sure that the material colour is present in the player armour texture-builder
map before the client composites the final player texture.

For `model.weapon.*` and `model.tool.*`, it also normalises the stock metal RGB
filter so the darkest channel ratios are retained but the brightest channel is
not needlessly dimmed. This makes gold, copper, bronze, seryll, adamantine, and
other supported metals visibly distinct on models that have a material mask.
Wood materials and iron are left unchanged. Models without a material mask are
also left untouched so wooden handles are never recoloured accidentally. The
stock standalone Plate and Chain models are the exception: the game ships them
without any material mask even though the meshes are entirely metal, so this
pack supplies a full-metal mask for those two armour families and their piles.
Wurm normally derives model properties only from the JAR that owns the WOM;
the mod therefore substitutes just its synthetic armour-mask filename inside
the Collada texture-preload method after each stock Plate/Chain WOM has loaded.

The reflection shader uses a short screen-space ray to pick up nearby visible
objects, with a time-of-day-aware sky/ground fallback when the reflected area is
outside the screen. Gold also tints its reflected light instead of becoming
silver-grey. Reflection is restricted to the green material-mask channel,
so handles and other non-metal parts retain the stock rendering. The default
strength can be adjusted with `reflectionStrength` in the mod properties.
In caves, screen-space samples are unlit G-buffer albedo; the shader attenuates
them with cave irradiance rather than treating pale rock textures as light.
Actual lantern and torch brightness is then added by the local-light pass.

Reflective Gold/Silver Plate and Chain use a separate armour surface profile.
A stable UV-space hammered micro-normal breaks the single broad mirror image,
softens direct-light glints, and slightly lowers shininess without reducing the
configured reflection amount. Screen-space fallback reflections are filtered
over neighbouring pixels on armour only. Gold/Silver weapons, tools, and other
masked items retain the sharper reflection profile; all other materials remain
non-reflective.

The reflection mask stored in the composited player texture uses a nearly
opaque `254/255` alpha marker. This keeps paperdolls and portrait avatars opaque
while the modern 3D skin shader can still recover the armour-only mask.

Reflection colour/brightness, SSR reach and thickness, and separate
sunlight/local-light glints can be adjusted in
`mods/armor-material-colors.properties`. Values are loaded at client startup.

This shader extension targets Wurm's modern deferred renderer. The legacy
renderer keeps the colour improvements but does not run the reflection pass.

## Installation

1. Install Ago's Wurm Unlimited Client Mod Launcher.
2. Extract the distribution archive into `WurmLauncher` so that the supplied
   `mods` directory is merged with the existing one.
3. Restart the client and re-equip or drop a Gold/Silver item.

Normal operation is silent in the client log. Only shader/resource loading
failures and invalid configuration values are reported.

## Updates

Version 0.3.19 embeds the canonical Chamomilo updater 1.1.0. All compatible
Chamomilo mods share one update check and one window available from Wurm's
`Mod updates` menu. The updater only opens the GitHub release page; ZIP
installation remains manual.
