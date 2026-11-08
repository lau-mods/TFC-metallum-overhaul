package net.claustra01.tfcm.compat.jei;

import java.util.List;
import java.util.function.Supplier;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.processing.SluiceRecipeSync;
import net.claustra01.tfcm.worldgen.VeinDisplaySync;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@JeiPlugin
public final class TFCMJeiPlugin implements IModPlugin {
    private static final TagKey<Item> SLUICES = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("tfc", "sluices"));

    private IJeiRuntime runtime;
    private final Synced<?> sluices = new Synced<>(SluiceRecipeCategory.TYPE, SluiceRecipeSync::received);
    private final Synced<?> veins = new Synced<>(OreVeinRecipeCategory.TYPE, VeinDisplaySync::received);
    private final Synced<?> geodes = new Synced<>(GeodeRecipeCategory.TYPE, VeinDisplaySync::receivedGeodes);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new SluiceRecipeCategory(guiHelper), new OreVeinRecipeCategory(guiHelper),
                new GeodeRecipeCategory(guiHelper));
        SluiceRecipeSync.setListener(sluices::refresh);
        VeinDisplaySync.setListener(() -> {
            veins.refresh();
            geodes.refresh();
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // addons declare their Sluices through TFC's tag
        BuiltInRegistries.ITEM.getTagOrEmpty(SLUICES).forEach(
                item -> registration.addRecipeCatalyst(item.value(), SluiceRecipeCategory.TYPE));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        sluices.reset();
        veins.reset();
        geodes.reset();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    /** Server data can arrive before JEI starts or, after /reload or a new login, while it is running. */
    private final class Synced<T> {
        private final RecipeType<T> type;
        private final Supplier<List<T>> received;
        private List<T> shown = List.of();

        private Synced(RecipeType<T> type, Supplier<List<T>> received) {
            this.type = type;
            this.received = received;
        }

        private void reset() {
            shown = List.of();
            refresh();
        }

        private void refresh() {
            if (runtime == null) return;
            if (!shown.isEmpty()) runtime.getRecipeManager().hideRecipes(type, shown);
            shown = received.get();
            if (!shown.isEmpty()) runtime.getRecipeManager().addRecipes(type, shown);
        }
    }
}
