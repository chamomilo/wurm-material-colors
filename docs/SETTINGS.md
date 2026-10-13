# Material Colors settings

Open **Main menu > Mod updates > Material Colors > Settings**. The shared
editor provides hover help, validation and automatic saving. Reflection controls
apply during play. Changing the resource pack requires restarting the client;
the editor shows a notice and offers Wurm's normal quit confirmation.

| Property | Default | Allowed range | Effect |
| --- | ---: | --- | --- |
| `reflectionStrength` | 0.30 | 0–1 | Environment reflection strength. |
| `sunGlintStrength` | 0.28 | 0–1 | Direct sunlight highlights. |
| `localGlintStrength` | 0.32 | 0–1 | Local-light highlights. |
| `caveLocalReflectionBoost` | 2.50 | 0–4 | Additional underground local-light compensation. |
| `glintSharpness` | 0.45 | 0–1 | Highlight concentration. |
| `reflectionTint` | 0.72 | 0–1 | Metal hue in reflected light. |
| `frontReflectionDetail` | 0.00 | 0–1 | Additional forward-facing detail. |
| `environmentBrightness` | 1.00 | 0.25–2 | Reflected surroundings brightness. |
| `ssrDetail` | 1.00 | 0–1.5 | Detail in reflections of visible objects. |
| `reflectionContrast` | 1.00 | 0.5–2 | Reflected light contrast. |
| `skyBrightness` | 1.00 | 0.25–2 | Sky contribution. |
| `groundBrightness` | 1.00 | 0.25–2 | Ground contribution. |
| `caveBrightness` | 1.00 | 0.25–2 | Underground surroundings brightness. |
| `sunDiscStrength` | 1.50 | 0–3 | Reflected sun disc strength. |
| `ssrRayDistance` | 1.00 | 0.5–2 | Screen reflection distance multiplier. |
| `ssrThickness` | 1.00 | 0.5–3 | Screen reflection surface tolerance. |
| `backdropContrast` | 1.12 | 0.5–2 | Environment backdrop contrast. |
| `resourcePack` | Version-matched JAR path | Text | Material masks and shaders; restart required. |

Editor overrides are stored in
`<user-home>/.chamomilo/mod-settings/armor-material-colors.properties` and take
precedence over the installation's `mods/armor-material-colors.properties`.
The editor does not rewrite the installation descriptor. Remove an override
from the saved file while the client is closed to restore its installation
default. Changing `classpath` or other loader metadata still requires editing
the installation descriptor while the game is closed.

The shared language selector controls the registry and settings editor.
Startup resource paths using the standard versioned name are updated to the
current release automatically; explicitly custom paths remain unchanged.
