package net.claustra01.tfcm.compat.jei;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.worldgen.VeinDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** One YAML vein: its ore names, the rock it replaces, the weighted ores it places there, its indicator and shape. */
final class OreVeinRecipeCategory implements IRecipeCategory<VeinDisplay> {
    static final RecipeType<VeinDisplay> TYPE = RecipeType.create(TFCM.MOD_ID, "ore_vein", VeinDisplay.class);
    private static final int ORE_X = 45;
    private static final int SLOT_Y = 12;
    // six ores fit in one row; veins with more are not laid out
    private static final int CELL_WIDTH = 20;
    private static final int INFO_X = 24;
    private static final int INFO_Y = 41;
    private static final int INFO_COLUMN_WIDTH = 73;
    private static final int INFO_ROWS = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_COLOR = 0xFF404040;

    private final IDrawable icon;
    private final IDrawableStatic arrow;

    OreVeinRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("tfc", "metal/propick/wrought_iron")).getDefaultInstance());
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<VeinDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tfcm.jei.ore_vein");
    }

    @Override
    public int getWidth() {
        return INFO_X + 2 * INFO_COLUMN_WIDTH;
    }

    @Override
    public int getHeight() {
        return INFO_Y + INFO_ROWS * LINE_HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VeinDisplay vein, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, SLOT_Y).addItemStacks(vein.rocks()).setStandardSlotBackground()
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("tfcm.jei.ore_vein.rock")));
        for (int index = 0; index < vein.ores().size(); index++) {
            VeinDisplay.Ore ore = vein.ores().get(index);
            builder.addSlot(RecipeIngredientRole.OUTPUT, oreX(index), SLOT_Y).addItemStacks(ore.stacks())
                    .setStandardSlotBackground()
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                            "tfcm.jei.ore_vein.chance", SluiceRecipeCategory.percent(ore.chance()))));
        }
        vein.indicator().filter(indicator -> !indicator.stacks().isEmpty()).ifPresent(indicator -> builder
                .addSlot(RecipeIngredientRole.OUTPUT, 1, INFO_Y).addItemStacks(indicator.stacks())
                .setStandardSlotBackground()
                .addRichTooltipCallback((view, tooltip) -> {
                    tooltip.add(Component.translatable("tfcm.jei.ore_vein.indicator"));
                    tooltip.add(indicator.rarity() > 0
                            ? Component.translatable("tfcm.jei.ore_vein.indicator.surface",
                                    indicator.rarity(), indicator.depth())
                            : Component.translatable("tfcm.jei.ore_vein.indicator.no_surface"));
                    if (indicator.undergroundCount() > 0) tooltip.add(Component.translatable(
                            "tfcm.jei.ore_vein.indicator.underground",
                            indicator.undergroundCount(), indicator.undergroundRarity()));
                }));
    }

    @Override
    public void draw(VeinDisplay vein, IRecipeSlotsView view, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, font.plainSubstrByWidth(name(vein), getWidth()), 0, 0, TEXT_COLOR, false);
        arrow.draw(graphics, 21, SLOT_Y);
        graphics.pose().pushPose();
        graphics.pose().scale(.5f, .5f, 1);
        for (int index = 0; index < vein.ores().size(); index++) {
            String text = SluiceRecipeCategory.percent(vein.ores().get(index).chance()) + "%";
            graphics.drawString(font, text, (oreX(index) + 8) * 2 - font.width(text) / 2,
                    (SLOT_Y + 19) * 2, 0xFF555555, false);
        }
        graphics.pose().popPose();
        List<Component> info = info(vein);
        for (int index = 0; index < info.size(); index++) {
            var line = font.substrByWidth(info.get(index), INFO_COLUMN_WIDTH - 2);
            graphics.drawString(font, Language.getInstance().getVisualOrder(line),
                    INFO_X + index % 2 * INFO_COLUMN_WIDTH, INFO_Y + 1 + index / 2 * LINE_HEIGHT, TEXT_COLOR, false);
        }
    }

    /** The ore names without grade or rock, as the Prospector's Pick reports them; plain block names otherwise. */
    private static String name(VeinDisplay vein) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (VeinDisplay.Ore ore : vein.ores()) {
            String key = ore.stacks().getFirst().getDescriptionId();
            names.add(I18n.get(I18n.exists(key + ".prospected") ? key + ".prospected" : key));
        }
        return String.join(", ", names);
    }

    private static List<Component> info(VeinDisplay vein) {
        String type = ResourceLocation.parse(vein.type()).getPath();
        List<Component> info = new ArrayList<>();
        info.add(Component.translatable("tfcm.jei.ore_vein.dimension." + vein.dimension()));
        info.add(Component.translatable("tfcm.jei.ore_vein.type." + type));
        info.add(Component.translatable("tfcm.jei.ore_vein.y", vein.minY(), vein.maxY()));
        info.add(Component.translatable("tfcm.jei.ore_vein.rarity", vein.rarity()));
        info.add(Component.translatable("tfcm.jei.ore_vein.density", SluiceRecipeCategory.percent(vein.density())));
        // the shape values each TFC vein feature actually reads
        if (!type.equals("pipe_vein")) info.add(Component.translatable("tfcm.jei.ore_vein.size", vein.size()));
        if (!type.equals("cluster_vein")) info.add(Component.translatable("tfcm.jei.ore_vein.height", vein.height()));
        if (type.equals("pipe_vein")) info.add(Component.translatable("tfcm.jei.ore_vein.radius", vein.radius()));
        return info;
    }

    private static int oreX(int index) {
        return ORE_X + index * CELL_WIDTH;
    }
}
