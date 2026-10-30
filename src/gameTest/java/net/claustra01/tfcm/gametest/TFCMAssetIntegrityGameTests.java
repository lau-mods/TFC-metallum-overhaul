package net.claustra01.tfcm.gametest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.metal.MetalForm;
import net.claustra01.tfcm.common.material.MaterialPart;
import net.claustra01.tfcm.common.ore.Ore;
import net.claustra01.tfcm.common.processing.ProcessingForm;
import net.claustra01.tfcm.common.processing.ProcessingSeries;
import net.claustra01.tfcm.common.tool.ToolMetal;
import net.claustra01.tfcm.common.tool.ToolPart;
import net.claustra01.tfcm.common.tool.area.AreaToolMetal;
import net.claustra01.tfcm.common.tool.area.AreaToolType;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMAssetIntegrityGameTests {
    private static final String BATCH = "tfcm_foundation";

    private TFCMAssetIntegrityGameTests() {
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void registeredContentHasModelsTranslationsAndBlockLoot(GameTestHelper helper) {
        JsonObject language = readJson("assets/tfcm/lang/en_us.json");
        checkItems(language);
        checkBlocks(language);
        checkFluids(language);
        checkShield("invar", "texture", "layer0");
        checkShield("titanium", "1", "2");
        checkShield("netherite", "texture", "particle");
        checkShield("tungsten_steel", "0", "particle");
        helper.getLevel().getServer().getResourceManager().listResources("tags", id -> id.getPath().endsWith(".json"))
                .forEach(TFCMAssetIntegrityGameTests::checkTag);
        helper.succeed();
    }

    private static void checkItems(JsonObject language) {
        Set<ResourceLocation> items = new HashSet<>();
        BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getNamespace().equals(TFCM.MOD_ID)).forEach(id -> {
            require(items.add(id), "Duplicate item " + id);
            checkModel(ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, "item/" + id.getPath()));
            Item item = BuiltInRegistries.ITEM.get(id);
            require(language.has(item.getDescriptionId()), "Item translation " + id);
        });
    }

    private static void checkBlocks(JsonObject language) {
        BuiltInRegistries.BLOCK.keySet().stream().filter(id -> id.getNamespace().equals(TFCM.MOD_ID)).forEach(id -> {
            String statePath = "assets/tfcm/blockstates/" + id.getPath() + ".json";
            checkBlockstateModels(readJson(statePath));
            Block block = BuiltInRegistries.BLOCK.get(id);
            require(language.has(block.getDescriptionId()), "Block translation " + id);
            if (!(block instanceof LiquidBlock)) {
                ResourceLocation loot = block.getLootTable().location();
                if (!loot.equals(ResourceLocation.parse("minecraft:empty")))
                    checkLoot(readJson("data/" + loot.getNamespace() + "/loot_table/" + loot.getPath() + ".json"));
            }
        });
    }

    private static void checkFluids(JsonObject language) {
        BuiltInRegistries.FLUID.keySet().stream().filter(id -> id.getNamespace().equals(TFCM.MOD_ID)).forEach(id -> {
            Fluid fluid = BuiltInRegistries.FLUID.get(id);
            Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            require(blockId.getNamespace().equals(TFCM.MOD_ID), "Fluid block " + id);
            checkBlockstateModels(readJson("assets/tfcm/blockstates/" + blockId.getPath() + ".json"));
            require(language.has(block.getDescriptionId()), "Fluid translation " + id);
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void metalsOresAndToolsHaveTfcAttributes(GameTestHelper helper) {
        checkMetalShapes();
        checkMoltenFluids();
        checkMaterialsAndProcessing();
        checkEquipment();
        checkAreaTools();
        checkOreTags();
        helper.succeed();
    }

    private static void checkMetalShapes() {
        Set<String> metalShapes = new HashSet<>();
        for (Metal metal : Metal.values()) for (MetalForm form : MetalForm.values()) {
            require(metalShapes.add(form.item(metal)), "Duplicate metal shape " + form.item(metal));
            ItemStack stack = BuiltInRegistries.ITEM.get(ResourceLocation.parse(form.item(metal))).getDefaultInstance();
            require(!stack.isEmpty(), "Metal form " + form.item(metal));
            require(HeatCapability.getDefinition(stack) != null, "Metal heat " + form.item(metal));
            require(ItemSizeManager.getDefinition(stack) != null, "Metal size " + form.item(metal));
            if (form.isItem()) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM,
                        ResourceLocation.parse("c:" + form.tag() + "/" + metal.id()));
                require(stack.is(tag), "Metal form tag " + form.item(metal));
            }
        }
    }

    private static void checkMoltenFluids() {
        for (Metal metal : Metal.values()) {
            ResourceLocation still = ResourceLocation.parse("tfcm:metal/" + metal.id());
            ResourceLocation flowing = ResourceLocation.parse("tfcm:metal/flowing_" + metal.id());
            TagKey<Fluid> molten = TagKey.create(Registries.FLUID,
                    ResourceLocation.parse("c:molten_" + metal.id()));
            require(BuiltInRegistries.FLUID.get(still).is(molten)
                    && BuiltInRegistries.FLUID.get(flowing).is(molten), "Molten fluid tag " + metal.id());
            require(resourceExists("data/tfcm/tfc/fluid_heat/" + metal.id() + ".json"),
                    "Molten fluid heat " + metal.id());
        }
    }

    private static void checkMaterialsAndProcessing() {
        for (MaterialPart part : MaterialPart.values()) {
            ResourceLocation id = ResourceLocation.parse("tfcm:" + part.id());
            require(ItemSizeManager.getDefinition(BuiltInRegistries.ITEM.get(id).getDefaultInstance()) != null,
                    "Material size " + id);
        }
        for (Ore ore : Ore.values()) {
            ResourceLocation id = ResourceLocation.parse("tfcm:ore/small_" + ore.id());
            if (BuiltInRegistries.ITEM.containsKey(id))
                require(HeatCapability.getDefinition(BuiltInRegistries.ITEM.get(id).getDefaultInstance()) != null,
                        "Small ore heat " + id);
        }
        for (ProcessingSeries series : ProcessingSeries.values()) for (ProcessingForm form : ProcessingForm.values()) {
            if (form != ProcessingForm.POWDER && form != ProcessingForm.PELLET && form != ProcessingForm.BRIQUET) continue;
            ResourceLocation id = ResourceLocation.parse("tfcm:ore_processing/" + form.id() + "/" + series.id());
            if (BuiltInRegistries.ITEM.containsKey(id) && series.recoveryFluid() != null
                    && (form == ProcessingForm.POWDER || !series.nonMetal()))
                require(HeatCapability.getDefinition(BuiltInRegistries.ITEM.get(id).getDefaultInstance()) != null,
                        "Processing heat " + id);
        }
    }

    private static void checkEquipment() {
        TagKey<Item> rack = TagKey.create(Registries.ITEM, ResourceLocation.parse("tfc:usable_on_tool_rack"));
        for (ToolMetal metal : ToolMetal.values()) for (ToolPart part : ToolPart.values()) {
            ItemStack stack = TFCMItems.METAL_ITEMS.get(metal).get(part).toStack();
            require(HeatCapability.getDefinition(stack) != null, "Tool heat " + part.id());
            require(ItemSizeManager.getDefinition(stack) != null, "Tool size " + part.id());
            if (part.isFinishedTool()) require(stack.is(rack), "Tool rack " + part.id());
        }
    }

    private static void checkAreaTools() {
        TagKey<Item> rack = TagKey.create(Registries.ITEM, ResourceLocation.parse("tfc:usable_on_tool_rack"));
        TagKey<Item> tools = TagKey.create(Registries.ITEM, ResourceLocation.parse("c:tools/mining_hammer"));
        TagKey<Item> mining = TagKey.create(Registries.ITEM, ResourceLocation.parse("c:tools/mining_tool"));
        for (AreaToolMetal metal : AreaToolMetal.values()) for (AreaToolType type : AreaToolType.values()) {
            ItemStack stack = TFCMItems.AREA_TOOLS.get(metal).get(type).toStack();
            ItemStack head = TFCMItems.AREA_TOOL_HEADS.get(metal).get(type).toStack();
            ItemStack ingot = BuiltInRegistries.ITEM.get(ResourceLocation.parse(metal.namespace()
                    + ":metal/ingot/" + metal.id())).getDefaultInstance();
            require(stack.is(rack), "Area tool rack " + type.id());
            if (type == AreaToolType.MINING_HAMMER) require(stack.is(tools), "Mining tool category");
            if (type == AreaToolType.EXCAVATOR) require(stack.is(ItemTags.SHOVELS), "Excavator category");
            else require(stack.is(ItemTags.PICKAXES) && stack.is(mining), "Hammer category " + type.id());
            require(HeatCapability.getDefinition(stack) != null && HeatCapability.getDefinition(head) != null,
                    "Area tool heat " + type.id());
            require(ItemSizeManager.getDefinition(stack) != null && ItemSizeManager.getDefinition(head) != null,
                    "Area tool size " + type.id());
            require(ItemSizeManager.getDefinition(stack).size() == Size.VERY_LARGE
                    && ItemSizeManager.getDefinition(stack).weight() == Weight.VERY_HEAVY
                    && ItemSizeManager.getDefinition(head).size() == Size.LARGE
                    && ItemSizeManager.getDefinition(head).weight() == Weight.MEDIUM,
                    "Area tool storage " + type.id());
            require(metal.repairIngredient().test(ingot), "Area tool repair " + type.id());
            HeatCapability.setTemperature(stack, metal.forgingTemperature());
            require(Math.abs(HeatCapability.getTemperature(stack) - metal.forgingTemperature()) < 0.01f,
                    "Area tool heating " + type.id());
        }
    }

    private static void checkOreTags() {
        TagKey<Block> ores = TagKey.create(Registries.BLOCK,
                ResourceLocation.parse("c:ores"));
        BuiltInRegistries.BLOCK.keySet().stream()
                .filter(id -> id.getNamespace().equals(TFCM.MOD_ID) && id.getPath().startsWith("ore/")
                        && !id.getPath().startsWith("ore/small_"))
                .forEach(id -> {
                    var block = BuiltInRegistries.BLOCK.get(id);
                    require(block.defaultBlockState().is(ores), "Ore tag " + id);
                    require(block.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE), "Ore pickaxe " + id);
                });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void neutroniumTexturesHaveAnimationFrames(GameTestHelper helper) {
        for (String form : new String[] {"ingot", "double_ingot", "sheet", "double_sheet", "rod", "foil",
                "gear", "heavy_sheet", "nail", "ring", "rivet", "screw", "stamen", "wire"}) {
            checkAnimation("item/metal/" + form + "/neutronium");
        }
        checkAnimation("block/metal/block/neutronium");
        helper.succeed();
    }

    private static void checkAnimation(String texture) {
        String path = "assets/tfcm/textures/" + texture + ".png";
        JsonObject metadata = readJson(path + ".mcmeta");
        require(metadata.has("animation") && metadata.getAsJsonObject("animation").get("frametime").getAsInt() > 0,
                "Animation metadata " + texture);
        try (var stream = TFCMAssetIntegrityGameTests.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "Animation texture " + texture);
            var image = ImageIO.read(stream);
            require(image != null && image.getWidth() == 16 && image.getHeight() > image.getWidth()
                    && image.getHeight() % image.getWidth() == 0, "Animation frames " + texture);
            boolean changed = false;
            for (int y = 0; y < image.getWidth() && !changed; y++) for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != image.getRGB(x, y + image.getWidth())) {
                    changed = true;
                    break;
                }
            }
            require(changed, "Identical animation frames " + texture);
        } catch (java.io.IOException exception) {
            throw new GameTestAssertException("Cannot read " + path);
        }
    }

    private static JsonObject readJson(String path) {
        try (var stream = TFCMAssetIntegrityGameTests.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "Resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException exception) {
            throw new GameTestAssertException("Cannot read " + path);
        }
    }

    private static void checkBlockstateModels(JsonElement element) {
        if (element.isJsonObject()) {
            for (var entry : element.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals("model")) {
                    ResourceLocation model = ResourceLocation.parse(entry.getValue().getAsString());
                    if (model.getNamespace().equals(TFCM.MOD_ID)) checkModel(model);
                }
                else checkBlockstateModels(entry.getValue());
            }
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(TFCMAssetIntegrityGameTests::checkBlockstateModels);
        }
    }

    private static void checkLoot(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("type") && object.get("type").getAsString().equals("minecraft:item") && object.has("name")) {
                ResourceLocation item = ResourceLocation.parse(object.get("name").getAsString());
                require(BuiltInRegistries.ITEM.containsKey(item), "Loot item " + item);
            }
            object.entrySet().forEach(entry -> checkLoot(entry.getValue()));
        } else if (element.isJsonArray()) element.getAsJsonArray().forEach(TFCMAssetIntegrityGameTests::checkLoot);
    }

    private static void checkShield(String metal, String first, String second) {
        JsonObject model = readJson("assets/tfcm/models/item/metal/shield/" + metal + ".json");
        JsonObject textures = model.getAsJsonObject("textures");
        require(textures.has(first) && textures.has(second), "Shield texture keys " + metal);
        for (var entry : textures.entrySet()) {
            ResourceLocation texture = ResourceLocation.parse(entry.getValue().getAsString());
            require(resourceExists("assets/" + texture.getNamespace() + "/textures/" + texture.getPath() + ".png"),
                    "Shield texture " + texture);
        }
    }

    private static void checkTag(ResourceLocation file, Resource resource) {
        String path = file.getPath();
        if (!path.startsWith("tags/item/") && !path.startsWith("tags/block/") && !path.startsWith("tags/fluid/")) return;
        try (var reader = resource.openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (!json.has("values")) return;
            for (JsonElement value : json.getAsJsonArray("values")) {
                String name = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
                if (name.startsWith("#")) continue;
                ResourceLocation id = ResourceLocation.parse(name);
                if (!id.getNamespace().equals(TFCM.MOD_ID)) continue;
                boolean exists = path.startsWith("tags/item/") ? BuiltInRegistries.ITEM.containsKey(id)
                        : path.startsWith("tags/block/") ? BuiltInRegistries.BLOCK.containsKey(id)
                        : BuiltInRegistries.FLUID.containsKey(id);
                boolean required = !value.isJsonObject() || !value.getAsJsonObject().has("required")
                        || value.getAsJsonObject().get("required").getAsBoolean();
                require(exists || !required, "Tag " + file + " references " + id);
            }
        } catch (IOException exception) {
            throw new GameTestAssertException("Cannot read tag " + file);
        }
    }

    private static void checkModel(ResourceLocation id) {
        String path = "assets/" + id.getNamespace() + "/models/" + id.getPath() + ".json";
        JsonObject model = readJson(path);
        if (model.has("parent")) {
            ResourceLocation parent = ResourceLocation.parse(model.get("parent").getAsString());
            if (parent.getNamespace().equals(TFCM.MOD_ID)) checkModel(parent);
            else if (parent.getNamespace().equals("tfc")) require(resourceExists("assets/tfc/models/" + parent.getPath() + ".json"),
                    "Model parent " + parent);
        }
        if (model.has("textures")) for (var entry : model.getAsJsonObject("textures").entrySet()) {
            String name = entry.getValue().getAsString();
            if (name.startsWith("#")) continue;
            ResourceLocation location = ResourceLocation.parse(name);
            if (location.getNamespace().equals(TFCM.MOD_ID) || location.getNamespace().equals("tfc"))
                require(resourceExists("assets/" + location.getNamespace() + "/textures/" + location.getPath() + ".png"),
                        "Texture " + location);
        }
    }

    private static boolean resourceExists(String path) {
        return TFCMAssetIntegrityGameTests.class.getClassLoader().getResource(path) != null;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }
}
