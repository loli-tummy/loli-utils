package com.bnuyhq.loli_utils.mixin;

import com.bnuyhq.loli_utils.events.ChunkLoadEvent;
import com.bnuyhq.loli_utils.events.ChunkUnloadEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.ClientPlayerSession;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayNetworkHandler.class, priority = 1)
public abstract class ClientPlayNetworkHandlerMixin {

  @Shadow private @Nullable ClientPlayerSession session;

  @Shadow private MessageChain.Packer messagePacker;

  @Shadow
  public abstract ClientConnection getConnection();

  @Inject(method = "loadChunk", at = @At("TAIL"))
  public void onLoadChunk(int x, int z, ChunkData chunkData, CallbackInfo ci) {
    MeteorClient.EVENT_BUS.post(ChunkLoadEvent.INSTANCE.get(x, z));
  }

  @Inject(method = "onUnloadChunk", at = @At("HEAD"))
  public void onUnloadChunk(UnloadChunkS2CPacket packet, CallbackInfo ci) {
    MeteorClient.EVENT_BUS.post(ChunkUnloadEvent.INSTANCE.get(packet.pos().x, packet.pos().z));
  }

  @Inject(method = "updateKeyPair", at = @At("HEAD"), cancellable = true)
  public void onUpdateKeypair(PlayerKeyPair keyPair, CallbackInfo ci) {
    this.session = new ClientPlayerSession(null, keyPair);
    this.messagePacker = this.session.createPacker(null);
    this.getConnection()
        .send(new PlayerSessionC2SPacket(this.session.toPublicSession().toSerialized()));
    ci.cancel();
  }
}
