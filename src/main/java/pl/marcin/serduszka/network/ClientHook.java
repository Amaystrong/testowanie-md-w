package pl.marcin.serduszka.network;

import java.util.UUID;

import pl.marcin.serduszka.client.ClientEmotes;

final class ClientHook {
    private ClientHook() {}

    static void apply(UUID player, boolean active) {
        ClientEmotes.setActive(player, active);
    }
}
