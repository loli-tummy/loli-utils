package pictures.cunny.loli_utils.mixin;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pictures.cunny.loli_utils.modules.PacketPlace;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
  @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
  public void interactBlock(
      ClientPlayerEntity player,
      Hand hand,
      BlockHitResult hitResult,
      CallbackInfoReturnable<ActionResult> cir) {
    if (!Modules.get().isActive(PacketPlace.class)) return;
    assert mc.getNetworkHandler() != null;
    mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult, 0));
    cir.setReturnValue(ActionResult.PASS);
  }
}
