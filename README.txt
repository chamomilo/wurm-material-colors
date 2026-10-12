Material Colors 0.3.28
Wurm Unlimited client mod

Metal colours for all 14 metals on equipment and worn plate/chain armour.
Gold, Silver, Adamantine, Seryll and Glimmersteel also receive environment
reflections. Default reflection strength: 30%; cave lamp compensation: 2.5x.
Requires Ago's Wurm Unlimited Client Mod Launcher. Reflections require the
modern deferred renderer; colour improvements also work with the legacy one.

INSTALLATION

1. Close the game.
2. If upgrading, remove the old armor-material-colors-*.jar and
   armor-material-colors-resources-*.jar from mods/armor-material-colors.
3. Extract this ZIP into WurmLauncher, merging the supplied mods directory.
4. Restart the client. Re-equip or drop equipment if it needs refreshing.

The ZIP contains only:
  mods/armor-material-colors.properties
  mods/armor-material-colors/armor-material-colors-0.3.28.jar
  mods/armor-material-colors/armor-material-colors-resources-0.3.28.jar
  mods/armor-material-colors/README.txt

SETTINGS

Edit mods/armor-material-colors.properties while the game is closed.
Restart the client after changing settings.

reflectionStrength=0.30         Overall reflection strength (0 to 1).
reflectionTint=0.72             Metal hue in reflected light.
caveLocalReflectionBoost=2.50   Cave lamp/torch reflections and glints (0 to 4).
sunGlintStrength=0.28           Sunlight highlights.
localGlintStrength=0.32         Local-light highlights.
glintSharpness=0.45             Highlight sharpness.

Set caveLocalReflectionBoost=0 to disable the added underground contribution.
Keep classpath and resourcePack matched to the two installed JAR filenames.
Custom models without a known metal mask keep their original rendering.

UPDATES AND DOCUMENTATION

Includes Chamomilo Updater 1.4.1 and Chamomilo UI 0.4.11.
Compatible Chamomilo mods share one Mod updates window and use the newest
compatible embedded updater. It opens release pages; installation is manual.
The updater supports English, Brazilian Portuguese, German and Russian.

Illustrated guide and source:
https://github.com/chamomilo/wurm-material-colors

Releases:
https://github.com/chamomilo/wurm-material-colors/releases/latest

LICENCE

GNU General Public License v3. The full licence is included as META-INF/LICENSE
inside the mod JAR and as LICENSE in the source repository. Embedded fonts
retain their own licence notices inside the JAR.
