# Material Colors

Metal colours and reflections for **Wurm Unlimited**. This client mod gives
weapons, tools, shields and armour a consistent finish based on their metal.

**[Download 0.3.28](https://github.com/chamomilo/wurm-material-colors/releases/latest)**
· Includes Chamomilo Updater **1.4.1** and Chamomilo UI **0.4.11**.

![Gold armour and sword in Wurm Unlimited](docs/material-colors.png)

## What it changes

- One palette for all 14 metals, based on High Res Icons, across held equipment,
  worn plate and chain armour, helmets, shields and supported ground items/piles.
- Orange copper, distinct cool grey lead and consistent colours on neutral
  armour textures, without applying the metal tint twice.
- Environment reflections on **Gold, Silver, Adamantine, Seryll and Glimmersteel**,
  with a softer finish on armour and sharper highlights on separate items.
- Stronger lamp and torch reflections underground, where sunlight is absent.
  The default reflection strength is **30%**, with **2.5×** cave compensation.
- Wooden handles, leather, dyes and transparent cutouts keep their appearance.

Reflections require the **modern deferred renderer**. Colour improvements also
work with the legacy renderer. Screen-space reflections depend on the visible
scene, lighting and viewing angle, with a sky/ground fallback.
Custom models and pile textures without a known metal mask keep their original
rendering.

## Metal gallery

The following previews use original Wurm models and textures under identical
studio lighting. They were rendered with the 0.3.26 palette, which is retained
in 0.3.28; they show colour rather than in-game environment reflections.

![Iron, Steel, Zinc, Tin and Lead on axes, helmets and shields](docs/grey-metals.png)

![Copper, Bronze, Brass and Electrum on axes, helmets and shields](docs/warm-metals.png)

![Gold, Silver, Adamantine, Seryll and Glimmersteel on axes, helmets and shields](docs/premium-metals.png)

## Reflections underground

Local lights illuminate reflected cave walls and add metal highlights. The
comparison below shows the cave lighting improvement introduced in 0.3.27 and
included in 0.3.28, at the same 30% reflection strength. This is a controlled
offscreen render using stock game geometry and textures.

![Glimmersteel plate jacket: cave lamp reflections before and after compensation](docs/cave-armour.png)

## Installation

Requires **Ago's Wurm Unlimited Client Mod Launcher**.

1. Download `armor-material-colors-0.3.28.zip` from the
   [latest release](https://github.com/chamomilo/wurm-material-colors/releases/latest).
2. Close the game.
3. If upgrading, remove the old `armor-material-colors-*.jar` and
   `armor-material-colors-resources-*.jar` from `mods/armor-material-colors`.
4. Extract the ZIP into **WurmLauncher**, merging its `mods` directory with yours.
5. Restart the client. Re-equip or drop affected equipment if it needs refreshing.

The ZIP contains exactly four files:

```text
mods/
  armor-material-colors.properties
  armor-material-colors/
    armor-material-colors-0.3.28.jar
    armor-material-colors-resources-0.3.28.jar
    README.txt
```

The illustrated README and gallery stay in this repository. Licence notices
are included inside the mod JAR.

## Settings

Edit `mods/armor-material-colors.properties` while the client is closed, then
restart it. The most useful settings are:

| Setting | Default | Effect |
| --- | --- | --- |
| `reflectionStrength` | `0.30` | Overall reflection strength, from 0 to 1. |
| `reflectionTint` | `0.72` | How strongly the metal hue colours reflected light. |
| `caveLocalReflectionBoost` | `2.50` | Underground lamp/torch reflection and glint multiplier, from 0 to 4. |
| `sunGlintStrength` | `0.28` | Direct sunlight highlights. |
| `localGlintStrength` | `0.32` | Highlights from local lights. |
| `glintSharpness` | `0.45` | Highlight sharpness. |

`caveLocalReflectionBoost=0` disables the extra underground reflection/glint
contribution. Surface lights retain their normal strength.

Additional controls adjust environment brightness, reflection contrast,
sky/ground/cave brightness and screen-space reflection detail, distance and
thickness. Keep `classpath` and `resourcePack` matched to the installed JAR names.

## Shared updater

Chamomilo mods share one **Mod updates** window. Updater 1.4.1 selects the newest
compatible embedded updater among active installed mods, opens GitHub release
pages and supports English, Brazilian Portuguese, German and Russian.
Updates are installed manually. Its language selector also applies the chosen
language to installed mods that support it.

## Build from source

Use **JDK 8**, the Gradle wrapper and the Wurm client libraries
`client-patched.jar`, `common.jar`, `javassist.jar` and `modlauncher.jar`.
Set `WURM_CLIENT_LIBS` or pass `-PwurmClientLibDir=/path/to/libs`.
Supply the canonical Chamomilo updater reference through
`CHAMOMILO_UPDATER_REFERENCE` or `-PchamomiloUpdaterReference=/path/to/updater`.
If required, pass `-PwurmJavaFxJar=/path/to/jfxrt.jar` for its UI verification.

```text
gradlew clean build dist
```

The build runs the mod and updater checks, verifies the exact embedded updater
entries and bytes, and writes the installable ZIP to `build/distributions`.
An optional `verifyMaterialOpacityGpu` task checks shader rendering using an
offscreen context; pass `-PwurmNativeLibDir=/path/to/nativelibs` if needed.

## Licence

[GNU General Public License v3](LICENSE). The complete source for this release
is available in this repository under the matching release tag.
