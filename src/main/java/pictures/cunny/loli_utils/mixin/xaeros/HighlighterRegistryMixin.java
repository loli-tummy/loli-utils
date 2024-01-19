package pictures.cunny.loli_utils.mixin.xaeros;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.utility.xaeros.SavedHighlighter;
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
