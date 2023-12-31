package com.bnuyhq.loli_utils.mixin.meteorclient;

import meteordevelopment.meteorclient.systems.modules.misc.PacketCanceller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PacketCanceller.class, remap = false)
public class PacketCancellerMixin {
  @Inject(method = "<init>", at = @At("TAIL"))
  public void onInit(CallbackInfo ci) {
    ((PacketCanceller) (Object) this).runInMainMenu = true;
  }
}
