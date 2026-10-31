package net.claustra01.tfcm.gametest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.material.MaterialPart;
import net.claustra01.tfcm.common.material.PlantFiberHarvest;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMMaterialGameTests {
    private static final String BATCH = "tfcm_foundation";

    private TFCMMaterialGameTests() {
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void materialPartsHaveRegistrationNamesModelsAndSize(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (MaterialPart part : MaterialPart.values()) {
                Item item = TFCMItems.MATERIAL_PARTS.get(part).get();
                require(BuiltInRegistries.ITEM.getKey(item).equals(id(part.id())), "Wrong registration for " + part.id());
                assertSize(item.getDefaultInstance(), part.size(), part.weight(), part.id());
                require(TFCMMaterialGameTests.class.getResource("/assets/tfcm/models/item/" + part.id() + ".json") != null,
                        "Missing model for " + part.id());
                require(hasLanguageKey("item.tfcm." + part.id()), "Missing name for " + part.id());
            }
            require(BuiltInRegistries.BLOCK.getKey(TFCMBlocks.PLANT_BLOCK.get()).equals(id("plant_block")),
                    "Plant block is not registered");
            assertSize(TFCMItems.PLANT_BLOCK.get().getDefaultInstance(), "large", "medium", "plant_block");
            require(hasLanguageKey("block.tfcm.plant_block"), "Plant block name is missing");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void grassBambooAndPlantBlockProduceAndDry(GameTestHelper helper) {
        helper.succeedIf(() -> {
            BlockPos grassPos = helper.absolutePos(new BlockPos(1, 1, 1));
            Item knife = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tfc", "metal/knife/copper"));
            RandomSource random = RandomSource.create(1);
            int hand = 0;
            int knifeFibers = 0;
            for (int roll = 0; roll < 10000; roll++) {
                hand += PlantFiberHarvest.fibers(random, false);
                knifeFibers += PlantFiberHarvest.fibers(random, true);
            }
            require(hand > 10500 && hand < 11500, "Hand harvest is not 80% and 30%: " + hand);
            require(knifeFibers > 36000 && knifeFibers < 38000, "Knife harvest is not 3 at 90% and 2 at 50%: " + knifeFibers);
            Block tfcGrass = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("tfc", "plant/bluegrass"));
            require(tfcGrass.defaultBlockState().is(TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:plant/grass"))),
                    "TFC grass is not harvestable");
            helper.getLevel().setBlock(grassPos, Blocks.SHORT_GRASS.defaultBlockState(), Block.UPDATE_ALL);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, knife.getDefaultInstance());
            PlantFiberHarvest.harvest(helper.getLevel(), grassPos, player, InteractionHand.MAIN_HAND);
            require(player.getMainHandItem().getDamageValue() == 1, "The Knife was not damaged");
            require(player.getCooldowns().isOnCooldown(knife), "The harvest cooldown is missing");
            require(Block.getDrops(Blocks.SHORT_GRASS.defaultBlockState(), helper.getLevel(), grassPos, null, null,
                    knife.getDefaultInstance()).stream().noneMatch(stack -> stack.is(part(MaterialPart.PLANT_FIBER).getItem())),
                    "Breaking grass must not drop plant fiber");

            assertKnifeCraft(helper, "bamboo_fiber", MaterialPart.BAMBOO_FIBER, 4, Items.BAMBOO.getDefaultInstance());
            assertCraft(helper, "bamboo_string", MaterialPart.BAMBOO_STRING, 1,
                    part(MaterialPart.BAMBOO_FIBER), part(MaterialPart.BAMBOO_FIBER), part(MaterialPart.BAMBOO_FIBER));

            BlockPos plantPos = helper.absolutePos(new BlockPos(2, 1, 1));
            helper.getLevel().setBlock(plantPos, TFCMBlocks.PLANT_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
            int dried = 0;
            for (int tick = 0; tick < 2000; tick++) {
                helper.getLevel().setBlock(plantPos, TFCMBlocks.PLANT_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                helper.getLevel().getBlockState(plantPos).randomTick(helper.getLevel(), plantPos, random);
                if (helper.getLevel().getBlockState(plantPos).is(TFCBlocks.THATCH.get())) dried++;
            }
            require(dried > 140 && dried < 260, "Plant block must dry at 10% per random tick: " + dried);
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void bowCrossbowArrowAndToolPartsAssembleWithKnifeDamage(GameTestHelper helper) {
        helper.succeedIf(() -> {
            ItemStack empty = ItemStack.EMPTY;
            assertGrid(helper, "bow", Items.BOW, 1, 3, 3,
                    part(MaterialPart.PLANT_STRING), empty, empty,
                    empty, part(MaterialPart.BOW_LIMB), part(MaterialPart.BOW_CHORD),
                    empty, part(MaterialPart.BOW_CHORD), empty);
            ItemStack lumber = BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfc:wood/lumber/oak")).getDefaultInstance();
            assertGrid(helper, "crossbow", Items.CROSSBOW, 1, 3, 3,
                    BuiltInRegistries.ITEM.get(id("metal/ring/invar")).getDefaultInstance(), lumber, part(MaterialPart.LEATHER_STRAP),
                    lumber, part(MaterialPart.BOW_LIMB), part(MaterialPart.BOW_CHORD),
                    part(MaterialPart.LEATHER_STRAP), part(MaterialPart.BOW_CHORD), lumber);
            assertGrid(helper, "arrow", Items.ARROW, 1, 3, 3,
                    empty, part(MaterialPart.ARROW_TREAD), part(MaterialPart.ARROW_HEAD),
                    empty, part(MaterialPart.ARROW_SHAFT), empty,
                    part(MaterialPart.ARROW_FLETCH), part(MaterialPart.ARROW_TREAD), empty);
            assertGrid(helper, "plant_block", TFCMItems.PLANT_BLOCK.get(), 1, 2, 2,
                    part(MaterialPart.PLANT_FIBER), part(MaterialPart.PLANT_FIBER),
                    part(MaterialPart.PLANT_FIBER), part(MaterialPart.PLANT_FIBER));
            assertKnifeCraft(helper, "leather_sheet_knives", MaterialPart.LEATHER_SHEET, 2, Items.LEATHER.getDefaultInstance());
            assertKnifeCraft(helper, "leather_strip_knives", MaterialPart.LEATHER_STRIP, 2, part(MaterialPart.LEATHER_SHEET));
            assertKnifeCraft(helper, "leather_strap_knives", MaterialPart.LEATHER_STRAP, 2, part(MaterialPart.LEATHER_STRIP));
            assertKnifeCraft(helper, "leather_string", MaterialPart.LEATHER_STRING, 2, part(MaterialPart.LEATHER_STRAP));
            assertCraft(helper, "plant_string", MaterialPart.PLANT_STRING, 1,
                    part(MaterialPart.PLANT_FIBER), part(MaterialPart.PLANT_FIBER), part(MaterialPart.PLANT_FIBER));
            assertCraft(helper, "plant_fabric", MaterialPart.PLANT_FABRIC, 1,
                    part(MaterialPart.PLANT_STRING), part(MaterialPart.PLANT_STRING),
                    part(MaterialPart.PLANT_STRING), part(MaterialPart.PLANT_STRING));
            assertGrid(helper, "plant_clothing", TFCMItems.MATERIAL_PARTS.get(MaterialPart.PLANT_CLOTHING).get(), 1, 3, 3,
                    part(MaterialPart.PLANT_FABRIC), empty, part(MaterialPart.PLANT_FABRIC),
                    part(MaterialPart.PLANT_FABRIC), part(MaterialPart.PLANT_FABRIC), part(MaterialPart.PLANT_FABRIC),
                    part(MaterialPart.PLANT_FABRIC), part(MaterialPart.PLANT_FABRIC), part(MaterialPart.PLANT_FABRIC));
            assertKnifeCraft(helper, "handle_short", MaterialPart.SHORT_WOOD_HANDLE, 2, Items.STICK.getDefaultInstance());
            assertKnifeCraft(helper, "handle_saw", MaterialPart.SAW_WOOD_HANDLE, 2, lumber);
            assertKnifeCraft(helper, "bow_limb", MaterialPart.BOW_LIMB, 1,
                    BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfc:wood/support/oak")).getDefaultInstance());
            assertKnifeCraft(helper, "arrow_fletch", MaterialPart.ARROW_FLETCH, 2, Items.FEATHER.getDefaultInstance());
            assertKnifeCraft(helper, "arrow_shaft", MaterialPart.ARROW_SHAFT, 1, Items.STICK.getDefaultInstance());
        });
    }

    private static void assertGrid(GameTestHelper helper, String name, Item output, int count,
            int width, int height, ItemStack... stacks) {
        CraftingRecipe recipe = crafting(helper, "crafting/material/" + name);
        CraftingInput input = CraftingInput.of(width, height, List.of(stacks));
        require(recipe.matches(input, helper.getLevel()), name + " does not match the original assembly");
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        require(result.is(output) && result.getCount() == count, name + " has the wrong result");
    }

    private static void assertCraft(GameTestHelper helper, String name, MaterialPart output, int count, ItemStack... stacks) {
        CraftingRecipe recipe = crafting(helper, "crafting/material/" + name);
        CraftingInput input = craftingInput(List.of(stacks));
        require(recipe.matches(input, helper.getLevel()), name + " recipe does not match");
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        require(result.is(TFCMItems.MATERIAL_PARTS.get(output).get()) && result.getCount() == count,
                name + " recipe has the wrong result");
    }

    private static void assertKnifeCraft(GameTestHelper helper, String name, MaterialPart output, int count,
            ItemStack... ingredients) {
        ItemStack knife = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tfc", "metal/knife/copper"))
                .getDefaultInstance();
        List<ItemStack> stacks = new ArrayList<>(List.of(ingredients));
        stacks.addFirst(knife);
        CraftingRecipe recipe = crafting(helper, "crafting/material/" + name);
        boolean vertical = List.of("bamboo_fiber", "bow_limb", "arrow_fletch", "leather_sheet_knives",
                "leather_strip_knives", "leather_strap_knives", "handle_short", "handle_saw").contains(name);
        CraftingInput input = vertical ? CraftingInput.of(1, 2, stacks) : craftingInput(stacks);
        require(recipe.matches(input, helper.getLevel()), name + " recipe does not match");
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        require(result.is(TFCMItems.MATERIAL_PARTS.get(output).get()) && result.getCount() == count,
                name + " recipe has the wrong result");
        NonNullList<ItemStack> remainders = recipe.getRemainingItems(input);
        ItemStack remainder = remainders.stream().filter(stack -> stack.is(knife.getItem())).findFirst().orElseThrow();
        require(remainder.getDamageValue() == knife.getDamageValue() + 1,
                name + " did not damage the knife once");
    }

    private static CraftingInput craftingInput(List<ItemStack> stacks) {
        int width = stacks.size() == 4 ? 2 : stacks.size();
        return CraftingInput.of(width, stacks.size() / width, stacks);
    }

    private static ItemStack part(MaterialPart part) {
        return TFCMItems.MATERIAL_PARTS.get(part).get().getDefaultInstance();
    }

    private static void assertSize(ItemStack stack, String size, String weight, String name) {
        var definition = ItemSizeManager.getDefinition(stack);
        require(definition.size().getSerializedName().equals(size), "Wrong size for " + name);
        require(definition.weight().getSerializedName().equals(weight), "Wrong weight for " + name);
    }

    private static int count(List<ItemStack> stacks, MaterialPart part) {
        return stacks.stream().filter(stack -> stack.is(TFCMItems.MATERIAL_PARTS.get(part).get())).mapToInt(ItemStack::getCount).sum();
    }

    private static CraftingRecipe crafting(GameTestHelper helper, String path) {
        return (CraftingRecipe) recipe(helper, path);
    }

    private static Recipe<?> recipe(GameTestHelper helper, String path) {
        return helper.getLevel().getServer().getRecipeManager().byKey(id(path)).orElseThrow().value();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, path);
    }

    private static boolean hasLanguageKey(String key) {
        try (var input = TFCMMaterialGameTests.class.getResourceAsStream("/assets/tfcm/lang/en_us.json")) {
            return input != null && new String(input.readAllBytes(), StandardCharsets.UTF_8).contains('"' + key + '"');
        } catch (Exception exception) {
            return false;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }
}
