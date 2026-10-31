package net.claustra01.tfcm.gametest;

import java.util.List;
import java.util.Map;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.ore.Ore;
import net.claustra01.tfcm.common.ore.OreGrade;
import net.claustra01.tfcm.common.ore.TfcOre;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMFluids;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.items.PropickItem;
import net.dries007.tfc.common.items.ProspectResult;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.QuernRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredBlock;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMOreGameTests {
    private static final String BATCH = "tfcm_foundation";

    private TFCMOreGameTests() {
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void customOreItemsAndGradesAreComplete(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(Ore.values().length == 19, "M06 must define exactly nineteen custom ores");
            require(TFCMItems.GRADED_ORES.size() == 16 && TFCMItems.ORES.size() == 3,
                    "Wrong graded or ungraded ore count");
            for (Ore ore : Ore.values()) {
                if (ore.graded()) {
                    require(TFCMItems.GRADED_ORES.get(ore).size() == 3, "Missing grade for " + ore.id());
                    for (OreGrade grade : OreGrade.values()) require(
                            BuiltInRegistries.ITEM.containsKey(id("ore/" + ore.itemName(grade))),
                            "Missing ore item " + ore.itemName(grade));
                } else require(BuiltInRegistries.ITEM.containsKey(id("ore/" + ore.id())),
                        "Missing ore item " + ore.id());
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void customOreHostsUseRockAndDimensionStrength(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (Ore ore : Ore.values()) {
                int gradeCount = ore.graded() ? OreGrade.values().length : 1;
                require(TFCMBlocks.ORES.get(ore).size() == gradeCount * (Rock.values().length + 2),
                        "Ore does not have every host variant: " + ore.id());
                for (OreGrade grade : ore.graded() ? OreGrade.values() : new OreGrade[] {null}) {
                    String name = ore.itemName(grade);
                    checkStrength(helper, TFCMBlocks.ORES.get(ore).get(name + "/netherrack").get(), 3, 10, 1);
                    checkStrength(helper, TFCMBlocks.ORES.get(ore).get(name + "/end_stone").get(), 3, 9, 2);
                    for (Rock rock : Rock.values()) {
                        Block block = TFCMBlocks.ORES.get(ore).get(name + "/" + rock.getSerializedName()).get();
                        checkStrength(helper, block, rock.category().hardness(6.5f), 10, 3);
                        require(singleDrop(helper, block).is(item("ore/" + name).getItem()),
                                "Wrong host ore drop: " + name + "/" + rock.getSerializedName());
                    }
                }
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void smallOreAndGroundcoverHaveDistinctAcquisition(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(TFCMBlocks.SMALL_ORES.size() == 16 && TFCMItems.SMALL_ORES.size() == 16,
                    "Every graded ore needs a small ore block item");
            require(TFCMBlocks.ORE_GROUNDCOVER.size() == 3 && TFCMBlocks.TFC_ORE_GROUNDCOVER.size() == 21,
                    "Ungraded ore groundcover is missing");
            BlockPos position = helper.absolutePos(new BlockPos(1, 1, 1));
            TFCMBlocks.TFC_ORE_GROUNDCOVER.forEach((ore, block) -> {
                require(!BuiltInRegistries.ITEM.containsKey(id("ore/small_" + ore)),
                        "Ungraded groundcover must not have a BlockItem");
                helper.getLevel().setBlock(position, block.get().defaultBlockState(), Block.UPDATE_ALL);
                require(singleDrop(helper, block.get()).is(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse(TfcOre.sampleDrop(ore)))),
                        "Ungraded groundcover did not yield its raw item: " + ore);
            });
            for (Ore ore : List.of(Ore.QUARTZ, Ore.FLUORITE, Ore.CAROBBIITE)) {
                require(!BuiltInRegistries.ITEM.containsKey(id("ore/small_" + ore.id())),
                        "Ungraded groundcover must not have a BlockItem");
                require(BuiltInRegistries.BLOCK.containsKey(id("ore/small_" + ore.id())),
                        "Ungraded groundcover block is missing");
                Block groundcover = TFCMBlocks.ORE_GROUNDCOVER.get(ore).get();
                helper.getLevel().setBlock(position, groundcover.defaultBlockState(), Block.UPDATE_ALL);
                require(singleDrop(helper, groundcover).is(TFCMItems.ORES.get(ore)),
                        "Ungraded groundcover did not yield its raw ore: " + ore.id());
            }
            Block smallOre = TFCMBlocks.SMALL_ORES.get(Ore.NATIVE_PLATINUM).get();
            helper.getLevel().setBlock(position, smallOre.defaultBlockState(), Block.UPDATE_ALL);
            require(singleDrop(helper, smallOre).is(TFCMItems.SMALL_ORES.get(Ore.NATIVE_PLATINUM)),
                    "Graded small ore did not yield its BlockItem");
            HeatingRecipe heating = (HeatingRecipe) recipe(helper, "heating/ore/small_native_platinum");
            ItemStack smallStack = TFCMItems.SMALL_ORES.get(Ore.NATIVE_PLATINUM).toStack();
            require(heating.matches(smallStack)
                    && heating.assembleFluid(smallStack).getFluid() == TFCMFluids.METALS
                            .get(net.claustra01.tfcm.common.metal.Metal.PLATINUM).source().get()
                    && heating.assembleFluid(smallStack).getAmount() == 10,
                    "Small Native Platinum must melt directly to 10 mB of platinum");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void quartzGemsRegisterAndPolishLikeTfcGems(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(TFCMItems.CUT_QUARTZ.isBound() && TFCMItems.CERTUS_QUARTZ.isBound(), "Quartz gems are missing");
            CraftingRecipe recipe = (CraftingRecipe) recipe(helper, "crafting/cut_quartz");
            CraftingInput input = CraftingInput.of(2, 1, List.of(tfcItem("sandpaper"), item("ore/quartz")));
            require(recipe.matches(input, helper.getLevel()), "Quartz polishing does not use TFC sandpaper");
            require(recipe.assemble(input, helper.getLevel().registryAccess()).is(TFCMItems.CUT_QUARTZ),
                    "Quartz polishing returned the wrong gem");
            NonNullList<ItemStack> remainder = recipe.getRemainingItems(input);
            require(remainder.getFirst().is(tfcItem("sandpaper").getItem())
                    && remainder.getFirst().getDamageValue() == 1,
                    "Quartz polishing did not damage TFC sandpaper once");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void quartzAndFluoriteQuernToFourTaggedPowders(GameTestHelper helper) {
        helper.succeedIf(() -> {
            TagKey<Item> gemPowders = TagKey.create(Registries.ITEM, ResourceLocation.parse("tfc:gem_powders"));
            TagKey<Item> bowlPowders = TagKey.create(Registries.ITEM, ResourceLocation.parse("tfc:bowl_powders"));
            for (Ore ore : List.of(Ore.QUARTZ, Ore.FLUORITE)) {
                QuernRecipe recipe = (QuernRecipe) recipe(helper, "quern/" + ore.id());
                ItemStack result = recipe.assemble(item("ore/" + ore.id()));
                require(result.is(TFCMItems.ORE_POWDERS.get(ore)) && result.getCount() == 4,
                        "Wrong quern result for " + ore.id());
                require(result.is(gemPowders) && result.is(bowlPowders), "Powder tags missing for " + ore.id());
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void tfcOresHaveTwoDimensionVariants(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (String ore : TfcOre.GRADED) {
                for (OreGrade grade : OreGrade.values()) checkDimensionVariants(grade.id() + "_" + ore);
            }
            for (String ore : TfcOre.UNGRADED) checkDimensionVariants(ore);
            require(TFCMBlocks.TFC_ORES.size() == (12 * 3 + 17) * 2,
                    "TFC ores must expose exactly fifty-three names in two host variants");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void chromiteExistsOnlyWithFirmaLife(GameTestHelper helper) {
        helper.succeedIf(() -> {
            boolean loaded = ModList.get().isLoaded("firmalife");
            require(TFCMBlocks.CHROMITE_ORES.size() == (loaded ? 6 : 0), "Wrong conditional Chromite block count");
            for (OreGrade grade : OreGrade.values()) {
                for (String host : List.of("netherrack", "end_stone")) {
                    String name = grade.id() + "_chromite/" + host;
                    require(BuiltInRegistries.BLOCK.containsKey(id("ore/" + name)) == loaded,
                            "Chromite registration did not follow FirmaLife presence");
                    if (loaded) require(singleDrop(helper, TFCMBlocks.CHROMITE_ORES.get(name).get()).is(
                            BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(
                                    "firmalife", "ore/" + grade.id() + "_chromite"))),
                            "Chromite dimension ore has the wrong drop: " + name);
                }
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void dimensionOresAreProspectableMineableAndDropTheirOre(GameTestHelper helper) {
        helper.succeedIf(() -> {
            TagKey<Block> ores = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores"));
            TagKey<Block> pickaxe = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/pickaxe"));
            for (Map.Entry<String, DeferredBlock<Block>> entry : allDimensionOres().entrySet()) {
                String name = entry.getKey();
                Block block = entry.getValue().get();
                require(block.defaultBlockState().is(ores) && block.defaultBlockState().is(pickaxe)
                        && block.defaultBlockState().is(TFCTags.Blocks.PROSPECTABLE)
                        && block.defaultBlockState().is(TFCTags.Blocks.MINEABLE_WITH_PROPICK),
                        "Dimension ore is not prospectable and tool-mineable: " + name);
                require(Items.DIAMOND_PICKAXE.getDefaultInstance().isCorrectToolForDrops(block.defaultBlockState())
                        && !Items.IRON_SHOVEL.getDefaultInstance().isCorrectToolForDrops(block.defaultBlockState()),
                        "Dimension ore does not require the correct mining tool: " + name);
                require(ProspectResult.FOUND.getText(block).toString()
                        .contains(block.getDescriptionId() + ".prospected"),
                        "Prospector result does not use the standard host and grade translation: " + name);
                String oreName = name.substring(0, name.indexOf('/'));
                ItemStack expectedDrop = TFCMBlocks.TFC_ORES.containsKey(name)
                        ? tfcItem("ore/" + oreName) : item("ore/" + oreName);
                require(singleDrop(helper, block).is(expectedDrop.getItem()),
                        "Dimension ore has the wrong grade or host loot: " + name);
            }
            checkProspectingAndLoot(helper, TFCMBlocks.TFC_ORES.get("normal_native_copper/end_stone").get(),
                    tfcItem("ore/normal_native_copper"));
            checkProspectingAndLoot(helper,
                    TFCMBlocks.ORES.get(Ore.NATIVE_PLATINUM).get("poor_native_platinum/netherrack").get(),
                    item("ore/poor_native_platinum"));
            checkMiningTools(helper,
                    TFCMBlocks.ORES.get(Ore.NATIVE_PLATINUM).get("rich_native_platinum/end_stone").get(),
                    item("ore/rich_native_platinum"));
        });
    }

    private static void checkDimensionVariants(String name) {
        for (String host : List.of("netherrack", "end_stone")) {
            ResourceLocation id = id("ore/" + name + "/" + host);
            require(BuiltInRegistries.BLOCK.containsKey(id), "Missing TFC dimension ore " + id);
        }
    }

    private static Map<String, DeferredBlock<Block>> allDimensionOres() {
        Map<String, DeferredBlock<Block>> ores = new java.util.LinkedHashMap<>(TFCMBlocks.TFC_ORES);
        for (Map<String, DeferredBlock<Block>> custom : TFCMBlocks.ORES.values()) {
            custom.forEach((name, block) -> {
                if (name.endsWith("/netherrack") || name.endsWith("/end_stone")) ores.put(name, block);
            });
        }
        return ores;
    }

    private static void checkProspectingAndLoot(GameTestHelper helper, Block block, ItemStack expectedDrop) {
        BlockPos position = helper.absolutePos(new BlockPos(5, 1, 1));
        helper.getLevel().setBlock(position, block.defaultBlockState(), Block.UPDATE_ALL);
        require(PropickItem.scanAreaFor(helper.getLevel(), position, 0, TFCTags.Blocks.PROSPECTABLE)
                .getInt(block) == 1, "Prospector Pick scan did not detect " + BuiltInRegistries.BLOCK.getKey(block));
        require(singleDrop(helper, block).is(expectedDrop.getItem()),
                "Dimension ore has the wrong loot: " + BuiltInRegistries.BLOCK.getKey(block));
    }

    private static void checkMiningTools(GameTestHelper helper, Block block, ItemStack expectedDrop) {
        require(Items.DIAMOND_PICKAXE.getDefaultInstance().isCorrectToolForDrops(block.defaultBlockState()),
                "Correct pickaxe cannot mine the ore");
        require(!Items.IRON_SHOVEL.getDefaultInstance().isCorrectToolForDrops(block.defaultBlockState()),
                "Incorrect shovel can mine the ore");
        require(singleDrop(helper, block).is(expectedDrop.getItem()),
                "Correct pickaxe produced the wrong ore loot");
    }

    private static void checkStrength(GameTestHelper helper, Block block, float hardness, float resistance, int x) {
        BlockPos position = helper.absolutePos(new BlockPos(x, 1, 1));
        helper.getLevel().setBlock(position, block.defaultBlockState(), 3);
        require(close(block.defaultBlockState().getDestroySpeed(helper.getLevel(), position), hardness), "Wrong hardness");
        require(close(block.getExplosionResistance(), resistance), "Wrong explosion resistance");
    }

    private static ItemStack singleDrop(GameTestHelper helper, Block block) {
        BlockPos position = helper.absolutePos(new BlockPos(3, 1, 1));
        helper.getLevel().setBlock(position, block.defaultBlockState(), Block.UPDATE_ALL);
        List<ItemStack> drops = Block.getDrops(block.defaultBlockState(), helper.getLevel(), position, null, null,
                Items.DIAMOND_PICKAXE.getDefaultInstance());
        require(drops.size() == 1 && drops.getFirst().getCount() == 1,
                "Expected exactly one drop from " + BuiltInRegistries.BLOCK.getKey(block));
        return drops.getFirst();
    }

    private static net.minecraft.world.item.crafting.Recipe<?> recipe(GameTestHelper helper, String id) {
        return helper.getLevel().getServer().getRecipeManager().byKey(id(id)).orElseThrow().value();
    }

    private static ItemStack item(String id) {
        return BuiltInRegistries.ITEM.get(TFCMOreGameTests.id(id)).getDefaultInstance();
    }

    private static ItemStack tfcItem(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tfc", id)).getDefaultInstance();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("tfcm", path);
    }

    private static boolean close(float first, float second) {
        return Math.abs(first - second) < .001f;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new GameTestAssertException(message);
    }
}
