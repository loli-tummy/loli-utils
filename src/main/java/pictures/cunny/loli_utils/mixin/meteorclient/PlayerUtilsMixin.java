package pictures.cunny.loli_utils.mixin.meteorclient;

import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pictures.cunny.loli_utils.utility.SpecialEffects;

@Mixin(value = PlayerUtils.class, remap = false)
public class PlayerUtilsMixin {
  @Inject(method = "getPlayerColor", at = @At("HEAD"), cancellable = true)
  private static void modifyColor(
      PlayerEntity entity, Color defaultColor, CallbackInfoReturnable<Color> cir) {
    if (SpecialEffects.hasColor(entity)) {
      cir.setReturnValue(SpecialEffects.getColor(entity));
    }
  }
}
