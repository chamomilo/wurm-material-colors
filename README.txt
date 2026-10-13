Material Colors 0.5.1
Wurm Unlimited client mod

WHAT IT DOES

Distinct finishes for all 26 stock wood materials and all 14 metals.
Restores missing wood masks on supported furniture, containers, racks, parts,
tools, piles and vehicles, including PMK wagon frames. Fabric, metal parts,
contents and existing custom masks retain their separate surfaces.
House structures do not retain their original construction wood species.

Gold, Silver, Adamantine, Seryll and Glimmersteel receive environment reflections.
Default reflection strength: 30%; underground local-light compensation: 2.5x.
Reflections require the modern deferred renderer. Material colours also work
with the legacy renderer. Unknown replacement packs may need additional masks.

INSTALLATION

Requires Ago's Wurm Unlimited Client Mod Launcher.

1. Close the game.
2. When upgrading, remove the old armor-material-colors-*.jar and
   armor-material-colors-resources-*.jar from mods/armor-material-colors.
3. Extract this ZIP into WurmLauncher, merging the supplied mods directory.
4. Restart the client. Re-equip or drop equipment if its model needs refreshing.

The ZIP contains exactly:
  mods/armor-material-colors.properties
  mods/armor-material-colors/armor-material-colors-0.5.1.jar
  mods/armor-material-colors/armor-material-colors-resources-0.5.1.jar
  mods/armor-material-colors/README.txt

SETTINGS AND UPDATES

Open Main menu > Mod updates, find Material Colors, and press Settings.
Chamomilo Updater 1.5.0 and Chamomilo UI 0.4.11 are embedded in the mod.
All 18 settings have hover help. Valid changes are saved automatically.
Reflection tuning applies immediately; resource-pack changes require a restart.
The settings editor shows a notice for changes requiring a restart.

Key defaults:
  reflectionStrength=0.30
  reflectionTint=0.72
  caveLocalReflectionBoost=2.50
  sunGlintStrength=0.28
  localGlintStrength=0.32
  glintSharpness=0.45

Set caveLocalReflectionBoost=0 to disable extra underground compensation.
Keep classpath and resourcePack matched to the installed JAR names.
Saved editor values take precedence over the installation properties.

Compatible Chamomilo mods share one Mods Registry and select the newest
compatible embedded updater. It opens release pages; installation is manual.
The shared language selector supports English, Brazilian Portuguese, German
and Russian.

ILLUSTRATED GUIDE AND SOURCE

https://github.com/chamomilo/wurm-material-colors

RELEASES

https://github.com/chamomilo/wurm-material-colors/releases/latest

LICENCE

GNU General Public License v3. The full licence is included as META-INF/LICENSE
inside the mod JAR and as LICENSE in the source repository. Embedded fonts
retain their licence notices inside the JAR.
