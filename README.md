TFC Metallum
=======

TFC Metallum is a NeoForge mod for Minecraft 1.21.1 that extends TerraFirmaCraft's
metallurgy and mining systems. It uses the `tfcm` mod ID and requires Java 21,
NeoForge 21.1.265, TerraFirmaCraft 4.2.12, and Patchouli 1.21.1-93-NEOFORGE.
FirmaLife 3.0.15 is an optional integration.

Build the mod with `./gradlew build`. The distributable JAR is written to `build/libs`.

## Magnox

In a TFC crucible, mix 70–80% molten magnesium with 20–30% molten aluminum. The 75:25 ratio matches Immersive Technology. Magnox uses tier 3 forging and melts at 650 °C.

Cast the molten alloy into ingots as usual. The ingots can be worked into the standard and extended metal forms, including sheets for plated building blocks. Molten Magnox can also be stored in a bucket.

## Immersive Geology ores

TFCM adds its own graded blocks and surface samples for fourteen metallic ores found in Immersive Geology. Their processed concentrates melt into existing TFC or TFCM metals. Secondary metals are recovered through ore washing. Chromite byproducts require FirmaLife.

The ores use the existing YAML vein loader. Registered ore blocks use `tfcm:ore/{tier}_<ore>/{rock}` and surface samples use `tfcm:ore/small_<ore>`. Veins are opt-in through the pack's YAML configuration.

## Ad Astra metals

Craft an Ad Astra Desh, Ostrum or Calorite ingot into the corresponding TFCM ingot at a 1:1 ratio. Their forging tiers are 4, 5 and 6, and their melting temperatures are 1200, 1400 and 1550 °C, respectively. Ad Astra supplies its own ore generation and processing.
