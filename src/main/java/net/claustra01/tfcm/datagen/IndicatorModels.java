package net.claustra01.tfcm.datagen;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;

/**
 * Surface sample (indicator) models. Each ore is given a shape matching its crystal habit; the faces are cut from
 * the ore's item texture like TFC's samples, with every UV fitted onto opaque pixels of that texture.
 */
final class IndicatorModels {
    private record Box(int x1, int y1, int z1, int x2, int y2, int z2, char axis, float angle) {
        float cx() {
            return (x1 + x2) / 2f;
        }

        float cz() {
            return (z1 + z2) / 2f;
        }
    }

    enum Shape {
        NUGGETS_A(b(5, 0, 6, 8, 2, 9), b(6, 2, 7, 7, 3, 8), b(8, 0, 8, 10, 1, 10), b(9, 0, 5, 11, 2, 7),
                b(6, 0, 10, 7, 1, 11), b(4, 0, 4, 5, 1, 5)),
        NUGGETS_B(b(6, 0, 6, 10, 1, 9), b(7, 1, 7, 9, 2, 8), r(7, 0, 9, 9, 1, 12, 'y', 22.5f),
                b(10, 0, 5, 12, 2, 7), b(4, 0, 8, 6, 1, 9), b(8, 0, 4, 9, 1, 6)),
        CHUNK_A(b(5, 0, 5, 10, 2, 10), b(6, 2, 6, 9, 3, 8), b(10, 0, 7, 12, 1, 9), b(4, 0, 9, 6, 1, 11)),
        CHUNK_B(b(4, 0, 6, 8, 2, 10), b(5, 2, 7, 7, 3, 9), b(9, 0, 5, 12, 2, 8), b(9, 0, 9, 11, 1, 11)),
        BLADES_A(b(6, 0, 7, 9, 2, 10), r(3, 0, 7, 11, 1, 8, 'y', 22.5f), r(5, 0, 9, 12, 1, 10, 'y', -22.5f),
                r(7, 2, 5, 8, 3, 12, 'y', 22.5f), r(8, 2, 8, 9, 5, 9, 'x', 22.5f)),
        BLADES_B(b(4, 0, 7, 10, 3, 8), b(6, 0, 8, 12, 2, 10), b(7, 0, 5, 9, 1, 7), b(5, 0, 9, 7, 1, 11)),
        CUBES_A(b(5, 0, 5, 9, 4, 9), b(9, 0, 8, 12, 3, 11), b(5, 0, 10, 7, 2, 12), b(10, 0, 4, 11, 1, 5)),
        CUBES_B(b(5, 0, 6, 8, 3, 9), r(7, 0, 7, 10, 3, 10, 'y', 45f), b(9, 0, 4, 11, 2, 6),
                r(4, 0, 10, 6, 2, 12, 'y', 22.5f)),
        CUBES_C(b(5, 0, 5, 10, 2, 10), b(6, 2, 6, 9, 4, 9), b(10, 0, 9, 12, 2, 11), b(4, 0, 9, 6, 1, 11)),
        CRYSTALS_A(b(5, 0, 6, 10, 1, 10), b(6, 1, 7, 8, 6, 9), b(8, 1, 6, 9, 4, 7),
                r(8, 1, 8, 10, 4, 10, 'x', 22.5f), r(5, 1, 7, 6, 3, 8, 'z', 22.5f), b(10, 0, 10, 11, 2, 11)),
        CRYSTALS_B(b(6, 0, 6, 10, 1, 10), b(7, 1, 7, 9, 5, 9), r(7, 1, 9, 9, 4, 11, 'x', 22.5f),
                r(7, 1, 5, 9, 4, 7, 'x', -22.5f), r(9, 1, 7, 11, 3, 9, 'z', -22.5f), r(5, 1, 7, 7, 3, 9, 'z', 22.5f)),
        CRYSTALS_C(b(4, 0, 7, 10, 2, 9), b(8, 0, 9, 10, 4, 11), r(6, 0, 4, 8, 2, 7, 'y', 22.5f),
                b(10, 0, 6, 11, 3, 7)),
        GEM_A(b(7, 0, 7, 9, 1, 9), b(6, 1, 6, 10, 2, 10), b(7, 2, 7, 9, 3, 9), r(10, 0, 10, 12, 2, 12, 'y', 45f),
                b(4, 0, 9, 5, 1, 10)),
        GEM_B(b(6, 0, 7, 10, 3, 9), b(7, 0, 6, 9, 3, 10), r(10, 0, 10, 12, 2, 12, 'y', 45f), b(4, 0, 4, 6, 1, 6)),
        PEBBLES(b(4, 0, 5, 6, 1, 7), b(7, 0, 4, 9, 2, 6), b(10, 0, 6, 12, 1, 8), b(5, 0, 9, 8, 2, 11),
                b(9, 0, 10, 11, 1, 12), b(8, 0, 7, 10, 1, 9)),
        CRUST_A(b(3, 0, 5, 9, 1, 10), b(7, 0, 8, 12, 1, 12), b(5, 1, 6, 8, 2, 9), b(10, 0, 4, 12, 1, 7)),
        CRUST_B(b(4, 0, 6, 10, 1, 11), r(6, 1, 5, 7, 4, 10, 'y', 22.5f), r(8, 1, 7, 12, 4, 8, 'y', -22.5f),
                b(5, 1, 9, 9, 3, 10)),
        LUMPS_A(b(5, 0, 5, 9, 2, 9), b(6, 2, 6, 8, 3, 8), b(9, 0, 6, 11, 2, 9), b(5, 0, 9, 8, 1, 11),
                b(4, 0, 6, 5, 1, 8)),
        LUMPS_B(b(6, 0, 4, 10, 3, 8), b(4, 0, 8, 7, 2, 11), b(9, 0, 9, 12, 1, 11), b(7, 0, 8, 9, 1, 9));

