package xzeroair.trinkets.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xzeroair.trinkets.capabilities.race.EntityProperties;
import xzeroair.trinkets.races.EntityRacePropertiesHandler;

@Mixin(Entity.class)
public abstract class EntityPoseMixin {

    @Inject(method = "canEnterPose", at = @At("HEAD"), cancellable = true)
    private void xat$fixRacePoseCollision(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        final Entity entity = (Entity) (Object) this;

        if (!(entity instanceof Player player)) {
            return;
        }

        final EntityProperties properties = EntityProperties.get(player);
        if (properties == null) {
            return;
        }

        final EntityRacePropertiesHandler handler = properties.getRaceHandler();

        if (handler == null) {
            return;
        }

        final float widthScale = handler.getWidthScale();
        final float heightScale = handler.getHeightScale();

        if (widthScale == 1.0F && heightScale == 1.0F) {
            return;
        }

        final EntityDimensions base = player.getDimensions(pose);

        final float width = handler.clampWidth(base.width * widthScale);

        final float height = handler.clampHeight(base.height * heightScale);

        final EntityDimensions scaled = base.fixed ? EntityDimensions.fixed(width, height) : EntityDimensions.scalable(width, height);

        cir.setReturnValue(player.level().noCollision(player, scaled.makeBoundingBox(player.position()).deflate(1.0E-7D)));
    }
}