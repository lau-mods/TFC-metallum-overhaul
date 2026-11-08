package net.claustra01.tfcm.compat.jei;

import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.worldgen.GeodeDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** One YAML geode: the clusters grown inside it and where it generates. */
final class GeodeRecipeCategory implements IRecipeCategory<GeodeDisplay> {
    static final RecipeType<GeodeDisplay> TYPE = RecipeType.create(TFCM.MOD_ID, "geode", GeodeDisplay.class);
    private static final int SLOT_Y = 12;
    // as in the ore vein category; more clusters than fit are not laid out
    private static final int CELL_WIDTH = 20;
    private static final int INFO_Y = 34;
    private static final int INFO_COLUMN_WIDTH = 85;
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_COLOR = 0xFF404040;

    private final IDrawable icon;

    GeodeRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(Items.AMETHYST_CLUSTER.getDefaultInstance());
    }

    @Override
    public RecipeType<GeodeDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tfcm.jei.geode");
    }

    @Override
    public int getWidth() {
        return 2 * INFO_COLUMN_WIDTH;
    }

    @Override
    public int getHeight() {
        return INFO_Y + 2 * LINE_HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GeodeDisplay geode, IFocusGroup focuses) {
        for (int index = 0; index < geode.clusters().size(); index++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 1 + index * CELL_WIDTH, SLOT_Y)
                    .addItemStack(geode.clusters().get(index)).setStandardSlotBackground();
        }
    }

    @Override
    public void draw(GeodeDisplay geode, IRecipeSlotsView view, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        String name = geode.clusters().stream().map(GeodeRecipeCategory::mineral).distinct()
                .collect(Collectors.joining(", "));
        graphics.drawString(font, font.plainSubstrByWidth(name, getWidth()), 0, 0, TEXT_COLOR, false);
        List<Component> info = List.of(
                Component.translatable("tfcm.jei.ore_vein.dimension." + geode.dimension()),
                Component.translatable("tfcm.jei.ore_vein.y", geode.minY(), geode.maxY()),
                Component.translatable("tfcm.jei.ore_vein.rarity", geode.rarity()));
        for (int index = 0; index < info.size(); index++) {
            graphics.drawString(font, font.plainSubstrByWidth(info.get(index).getString(), INFO_COLUMN_WIDTH - 2),
                    index % 2 * INFO_COLUMN_WIDTH, INFO_Y + 1 + index / 2 * LINE_HEIGHT, TEXT_COLOR, false);
        }
    }

    /** The mineral a cluster is made of, such as Amethyst; the cluster's own name if no mineral name is given. */
    private static String mineral(ItemStack cluster) {
        String key = cluster.getDescriptionId() + ".mineral";
        return I18n.exists(key) ? I18n.get(key) : cluster.getHoverName().getString();
    }
}
