package pl.marcin.serduszka.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import pl.marcin.serduszka.ServerEmotes;

/** Client -> server: "I started / stopped the emote". */
public record EmoteRequestPacket(boolean active) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
    }

    public static EmoteRequestPacket decode(FriendlyByteBuf buf) {
        return new EmoteRequestPacket(buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender != null) {
            ServerEmotes.set(sender, active);
        }
    }
}
