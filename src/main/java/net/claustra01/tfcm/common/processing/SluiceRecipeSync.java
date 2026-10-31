package net.claustra01.tfcm.common.processing;

import java.util.List;

import net.claustra01.tfcm.TFCM;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Loot tables stay on the server, so it sends clients the Sluice outputs that recipe viewers show. */
public final class SluiceRecipeSync {
    private static List<SluiceRecipe> received = List.of();
    private static Runnable listener = () -> { };

    private SluiceRecipeSync() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Payload.TYPE, Payload.STREAM_CODEC, (payload, context) -> {
            received = payload.recipes();
            listener.run();
        });
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        Payload payload = new Payload(SluiceRecipe.build(event.getPlayerList().getServer()));
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }

    public static List<SluiceRecipe> received() {
        return received;
    }

    public static void setListener(Runnable newListener) {
        listener = newListener;
    }

    public record Payload(List<SluiceRecipe> recipes) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, "sluice_recipes"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC =
                SluiceRecipe.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Payload::new, Payload::recipes);

        @Override
        public Type<Payload> type() {
            return TYPE;
        }
    }
}
