package net.claustra01.tfcm.gametest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.material.MaterialPart;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
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
            List<ItemStack> handDrops = Block.getDrops(Blocks.SHORT_GRASS.defaultBlockState(), helper.getLevel(), grassPos,
                    null, null, ItemStack.EMPTY);
            Item knife = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tfc", "metal/knife/copper"));
            List<ItemStack> knifeDrops = Block.getDrops(Blocks.SHORT_GRASS.defaultBlockState(), helper.getLevel(), grassPos,
                    null, null, knife.getDefaultInstance());
            require(count(handDrops, MaterialPart.PLANT_FIBER) == 1, "Grass by hand did not yield one plant fiber");
            require(count(knifeDrops, MaterialPart.PLANT_FIBER) == 2, "Grass with a knife did not yield two plant fibers");

            assertKnifeCraft(helper, "bamboo_fiber", MaterialPart.BAMBOO_FIBER, 4, Items.BAMBOO.getDefaultInstance());
            assertCraft(helper, "bamboo_string", MaterialPart.BAMBOO_STRING, 1,
                    part(MaterialPart.BAMBOO_FIBER), part(MaterialPart.BAMBOO_FIBER), part(MaterialPart.BAMBOO_FIBER));

            BlockPos plantPos = helper.absolutePos(new BlockPos(2, 1, 1));
            helper.getLevel().setBlock(plantPos, TFCMBlocks.PLANT_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
            RandomSource random = RandomSource.create(1);
            for (int tick = 0; tick < 4; tick++) {
                helper.getLevel().getBlockState(plantPos).randomTick(helper.getLevel(), plantPos, random);
            }
            require(helper.getLevel().getBlockState(plantPos).is(TFCBlocks.THATCH.get()),
                    "Plant block did not dry into TFC thatch");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void bowCrossbowArrowAndToolPartsAssembleWithKnifeDamage(GameTestHelper helper) {
        helper.succeedIf(() -> {
            assertAssembly(helper, "crafting/material/bow", Items.BOW, MaterialPart.BOW_LIMB,
                    MaterialPart.BOW_LIMB, MaterialPart.BOW_CHORD, MaterialPart.BOW_GRIP);
            assertAssembly(helper, "crafting/material/crossbow", Items.CROSSBOW, MaterialPart.CROSSBOW_LIMB,
                    MaterialPart.CROSSBOW_CHORD, MaterialPart.CROSSBOW_STOCK, MaterialPart.CROSSBOW_TRIGGER);
            assertAssembly(helper, "crafting/material/arrow", Items.ARROW, MaterialPart.ARROW_TREAD,
                    MaterialPart.ARROW_FLETCH, MaterialPart.ARROW_SHAFT, MaterialPart.PLANT_STRING);
            assertKnifeCraft(helper, "leather_strip", MaterialPart.LEATHER_STRIP, 4, Items.LEATHER.getDefaultInstance());
            assertKnifeCraft(helper, "leather_string", MaterialPart.LEATHER_STRING, 2, part(MaterialPart.LEATHER_STRIP));
            assertCraft(helper, "leather_strap", MaterialPart.LEATHER_STRAP, 1,
                    part(MaterialPart.LEATHER_STRIP), part(MaterialPart.LEATHER_STRIP));
            assertCraft(helper, "plant_string", MaterialPart.PLANT_STRING, 1,
                    part(MaterialPart.PLANT_FIBER), part(MaterialPart.PLANT_FIBER), part(MaterialPart.PLANT_FIBER));
            assertCraft(helper, "plant_fabric", MaterialPart.PLANT_FABRIC, 1,
                    part(MaterialPart.PLANT_STRING), part(MaterialPart.PLANT_STRING),
                    part(MaterialPart.PLANT_STRING), part(MaterialPart.PLANT_STRING));
            assertCraft(helper, "plant_clothing", MaterialPart.PLANT_CLOTHING, 1,
                    part(MaterialPart.PLANT_FABRIC), part(MaterialPart.PLANT_FABRIC), part(MaterialPart.PLANT_FABRIC));
            assertKnifeCraft(helper, "bow_limb", MaterialPart.BOW_LIMB, 1,
                    Items.STICK.getDefaultInstance(), Items.STICK.getDefaultInstance());
            assertKnifeCraft(helper, "bow_grip", MaterialPart.BOW_GRIP, 1, part(MaterialPart.LEATHER_STRAP));
            assertKnifeCraft(helper, "crossbow_limb", MaterialPart.CROSSBOW_LIMB, 1,
                    Items.STICK.getDefaultInstance(), Items.STICK.getDefaultInstance(), Items.STICK.getDefaultInstance());
            assertKnifeCraft(helper, "crossbow_stock", MaterialPart.CROSSBOW_STOCK, 1,
                    Items.STICK.getDefaultInstance(), Items.STICK.getDefaultInstance());
            assertKnifeCraft(helper, "arrow_tread", MaterialPart.ARROW_TREAD, 2, Items.FLINT.getDefaultInstance());
            assertKnifeCraft(helper, "arrow_fletch", MaterialPart.ARROW_FLETCH, 2, Items.FEATHER.getDefaultInstance());
            assertKnifeCraft(helper, "tool_handle", MaterialPart.TOOL_HANDLE, 1,
                    Items.STICK.getDefaultInstance(), part(MaterialPart.LEATHER_STRAP));
            assertCraft(helper, "tool_binding", MaterialPart.TOOL_BINDING, 1,
                    part(MaterialPart.LEATHER_STRING), part(MaterialPart.LEATHER_STRAP));
            assertCraft(helper, "tool_riveting", MaterialPart.TOOL_RIVETING, 1,
                    BuiltInRegistries.ITEM.get(id("metal/rivet/compressed_iron")).getDefaultInstance(),
                    part(MaterialPart.LEATHER_STRAP));
            assertKnifeCraft(helper, "arrow_shaft", MaterialPart.ARROW_SHAFT, 2, Items.STICK.getDefaultInstance());
        });
    }

    private static void assertAssembly(GameTestHelper helper, String recipeId, Item output, MaterialPart... parts) {
        CraftingRecipe recipe = crafting(helper, recipeId);
        List<ItemStack> stacks = Arrays.stream(parts)
                .map(part -> TFCMItems.MATERIAL_PARTS.get(part).get().getDefaultInstance()).toList();
        int width = stacks.size() == 4 ? 2 : stacks.size();
        CraftingInput input = CraftingInput.of(width, stacks.size() / width, stacks);
        require(recipe.matches(input, helper.getLevel()), recipeId + " does not match its parts");
        require(recipe.assemble(input, helper.getLevel().registryAccess()).is(output), recipeId + " has the wrong result");
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
        stacks.add(knife);
        CraftingRecipe recipe = crafting(helper, "crafting/material/" + name);
        CraftingInput input = craftingInput(stacks);
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
