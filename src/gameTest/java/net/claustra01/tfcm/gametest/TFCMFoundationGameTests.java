package net.claustra01.tfcm.gametest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.claustra01.tfcm.TFCM;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMFoundationGameTests {
    private static final String BATCH = "tfcm_foundation";

    static {
        StructureUtils.testStructuresDir = System.getProperty("tfcm.gametest.structures");
    }

    private TFCMFoundationGameTests() {
    }

    @BeforeBatch(batch = BATCH)
    public static void configureReport(net.minecraft.server.level.ServerLevel level) throws Exception {
        Path report = Path.of(System.getProperty("tfcm.gametest.report"));
        Files.createDirectories(report.getParent());
        GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(report.toFile()));
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void standaloneEnvironmentLoads(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(helper.getLevel() != null, "The GameTest world was not created");
            require(ModList.get().isLoaded(TFCM.MOD_ID), "TFCM was not loaded");
            require(version("tfc").equals("4.2.12"), "TFC 4.2.12 was not loaded");
            require(ModList.get().isLoaded("patchouli"), "Patchouli was not loaded");
            require(helper.getLevel().getServer().getRecipeManager().getRecipes().stream()
                    .anyMatch(recipe -> recipe.id().getNamespace().equals("tfc")), "TFC recipes were not loaded");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void firmaLifeEnvironmentMatchesRun(GameTestHelper helper) {
        helper.succeedIf(() -> {
            boolean expected = Boolean.getBoolean("tfcm.gametest.firmalife");
            boolean loaded = ModList.get().isLoaded("firmalife");
            require(loaded == expected, "FirmaLife presence did not match the GameTest environment");
            ResourceLocation chromite = ResourceLocation.fromNamespaceAndPath("firmalife", "ore/normal_chromite");
            ResourceLocation chromiteHeating = ResourceLocation.fromNamespaceAndPath(
                    "firmalife", "heating/ore/normal_chromite");
            require(BuiltInRegistries.ITEM.containsKey(chromite) == expected,
                    "FirmaLife Chromite registration did not match the GameTest environment");
            require(helper.getLevel().getServer().getRecipeManager().getRecipes().stream()
                    .anyMatch(recipe -> recipe.id().equals(chromiteHeating)) == expected,
                    "FirmaLife Chromite processing did not match the GameTest environment");
            if (expected) {
                require(version("firmalife").equals("3.0.15"), "FirmaLife 3.0.15 was not loaded");
                require(ModList.get().isLoaded(TFCM.MOD_ID), "TFCM did not load with FirmaLife");
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void dependencyMetadataRejectsUnsupportedEnvironments(GameTestHelper helper) {
        helper.succeedIf(() -> {
            try (var stream = TFCM.class.getResourceAsStream("/META-INF/neoforge.mods.toml")) {
                require(stream != null, "The generated mod metadata is missing");
                String metadata = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                require(metadata.contains("loaderVersion=\"[4,)\""), "The supported mod loader range is missing");
                require(metadata.contains("versionRange=\"[21.1.265,)\""), "The supported NeoForge range is missing");
                require(metadata.contains("modId=\"tfc\"\n    type=\"required\""), "TFC is not a required dependency");
                require(metadata.contains("versionRange=\"[4.2.12,)\""), "The supported TFC range is missing");
                require(metadata.contains("modId=\"patchouli\"\n    type=\"required\""), "Patchouli is not a required dependency");
                require(metadata.contains("versionRange=\"[1.21.1-93-NEOFORGE,)\""), "The supported Patchouli range is missing");
                require(metadata.contains("modId=\"firmalife\"\n    type=\"optional\""), "FirmaLife is not optional");
                require(metadata.contains("versionRange=\"[3.0.15,)\""), "The supported FirmaLife range is missing");
                require(metadata.contains("versionRange=\"[1.21.1]\""), "Minecraft is not restricted to 1.21.1");
                require(metadata.contains("javaVersion=\"[21,22)\""), "Java is not restricted to version 21");
                require(!metadata.contains("modId=\"examplemod\""), "A legacy examplemod alias remains");
            } catch (Exception exception) {
                throw new GameTestAssertException("Could not inspect generated mod metadata: " + exception.getMessage());
            }
        });
    }

    private static String version(String modId) {
        return ModList.get().getModContainerById(modId).orElseThrow().getModInfo().getVersion().toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }
}
