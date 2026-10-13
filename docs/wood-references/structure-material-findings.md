# Wood species in house structures

Verified against the installed Wurm Unlimited client and server on 2026-10-13.

**Stock house structures do not retain the species of the wood consumed during
construction.** They store a structural material category, not an item material
such as `oakenwood` or `pinewood`. This applies to walls, wooden floors and
ceilings, wooden roof sections, and wooden staircases.

| Element | Server representation | Persisted material | Client representation |
| --- | --- | --- | --- |
| Wall, window, doorway and similar wall sections | `Wall` / `DbWall` | `StructureMaterialEnum`, including `WOOD`, `TIMBER_FRAMED`, `LOG`, `CRUDE_WOOD` | `WallData` / `HouseWallData`, structural type code |
| Wooden floor or ceiling | `Floor` / `DbFloor` | `StructureConstants.FloorMaterial.WOOD` | `FloorData` / `HouseFloorData`, `FloorMaterial` |
| Wooden roof section | `Floor` / `DbFloor` with a roof `FloorType` | `FloorMaterial.WOOD` | `RoofData` / `HouseRoofData`, `FloorMaterial` |
| Wooden staircase | `Floor` / `DbFloor` with a staircase `FloorType` | `FloorMaterial.WOOD` | Floor/structure data with `FloorMaterial` |

## Evidence

The server's `Wall.material` field is a `StructureMaterialEnum`. Its complete
enum has structural categories and no tree-species entries. `Floor.material`
is a `StructureConstants.FloorMaterial`, which likewise has a single `WOOD`
category and no birch, pine, oak or other wood-species entries.

`DbWall.save()` binds `getMaterial().material` to the `WALLS.MATERIAL` column.
`DbFloor.save()` binds `getMaterial().getCode()` to `FLOORS.MATERIAL`.
The INSERT and UPDATE statements contain no separate wood-species column.
Neither the `Wall` nor the `Floor` object has a separate species field.

`Communicator.sendAddWall()` sends the structural type from
`Wall.getType().value`, plus `Wall.getMaterialString()`. The latter returns
`"wood"` for a wooden wall; it does not return `"oakenwood"`, `"pinewood"`, etc.
Wall paint is sent separately as optional RGB bytes from `Wall.getColor()`.
That colour is not a retained construction-wood material ID.

`Communicator.sendAddFloor()` sends the type, the structural material byte from
`FloorMaterial.getCode()`, state, position, layer and direction. The same
floor/roof representation covers roofs. No item material byte or wood-species
name is transmitted. The client fields and constructors accept those same
structural enums, without a separate wood-species value.

## Consequence for Material Colors

A client-only mod cannot recover the original wood species of an existing
stock house. It can apply a chosen common tint to a structural category, but
that would be a visual override rather than colouring by actual construction
wood. Supporting actual species would require a server feature that records
the chosen species and communicates it to the client. Existing structures
would require an explicit assignment or a reliable external history.

This finding concerns the installed stock code. A custom server mod could keep
additional metadata outside those stock fields; that was not tested against a
remote server. Item-based objects such as wagon, furniture, tools and shields
follow a different material system and are the subjects of the sampler.

## Local source files

- `WurmServerLauncher/server.jar`: `Wall`, `Floor`, `DbWall`, `DbFloor`,
  `RoofFloorEnum`, `Communicator`.
- `WurmLauncher/common.jar`: `StructureMaterialEnum`,
  `StructureConstants.FloorMaterial` and `StructureConstants.FloorType`.
- `WurmLauncher/client-patched.jar`: `WallData`, `FloorData`, `RoofData`,
  `HouseWallData`, `HouseFloorData`, `HouseRoofData`.

The files were inspected with JDK 8 `javap -p -c -constants`; no game files,
server database, running client or running server were changed.
