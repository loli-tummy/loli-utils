package com.bnuyhq.loli_utils.mixin;

import com.bnuyhq.loli_utils.events.ChunkLoadEvent;
import com.bnuyhq.loli_utils.events.ChunkUnloadEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "loadChunk", at = @At("TAIL"))
    public void onLoadChunk(int x, int z, ChunkData chunkData, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(ChunkLoadEvent.INSTANCE.get(x, z));
    }

    @Inject(method = "onUnloadChunk", at = @At("HEAD"))
    public void onUnloadChunk(UnloadChunkS2CPacket packet, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(ChunkUnloadEvent.INSTANCE.get(packet.getX(), packet.getZ()));
    }
}
