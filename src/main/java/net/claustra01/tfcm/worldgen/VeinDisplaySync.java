package net.claustra01.tfcm.worldgen;

import java.util.List;
import java.util.Map;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.config.TFCMConfig;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.Dimension;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** The YAML files live on the server, so it sends clients the veins and geodes it generates for recipe viewers. */
public final class VeinDisplaySync {
    private static List<VeinDisplay> received = List.of();
    private static List<GeodeDisplay> receivedGeodes = List.of();
    private static Runnable listener = () -> { };

    private VeinDisplaySync() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Payload.TYPE, Payload.STREAM_CODEC, (payload, context) -> {
            received = payload.veins();
            receivedGeodes = payload.geodes();
            listener.run();
        });
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        Payload payload = build(event.getPlayerList().getServer());
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }

    /** Empty unless custom generation is on; uses the definitions the biome modifier read at server start. */
    public static Payload build(MinecraftServer server) {
        if (!TFCMConfig.ENABLE_CUSTOM_VEIN_GENERATION.get()) return new Payload(List.of(), List.of());
        Map<String, Dimension> definitions = definitions(server);
        return new Payload(VeinDisplay.build(definitions, server), GeodeDisplay.build(definitions));
    }

    static Map<String, Dimension> definitions(MinecraftServer server) {
        return server.registryAccess().registry(NeoForgeRegistries.Keys.BIOME_MODIFIERS).stream()
                .flatMap(Registry::stream)
                .filter(CustomWorldgenBiomeModifier.class::isInstance)
                .map(modifier -> ((CustomWorldgenBiomeModifier) modifier).definitions())
                .findFirst().orElse(Map.of());
    }

    public static List<VeinDisplay> received() {
        return received;
    }

    public static List<GeodeDisplay> receivedGeodes() {
        return receivedGeodes;
    }

    public static void setListener(Runnable newListener) {
        listener = newListener;
    }

    public record Payload(List<VeinDisplay> veins, List<GeodeDisplay> geodes) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, "ore_veins"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC = StreamCodec.composite(
                VeinDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), Payload::veins,
                GeodeDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), Payload::geodes,
                Payload::new);

        @Override
        public Type<Payload> type() {
            return TYPE;
        }
    }
}
