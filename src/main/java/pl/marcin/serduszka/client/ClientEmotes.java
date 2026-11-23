package pl.marcin.serduszka.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.marcin.serduszka.SerduszkaMod;
import pl.marcin.serduszka.network.EmoteRequestPacket;
import pl.marcin.serduszka.network.Net;

@Mod.EventBusSubscriber(modid = SerduszkaMod.MODID, value = Dist.CLIENT)
public final class ClientEmotes {
    public static final KeyMapping TOGGLE_KEY = new KeyMapping("key.serduszka.toggle",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.serduszka");

    /** player UUID -> client tick when the emote started */
    private static final Map<UUID, Long> ACTIVE = new HashMap<>();
    private static final Set<Integer> PUSHED = new HashSet<>();
    private static final ResourceLocation HEART = new ResourceLocation("minecraft", "heart");
    private static long clientTicks;
    private static CameraType previousCamera;
    /** ignore movement for a moment right after starting, so the key press itself does not cancel it */
    private static int graceTicks;
    private static boolean particlesBroken;

    private ClientEmotes() {}

    // ---------------------------------------------------------------- state

    public static boolean isActive(UUID id) {
        return ACTIVE.containsKey(id);
    }

    /** Animation time in seconds for this player, or -1 when not emoting. */
    public static float animTime(UUID id) {
        Long start = ACTIVE.get(id);
        if (start == null) return -1;
        float partial = Minecraft.getInstance().getFrameTime();
        return (clientTicks - start + partial) / 20.0F;
    }

    public static void setActive(UUID id, boolean active) {
        Minecraft mc = Minecraft.getInstance();
        boolean self = mc.player != null && mc.player.getUUID().equals(id);
        if (active) {
            if (ACTIVE.containsKey(id)) return;
            ACTIVE.put(id, clientTicks);
            if (self) {
                graceTicks = 5;
                if (mc.options.getCameraType().isFirstPerson()) {
                    previousCamera = mc.options.getCameraType();
                    mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                }
            }
        } else {
            if (ACTIVE.remove(id) == null) return;
            if (self && previousCamera != null) {
                mc.options.setCameraType(previousCamera);
                previousCamera = null;
            }
        }
    }

    private static boolean serverHasMod() {
        ClientPacketListener conn = Minecraft.getInstance().getConnection();
        return conn != null && Net.CHANNEL.isRemotePresent(conn.getConnection());
    }

    private static void setSelf(boolean active) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        setActive(player.getUUID(), active);
        if (serverHasMod()) {
            Net.CHANNEL.sendToServer(new EmoteRequestPacket(active));
        }
    }

    // ---------------------------------------------------------------- events

    @Mod.EventBusSubscriber(modid = SerduszkaMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(TOGGLE_KEY);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE.clear();
        PUSHED.clear();
        previousCamera = null;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        clientTicks++;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        while (TOGGLE_KEY.consumeClick()) {
            setSelf(!isActive(player.getUUID()));
        }

        if (isActive(player.getUUID())) {
            if (graceTicks > 0) {
                graceTicks--;
            } else if (shouldInterrupt(mc, player)) {
                setSelf(false);
            }
        }

        if (mc.isPaused() || ACTIVE.isEmpty()) return;
        TextureAtlasSprite sprite = heartSprite(mc);
        if (sprite == null) return;
        RandomSource rnd = mc.level.random;
        for (AbstractClientPlayer p : mc.level.players()) {
            if (!isActive(p.getUUID()) || p.isInvisible()) continue;
            if (p == player && mc.options.getCameraType().isFirstPerson()) continue;
            int count = rnd.nextFloat() < 0.35F ? 2 : 1;
            for (int i = 0; i < count; i++) {
                spawnHeart(mc, p, rnd, sprite);
            }
        }
    }

    /** Heart sprite from the particle atlas (via the texture manager; ModelManager does not own that atlas). */
    private static TextureAtlasSprite heartSprite(Minecraft mc) {
        if (particlesBroken) return null;
        try {
            return mc.getTextureAtlas(TextureAtlas.LOCATION_PARTICLES).apply(HEART);
        } catch (Throwable t) {
            particlesBroken = true;
            org.slf4j.LoggerFactory.getLogger("serduszka").error("Serduszka: heart texture unavailable, particles disabled", t);
            return null;
        }
    }

    private static boolean shouldInterrupt(Minecraft mc, LocalPlayer p) {
        return p.input.forwardImpulse != 0 || p.input.leftImpulse != 0 || p.input.jumping
                || p.isShiftKeyDown() || p.isPassenger() || p.isSleeping() || p.isSwimming()
                || p.isFallFlying() || p.hurtTime > 0 || !p.isAlive()
                || mc.options.keyAttack.isDown() || mc.options.keyUse.isDown();
    }

    private static void spawnHeart(Minecraft mc, Player p, RandomSource rnd, TextureAtlasSprite sprite) {
        double angle = rnd.nextDouble() * Math.PI * 2;
        double radius = 0.25 + rnd.nextDouble() * 0.45;
        double x = p.getX() + Math.cos(angle) * radius;
        double z = p.getZ() + Math.sin(angle) * radius;
        double y = p.getY() + 1.45 + rnd.nextDouble() * 0.75;
        double vx = (rnd.nextDouble() - 0.5) * 0.01;
        double vz = (rnd.nextDouble() - 0.5) * 0.01;
        double vy = 0.012 + rnd.nextDouble() * 0.02;
        mc.particleEngine.add(new HeartParticle(mc.level, x, y, z, vx, vy, vz, sprite));
    }

    @SubscribeEvent
    public static void onRenderPre(RenderPlayerEvent.Pre event) {
        Player p = event.getEntity();
        float t = animTime(p.getUUID());
        if (t < 0) return;
        EmoteAnimation anim = EmoteAnimation.get();
        float bodyYaw = Mth.rotLerp(event.getPartialTick(), p.yBodyRotO, p.yBodyRot);
        event.getPoseStack().pushPose();
        PUSHED.add(p.getId());
        anim.applyRoot(event.getPoseStack(), anim.sample(t), bodyYaw);
    }

    @SubscribeEvent
    public static void onRenderPost(RenderPlayerEvent.Post event) {
        if (PUSHED.remove(event.getEntity().getId())) {
            event.getPoseStack().popPose();
        }
    }
}
