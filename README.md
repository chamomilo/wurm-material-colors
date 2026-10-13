# Material Colors

**Material finishes for Wurm Unlimited: 26 woods, 14 metals, and reflections on premium metal equipment.**

[Download 0.5.3](https://github.com/chamomilo/wurm-material-colors/releases/tag/v0.5.3) · [All settings](docs/SETTINGS.md) · [Release history](CHANGELOG.md)

![Gold plate armour and sword in Wurm Unlimited](docs/material-colors.png)

Material Colors gives supported items the appearance of the material they are made from. It covers held and dropped equipment, worn plate and chain armour, wooden furniture, containers, parts, piles and vehicles.

Version **0.5.3** includes the fix for disappearing world objects, the current **Chamomilo Updater 1.5.0**, and all **18 settings** in its shared settings editor.

## Wood finishes

All 26 stock wood materials have their own colour, including shrub woods. Supported models receive missing wood masks where needed, including the wooden frame of PMK wagons. Fabric, metal fittings, painted details and stored equipment keep their own surfaces. Existing stock and custom masks take precedence.

| Birch | Oak | Walnut |
| --- | --- | --- |
| ![Birch wagon](docs/wood-birchwood.png) | ![Oak wagon](docs/wood-oakenwood.png) | ![Walnut wagon](docs/wood-walnut.png) |

| Cherry | Thorn | Lingonberry |
| --- | --- | --- |
| ![Cherry wagon](docs/wood-cherrywood.png) | ![Thorn wagon](docs/wood-thorn.png) | ![Lingonberry wagon](docs/wood-lingonberrywood.png) |

The wood gallery uses original game models and textures under the same studio lighting. These are material previews. House walls, floors, roofs and stairs do not retain their original wood species, so they cannot receive species-specific colours. Replacement models with different textures may need additional masks.

## Metal finishes

The palette covers Iron, Steel, Zinc, Tin, Lead, Copper, Bronze, Brass, Electrum, Gold, Silver, Adamantine, Seryll and Glimmersteel. Supported shields, helmets, ground items, held equipment and worn plate/chain armour use the same material palette.

![Iron, Steel, Zinc, Tin and Lead finishes](docs/grey-metals.png)

![Copper, Bronze, Brass and Electrum finishes](docs/warm-metals.png)

![Gold, Silver, Adamantine, Seryll and Glimmersteel finishes](docs/premium-metals.png)

Neutral armour textures keep the material colour from being applied twice. Wooden handles, leather and transparent cutouts retain their appearance.

## Reflections and cave lighting

**Gold, Silver, Adamantine, Seryll and Glimmersteel** reflect their surroundings. Worn armour uses a softer reflection profile. Default reflection strength is **30%**; underground, local-light reflections and glints receive **2.5x** compensation.

![Local-light reflection comparison on Glimmersteel plate armour in a cave](docs/cave-armour.png)

The metal gallery and cave comparison are controlled renders of original game assets. Reflections vary with lighting, the viewing angle and the visible scene. They require the modern deferred renderer; material colours also work with the legacy renderer.

## Install or upgrade

Requires **Ago's Wurm Unlimited Client Mod Launcher**.

1. Download `armor-material-colors-0.5.3.zip` from the [release page](https://github.com/chamomilo/wurm-material-colors/releases/tag/v0.5.3).
2. Close Wurm Unlimited completely.
3. When upgrading, remove the previous `armor-material-colors-*.jar` and `armor-material-colors-resources-*.jar` from `WurmLauncher/mods/armor-material-colors`.
4. Extract the ZIP into **WurmLauncher**, merging the supplied `mods` directory and replacing the Material Colors descriptor.
5. Start the client again.

A full restart is required after upgrading from a version that stopped loading objects. The corrected model loader starts with the new client session.

The installation ZIP contains exactly four files:

```text
mods/armor-material-colors.properties
mods/armor-material-colors/armor-material-colors-0.5.3.jar
mods/armor-material-colors/armor-material-colors-resources-0.5.3.jar
mods/armor-material-colors/README.txt
```

The updater and its interface resources are embedded in the main JAR. The illustrated guide and gallery are available in this repository. Licence notices are inside the JAR.

## Settings in the shared updater

Open **Main menu → Mod updates**, find **Material Colors**, and press **Settings**.

Chamomilo Updater **1.5.0** provides a scrollable editor for all **18 Material Colors controls**, with explanatory hover help, value validation and automatic saving. Reflection changes apply during play. Changing the resource pack requires a restart; the editor displays a notice and offers Wurm's normal quit confirmation.

| Control | Default | What it changes |
| --- | ---: | --- |
| Reflection strength | 0.30 | Amount of environment reflection on premium metals. |
| Sun glint strength | 0.28 | Highlights from direct sunlight. |
| Local glint strength | 0.32 | Highlights from lamps and torches. |
| Cave local reflection boost | 2.50 | Extra underground compensation; set to 0 to disable it. |
| Glint sharpness | 0.45 | Concentration of metal highlights. |
| Reflection tint | 0.72 | Amount of metal colour in reflected light. |

The editor also includes reflected-environment brightness, screen reflection detail, contrast, sky/ground/cave contributions, the sun disc, reflection distance and thickness, backdrop contrast, front reflection detail, and the material resource-pack path. See the [complete settings reference](docs/SETTINGS.md) for all defaults and allowed ranges.

Saved overrides are stored in `<user-home>/.chamomilo/mod-settings/armor-material-colors.properties`. They take precedence over the installation descriptor. Standard versioned resource-pack paths advance automatically during upgrades; custom paths remain selected.

Compatible Chamomilo mods share one registry and use the newest compatible embedded updater. Its language selector supports English, Brazilian Portuguese, German and Russian. The updater opens release pages for manual installation.

## Build from source

Use **JDK 8**, Gradle and the client libraries `client-patched.jar`, `common.jar`, `javassist.jar` and `modlauncher.jar`. Set `WURM_CLIENT_LIBS` or pass `-PwurmClientLibDir=/path/to/libs`. Configure the canonical updater reference with `CHAMOMILO_UPDATER_REFERENCE` or `-PchamomiloUpdaterReference=/path/to/updater`. Supply `-PwurmJavaFxJar=/path/to/jfxrt.jar` for its native UI verification when needed.

```text
gradlew clean build dist
```

The build tests the mod and shared updater, verifies the embedded module against the canonical reference, checks wood-rule loading through Wurm's real shared loader, and validates the four-file ZIP in `build/distributions`. Missing or damaged wood rules are also tested to ensure model loading remains available.

## Licence

[GNU General Public License v3](LICENSE). Embedded fonts retain their own licence notices. Source code is available under the matching release tag.
