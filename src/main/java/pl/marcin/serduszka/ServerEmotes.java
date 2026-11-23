package pl.marcin.serduszka;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import pl.marcin.serduszka.network.EmoteStatePacket;
import pl.marcin.serduszka.network.Net;

/** Server side list of players that are currently playing the emote. */
public final class ServerEmotes {
    private static final Set<UUID> ACTIVE = new HashSet<>();

    private ServerEmotes() {}

    public static void set(ServerPlayer player, boolean active) {
        boolean changed = active ? ACTIVE.add(player.getUUID()) : ACTIVE.remove(player.getUUID());
        if (changed || active) {
            broadcast(player, new EmoteStatePacket(player.getUUID(), active));
        }
    }

    private static void broadcast(ServerPlayer source, EmoteStatePacket packet) {
        for (ServerPlayer p : source.server.getPlayerList().getPlayers()) {
            send(p, packet);
        }
    }

    private static void send(ServerPlayer target, EmoteStatePacket packet) {
        if (Net.CHANNEL.isRemotePresent(target.connection.connection)) {
            Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), packet);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            for (UUID id : ACTIVE) {
                send(sp, new EmoteStatePacket(id, true));
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            set(sp, false);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp && ACTIVE.contains(sp.getUUID())) {
            set(sp, false);
        }
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp && ACTIVE.contains(sp.getUUID())) {
            set(sp, false);
        }
    }
}
