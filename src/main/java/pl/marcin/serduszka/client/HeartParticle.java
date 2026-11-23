package pl.marcin.serduszka.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** Small pixel heart that slowly floats up and drifts from side to side. */
public class HeartParticle extends TextureSheetParticle {
    private final float baseSize;
    private final double driftX;
    private final double driftZ;
    private final float phase;

    protected HeartParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        super(level, x, y, z);
        this.driftX = vx;
        this.driftZ = vz;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.hasPhysics = false;
        this.lifetime = 40 + this.random.nextInt(30);
        this.baseSize = 0.07F + this.random.nextFloat() * 0.09F;
        this.quadSize = this.baseSize * 0.3F;
        this.phase = this.random.nextFloat() * (float) (Math.PI * 2);
    }

    @Override
    public void tick() {
        super.tick();
        float life = (float) this.age / (float) this.lifetime;
        float grow = Math.min(1.0F, this.age / 5.0F);
        float shrink = life > 0.75F ? Math.max(0.0F, 1.0F - (life - 0.75F) * 4.0F) : 1.0F;
        this.quadSize = this.baseSize * Math.max(0.3F, grow) * shrink;
        float sway = Mth.sin(this.age * 0.15F + this.phase);
        this.xd = this.driftX + sway * 0.006F;
        this.zd = this.driftZ + Mth.cos(this.age * 0.12F + this.phase) * 0.006F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz) {
            HeartParticle p = new HeartParticle(level, x, y, z, vx, vy, vz);
            p.pickSprite(this.sprites);
            return p;
        }
    }
}
