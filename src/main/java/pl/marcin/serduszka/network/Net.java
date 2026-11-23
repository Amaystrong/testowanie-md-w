package pl.marcin.serduszka.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import pl.marcin.serduszka.SerduszkaMod;

public final class Net {
    private static final String VERSION = "1";

    /** Accepts any remote version (including absent), so the mod also works on servers without it (local only). */
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SerduszkaMod.MODID, "main"),
            () -> VERSION,
            remote -> true,
            remote -> true);

    private Net() {}

    public static void register() {
        CHANNEL.messageBuilder(EmoteRequestPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EmoteRequestPacket::encode)
                .decoder(EmoteRequestPacket::decode)
                .consumerMainThread(EmoteRequestPacket::handle)
                .add();
        CHANNEL.messageBuilder(EmoteStatePacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EmoteStatePacket::encode)
                .decoder(EmoteStatePacket::decode)
                .consumerMainThread(EmoteStatePacket::handle)
                .add();
    }
}