        private final List<Box> boxes;

        Shape(Box... boxes) {
            this.boxes = List.of(boxes);
        }
    }

    /** Ore sample name to shape; every surface sample must be listed. */
    static final Map<String, Shape> SHAPES = Map.ofEntries(
            Map.entry("native_platinum", Shape.NUGGETS_A), Map.entry("native_osmium", Shape.NUGGETS_A),
            Map.entry("native_iridium", Shape.NUGGETS_B), Map.entry("native_naquadah", Shape.NUGGETS_B),
            Map.entry("mithril_matrix", Shape.CHUNK_A), Map.entry("magnesite", Shape.CHUNK_A),
            Map.entry("cinnabar", Shape.CHUNK_A),
            Map.entry("uraninite", Shape.CHUNK_B), Map.entry("lapis_lazuli", Shape.CHUNK_B),
            Map.entry("cryolite", Shape.CHUNK_B),
            Map.entry("rutile", Shape.BLADES_A), Map.entry("stibnite", Shape.BLADES_A),
            Map.entry("wolframite", Shape.BLADES_B),
            Map.entry("galena", Shape.CUBES_A), Map.entry("pyrite", Shape.CUBES_A), Map.entry("sylvite", Shape.CUBES_A),
            Map.entry("fluorite", Shape.CUBES_B), Map.entry("carobbiite", Shape.CUBES_B),
            Map.entry("thorianite", Shape.CUBES_B),
            Map.entry("halite", Shape.CUBES_C), Map.entry("cobaltite", Shape.CUBES_C),
            Map.entry("quartz", Shape.CRYSTALS_A),
            Map.entry("amethyst", Shape.CRYSTALS_B), Map.entry("nether_quartz", Shape.CRYSTALS_B),
            Map.entry("spodumene", Shape.CRYSTALS_C), Map.entry("zircon", Shape.CRYSTALS_C),
            Map.entry("emerald", Shape.CRYSTALS_C), Map.entry("topaz", Shape.CRYSTALS_C),
            Map.entry("diamond", Shape.GEM_A),
            Map.entry("ruby", Shape.GEM_B), Map.entry("sapphire", Shape.GEM_B),
            Map.entry("bauxite", Shape.PEBBLES), Map.entry("opal", Shape.PEBBLES),
            Map.entry("saltpeter", Shape.CRUST_A), Map.entry("sulfur", Shape.CRUST_A), Map.entry("borax", Shape.CRUST_A),
            Map.entry("gypsum", Shape.CRUST_B),
            Map.entry("bituminous_coal", Shape.LUMPS_A), Map.entry("graphite", Shape.LUMPS_A),
            Map.entry("lignite", Shape.LUMPS_B));

    private static final String[] FACES = {"north", "east", "south", "west", "up", "down"};
    private final Map<String, Mask> masks = new HashMap<>();
    private final ExistingFileHelper existing;

    IndicatorModels(ExistingFileHelper existing) {
        this.existing = existing;
    }

    JsonObject model(String ore, String texture) {
        Shape shape = SHAPES.get(ore);
        if (shape == null) throw new IllegalStateException("No indicator shape for " + ore);
        Mask mask = masks.computeIfAbsent(texture, this::load);
        JsonObject json = new JsonObject();
        JsonObject textures = new JsonObject();
        textures.addProperty("0", texture);
        textures.addProperty("particle", texture);
        json.add("textures", textures);
        JsonArray elements = new JsonArray();
        for (int i = 0; i < shape.boxes.size(); i++) elements.add(element(shape.boxes.get(i), mask, texture + "#" + i));
        json.add("elements", elements);
        return json;
    }

