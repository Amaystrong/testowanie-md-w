package pl.marcin.serduszka.network;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** Server -> clients: player X started / stopped the emote. */
public record EmoteStatePacket(UUID player, boolean active) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeBoolean(active);
    }

    public static EmoteStatePacket decode(FriendlyByteBuf buf) {
        return new EmoteStatePacket(buf.readUUID(), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHook.apply(player, active));
    }
}
