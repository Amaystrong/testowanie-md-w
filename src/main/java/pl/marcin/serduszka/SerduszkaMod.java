package pl.marcin.serduszka;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pl.marcin.serduszka.network.Net;

@Mod(SerduszkaMod.MODID)
public class SerduszkaMod {
    public static final String MODID = "serduszka";

    public SerduszkaMod() {
        ModParticles.register(FMLJavaModLoadingContext.get().getModEventBus());
        Net.register();
        MinecraftForge.EVENT_BUS.register(ServerEmotes.class);
    }
}
