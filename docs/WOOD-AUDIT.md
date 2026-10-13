# Wood model coverage

Material Colors 0.5.1 supports all 26 stock item wood materials, using the exact
RGB multipliers from Wurm's stock `itemcolors.xml`.

The resource pack supplies 145 DDS masks through 454 exact model/atlas rules.
An existing global or matching mesh mask takes precedence. The rules cover
413 model paths; 297 had a missing mask in an audited mesh or replacement-pack
variant. The other 116 retain their existing masks.

Affected families include containers, furniture, racks, flags and poles,
vehicles, tools, resource piles, wooden parts, unfinished models and selected
decorative structures. PMK wagon models lack the stock properties that assign
the frame mask even though the material remains in their model name.

Fabric, metal components, stored equipment, vegetation and other non-wood
regions are excluded where present. Stock pre-coloured plank atlases use one
neutral baseline before receiving the species tint. Wood receives no metal
reflection marker.

## Model inventory

- [Complete list of 297 repaired model paths](wood-audit/added-models.md)
- [370 added main-LOD mesh masks and their source packs](wood-audit/added-meshes.csv)
- [All exact model/atlas/mask assignments](wood-audit/mask-rules.csv)
- [Mask provenance and SHA-256 hashes](wood-audit/mask-provenance.json)
- [All 26 wood colours](wood-references/wood-sampler-all.png)

## Verification

The asset inventory loaded 1,546 stock WOM models and 61 additional pack models.
Offline verification sampled actual main-LOD triangle UVs with stock diffuse
textures and production mask repairs: four points per triangle, up to 4,096
triangles per mesh, using bilinear filtering and repeating UVs.

Of 476 pack/model variants checked, all 458 with visible wood produced 26
distinct 8-bit colour results. 310 variants gained those results through the
repairs. 370 main-LOD meshes were repaired across 322 pack/model variants and
296 unique paths. The additional repaired path, the alchemy cupboard, needed
drawer LOD masks while retaining its existing main-mesh mask.

No missing textures or colour-verification failures were found. GPU checks
covered all 26 woods in four legacy modes, modern regular and instanced
rendering, and primary paint, alongside opacity and metal-reflection checks.
These checks use offline assets and rendering; they do not replace gameplay
verification on a running server.

## Compatibility limits

The remaining 18 sampled variants contain stone, marble, slate, cloth, leather
or no visible wood in the relevant preview. Covered and painted surfaces can
hide species differences. Unknown future replacement packs need a separate
mask review.

House walls, floors, roofs and stairs store structural categories rather than
wood species. A client mod cannot recover their construction species. See
[house material details](wood-references/structure-material-findings.md).
