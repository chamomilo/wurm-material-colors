Material Colors 0.5.3
Wurm Unlimited client mod

Material finishes for all 26 stock woods and 14 metals, with reflections on
Gold, Silver, Adamantine, Seryll and Glimmersteel equipment.

This release includes the fix for disappearing world objects, the current
Chamomilo Updater 1.5.0, and all 18 settings in its shared editor.

FEATURES

- Colours for supported held and dropped equipment, shields, helmets, worn
  plate/chain armour, wooden furniture, containers, parts, piles and vehicles.
- Missing wood masks restored on supported models, including PMK wagon frames.
- Separate fabric, metal fittings, painted details, contents and existing masks.
- Default reflection strength of 30%; cave local-light compensation of 2.5x.

Reflections require the modern deferred renderer. Material colours also work
with the legacy renderer. House structures do not retain their wood species.
Replacement models with different textures may need additional masks.

INSTALL OR UPGRADE

Requires Ago's Wurm Unlimited Client Mod Launcher.

1. Close Wurm Unlimited completely.
2. When upgrading, remove previous armor-material-colors-*.jar and
   armor-material-colors-resources-*.jar files from
   WurmLauncher/mods/armor-material-colors.
3. Extract this ZIP into WurmLauncher, merge the supplied mods directory and
   replace the Material Colors descriptor.
4. Start the client again.

A full restart is required after upgrading from a version that stopped loading
objects. The corrected model loader starts with the new client session.

This ZIP contains exactly four files:
  mods/armor-material-colors.properties
  mods/armor-material-colors/armor-material-colors-0.5.3.jar
  mods/armor-material-colors/armor-material-colors-resources-0.5.3.jar
  mods/armor-material-colors/README.txt

The updater and its interface resources are embedded in the main JAR.

SETTINGS IN THE SHARED UPDATER

Open Main menu > Mod updates, find Material Colors, and press Settings.

Chamomilo Updater 1.5.0 supplies all 18 controls with hover help, validation and
automatic saving. Reflection changes apply during play. Changing the resource
pack requires a restart and displays a restart notice.

Controls and defaults:
  reflectionStrength=0.30          Environment reflection strength
  sunGlintStrength=0.28            Direct sunlight highlights
  localGlintStrength=0.32          Lamp and torch highlights
  caveLocalReflectionBoost=2.50    Extra underground compensation; 0 disables it
  glintSharpness=0.45              Highlight concentration
  reflectionTint=0.72             Metal colour in reflected light
  frontReflectionDetail=0.00       Additional front-facing detail
  environmentBrightness=1.00      Reflected surroundings brightness
  ssrDetail=1.00                  Visible-scene reflection detail
  reflectionContrast=1.00         Reflected light contrast
  skyBrightness=1.00              Sky contribution
  groundBrightness=1.00           Ground contribution
  caveBrightness=1.00             Cave surroundings contribution
  sunDiscStrength=1.50            Reflected sun disc
  ssrRayDistance=1.00             Screen reflection distance
  ssrThickness=1.00               Screen reflection surface tolerance
  backdropContrast=1.12           Environment backdrop contrast
  resourcePack                    Version-matched material resource JAR

Saved overrides are stored in:
  <user-home>/.chamomilo/mod-settings/armor-material-colors.properties

They take precedence over the installation descriptor. Standard versioned
resource-pack paths advance automatically during upgrades; custom paths remain
selected. Loader metadata such as classpath is edited in the installation
properties only while the game is closed.

Compatible Chamomilo mods share one registry and select the newest compatible
embedded updater. Its language selector supports English, Brazilian Portuguese,
German and Russian. It opens release pages for manual installation.

ILLUSTRATED GUIDE AND SETTINGS REFERENCE

https://github.com/chamomilo/wurm-material-colors
https://github.com/chamomilo/wurm-material-colors/blob/main/docs/SETTINGS.md

RELEASES

https://github.com/chamomilo/wurm-material-colors/releases/latest

LICENCE

GNU General Public License v3. The full licence is included as META-INF/LICENSE
inside the main mod JAR and as LICENSE in the source repository. Embedded fonts
retain their own licence notices.