    private static JsonObject element(Box box, Mask mask, String key) {
        JsonObject json = new JsonObject();
        json.add("from", vector(box.x1, box.y1, box.z1));
        json.add("to", vector(box.x2, box.y2, box.z2));
        if (box.axis != 0) {
            JsonObject rotation = new JsonObject();
            rotation.addProperty("angle", box.angle);
            rotation.addProperty("axis", String.valueOf(box.axis));
            rotation.add("origin", vector(box.cx(), box.y1, box.cz()));
            json.add("rotation", rotation);
        }
        int dx = box.x2 - box.x1;
        int dy = box.y2 - box.y1;
        int dz = box.z2 - box.z1;
        JsonObject faces = new JsonObject();
        for (String face : FACES) {
            // Bottoms of raised pieces sit inside the pieces below them.
            if (face.equals("down") && box.y1 > 0) continue;
            int width = switch (face) {
                case "east", "west" -> dz;
                default -> dx;
            };
            int height = switch (face) {
                case "up", "down" -> dz;
                default -> dy;
            };
            JsonObject value = new JsonObject();
            value.add("uv", mask.fit(width, height, key + face));
            value.addProperty("texture", "#0");
            if (face.equals("down")) value.addProperty("cullface", "down");
            faces.add(face, value);
        }
        json.add("faces", faces);
        return json;
    }

    private static JsonArray vector(float... values) {
        JsonArray array = new JsonArray();
        for (float value : values) {
            if (value == Math.rint(value)) array.add((int) value);
            else array.add(value);
        }
        return array;
    }

    private static Box b(int x1, int y1, int z1, int x2, int y2, int z2) {
        return new Box(x1, y1, z1, x2, y2, z2, (char) 0, 0);
    }

    private static Box r(int x1, int y1, int z1, int x2, int y2, int z2, char axis, float angle) {
        return new Box(x1, y1, z1, x2, y2, z2, axis, angle);
    }

    private Mask load(String texture) {
        ResourceLocation id = ResourceLocation.parse(texture);
        String path = "assets/" + id.getNamespace() + "/textures/" + id.getPath() + ".png";
        try (InputStream stream = open(id, path)) {
            BufferedImage image = ImageIO.read(stream);
            if (image == null) throw new IllegalStateException("Unreadable indicator texture " + texture);
            return new Mask(image);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read indicator texture " + texture, e);
        }
    }

    private InputStream open(ResourceLocation id, String path) throws IOException {
        IModFileInfo mod = ModList.get().getModFileById(id.getNamespace());
        if (mod != null) {
            Path packed = mod.getFile().findResource(path.split("/"));
            if (Files.isRegularFile(packed)) return Files.newInputStream(packed);
        }
        // Vanilla client assets and the --existing resource directories.
        return existing.getResource(id, PackType.CLIENT_RESOURCES, ".png", "textures").open();
    }

    /** Opaque pixels of a texture, scaled to model UV units. */
    private static final class Mask {
        private final int size;
        private final boolean[][] opaque;
        private final boolean[][] interior;

        Mask(BufferedImage image) {
            size = image.getWidth();
            if (image.getHeight() != size) throw new IllegalStateException("Indicator texture must be square");
            opaque = new boolean[size][size];
            for (int v = 0; v < size; v++) {
                for (int u = 0; u < size; u++) opaque[v][u] = (image.getRGB(u, v) >>> 24) != 0;
            }
            // Pixels whose four neighbours are opaque: avoids the dark item outline where possible.
            interior = new boolean[size][size];
            for (int v = 1; v < size - 1; v++) {
                for (int u = 1; u < size - 1; u++) {
                    interior[v][u] = opaque[v][u] && opaque[v - 1][u] && opaque[v + 1][u] && opaque[v][u - 1]
                            && opaque[v][u + 1];
                }
            }
        }

        JsonArray fit(int width, int height, String key) {
            int scale = size / 16;
            int w = width * scale;
            int h = height * scale;
            while (true) {
                for (boolean[][] pixels : new boolean[][][] {interior, opaque}) {
                    List<int[]> candidates = candidates(pixels, w, h);
                    if (!candidates.isEmpty()) {
                        int[] at = candidates.get(Math.floorMod(key.hashCode(), candidates.size()));
                        JsonArray uv = new JsonArray();
                        uv.add(at[0] / (float) scale);
                        uv.add(at[1] / (float) scale);
                        uv.add((at[0] + w) / (float) scale);
                        uv.add((at[1] + h) / (float) scale);
                        return integral(uv);
                    }
                }
                // Stretch a smaller opaque patch over faces wider than the texture's opaque area.
                if (w >= h && w > 1) w--;
                else if (h > 1) h--;
                else throw new IllegalStateException("Indicator texture has no opaque pixel");
            }
        }

        private List<int[]> candidates(boolean[][] pixels, int w, int h) {
            List<int[]> list = new ArrayList<>();
            for (int v = 0; v + h <= size; v++) {
                for (int u = 0; u + w <= size; u++) {
                    if (filled(pixels, u, v, w, h)) list.add(new int[] {u, v});
                }
            }
            return list;
        }

        private static boolean filled(boolean[][] pixels, int u, int v, int w, int h) {
            for (int y = v; y < v + h; y++) {
                for (int x = u; x < u + w; x++) {
                    if (!pixels[y][x]) return false;
                }
            }
            return true;
        }

        private static JsonArray integral(JsonArray uv) {
            JsonArray out = new JsonArray();
            for (var value : uv) {
                float f = value.getAsFloat();
                if (f == Math.rint(f)) out.add((int) f);
                else out.add(f);
            }
            return out;
        }
    }
}
