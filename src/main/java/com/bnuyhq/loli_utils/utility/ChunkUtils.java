package com.bnuyhq.loli_utils.utility;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;

public class ChunkUtils {
  public static boolean containsBlocks(int x, int z, List<Block> blockList) {
    assert mc.world != null : "Minecraft world is null";

    for (ChunkSection section : mc.world.getChunk(x, z).getSectionArray()) {
      if (section.isEmpty()) continue;
      if (section.hasAny(blockState -> blockList.contains(blockState.getBlock()))) {
        return true;
      }
    }

    return false;
  }

  public static ObjectIntPair<ChunkPos> getPacketUpdates(ChunkDeltaUpdateS2CPacket packet) {
    var ref =
        new Object() {
          ChunkPos chunkPos = null;
          int updates = 0;
        };

    packet.visitUpdates(
        (pos, blockState) -> {
          if (ref.chunkPos == null) ref.chunkPos = new ChunkPos(pos);
          if (blockState.isLiquid()) {
            FlowableFluid state = (FlowableFluid) blockState.getFluidState().getFluid();
            assert mc.player != null : "The Minecraft Player entity is null";
            if (!state.getSpread(mc.player.getWorld(), pos, blockState).isEmpty()) ref.updates++;
          }
        });

    return new ObjectIntImmutablePair<>(ref.chunkPos, ref.updates);
  }
}
