package net.claustra01.tfcm.gametest;

import java.nio.file.Files;
import java.nio.file.Path;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.config.TFCMConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("tfcm_m12_fresh")
@PrefixGameTestTemplate(false)
public final class TFCMFreshConfigGameTests {
    private static final String BATCH = "tfcm_fresh_config";

    static {
        StructureUtils.testStructuresDir = System.getProperty("tfcm.gametest.structures");
    }

    private TFCMFreshConfigGameTests() {
    }

    @BeforeBatch(batch = BATCH)
    public static void configureReport(net.minecraft.server.level.ServerLevel level) throws Exception {
        String reportPath = System.getProperty("tfcm.gametest.freshConfigReport");
        if (reportPath == null) return;
        Path report = Path.of(reportPath);
        Files.createDirectories(report.getParent());
        GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(report.toFile()));
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void generatedConfigListsEveryAcceptedIdWithoutDisablingContent(GameTestHelper helper) {
        helper.succeedIf(() -> {
            try {
                String config = Files.readString(FMLPaths.CONFIGDIR.get().resolve("tfcm-common.toml"));
                require(config.contains("Accepted metal IDs: " + String.join(", ", TFCMConfig.metalIds())),
                        "The complete metal ID comment is missing");
                require(config.contains("Accepted ore IDs: " + String.join(", ", TFCMConfig.oreIds())),
                        "The complete ore ID comment is missing");
                require(config.contains("disabledMetals = []") && config.contains("disabledOres = []"),
                        "A generated comment changed the config values");
                for (String name : java.util.List.of("overworld", "nether", "end")) {
                    require(Files.isRegularFile(FMLPaths.CONFIGDIR.get().resolve("tfcm/" + name + ".yaml")),
                            "The default worldgen file was not created: " + name);
                }
                require(TFCMConfig.isMetalEnabled(Metal.LEAD) && TFCMConfig.isOreEnabled("galena"),
                        "A generated comment disabled content");
                require(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse("tfcm:metal/ingot/lead"))
                        && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse("tfcm:ore/normal_galena")),
                        "Fresh config did not preserve content");
            } catch (Exception exception) {
                throw new GameTestAssertException("Could not inspect Common Config: " + exception.getMessage());
            }
        });
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }
}
