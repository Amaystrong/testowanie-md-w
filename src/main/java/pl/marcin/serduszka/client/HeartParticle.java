package pl.marcin.serduszka.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Small floating heart using the vanilla heart texture. Purely client side, no registration needed. */
public class HeartParticle extends TextureSheetParticle {
    private final float baseSize;

    public HeartParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
                         TextureAtlasSprite sprite) {
        super(level, x, y, z);
        this.setSprite(sprite);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.gravity = 0.0F;
        this.friction = 0.97F;
        this.hasPhysics = false;
        this.lifetime = 22 + this.random.nextInt(18);
        this.baseSize = 0.06F + this.random.nextFloat() * 0.09F;
        this.quadSize = 0.0F;
        float shade = 0.85F + this.random.nextFloat() * 0.15F;
        this.rCol = 1.0F;
        this.gCol = shade;
        this.bCol = shade;
    }

    @Override
    public void tick() {
        super.tick();
        float life = (float) this.age / (float) this.lifetime;
        // pop in quickly, gently shrink and fade near the end
        float grow = Math.min(1.0F, this.age / 4.0F);
        this.quadSize = this.baseSize * grow * (life > 0.7F ? 1.0F - (life - 0.7F) * 1.5F : 1.0F);
        this.alpha = life > 0.6F ? 1.0F - (life - 0.6F) / 0.4F : 1.0F;
        this.xd += (this.random.nextFloat() - 0.5F) * 0.002F;
        this.zd += (this.random.nextFloat() - 0.5F) * 0.002F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
