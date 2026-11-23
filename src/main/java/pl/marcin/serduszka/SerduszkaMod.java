package pl.marcin.serduszka;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import pl.marcin.serduszka.network.Net;

@Mod(SerduszkaMod.MODID)
public class SerduszkaMod {
    public static final String MODID = "serduszka";

    public SerduszkaMod() {
        Net.register();
        MinecraftForge.EVENT_BUS.register(ServerEmotes.class);
    }
}
