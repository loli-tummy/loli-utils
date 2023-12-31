package com.bnuyhq.loli_utils.mixin.xaeros;

import com.bnuyhq.loli_utils.utility.xaeros.SavedHighlighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.highlight.AbstractHighlighter;

@Mixin(value = xaero.map.highlight.HighlighterRegistry.class, remap = false)
public abstract class HighlighterRegistryMixin {
  @Shadow
  public abstract void register(AbstractHighlighter highlighter);

  @Inject(method = "<init>", at = @At("TAIL"))
  public void onInit(CallbackInfo ci) {
    this.register(new SavedHighlighter());
  }
}
