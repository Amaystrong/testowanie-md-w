package pl.marcin.serduszka.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import pl.marcin.serduszka.client.ClientEmotes;
import pl.marcin.serduszka.client.EmoteAnimation;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Unique
    private boolean serduszka$dirty;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    private void serduszka$reset(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                 float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (serduszka$dirty) {
            serduszka$dirty = false;
            EmoteAnimation.resetModel((HumanoidModel<?>) (Object) this);
        }
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void serduszka$pose(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;
        float t = ClientEmotes.animTime(player.getUUID());
        if (t < 0) return;
        EmoteAnimation.applyToModel((HumanoidModel<?>) (Object) this, EmoteAnimation.get().sample(t));
        serduszka$dirty = true;
    }
}
