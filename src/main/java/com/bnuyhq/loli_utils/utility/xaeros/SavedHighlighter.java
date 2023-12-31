package com.bnuyhq.loli_utils.utility.xaeros;

import com.bnuyhq.loli_utils.modules.XaerosHighlights;
import com.bnuyhq.loli_utils.utility.SavingUtility;
import java.awt.*;
import java.util.List;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xaero.map.highlight.ChunkHighlighter;

public class SavedHighlighter extends ChunkHighlighter {
  BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
  Text portalText = Text.literal("Portal");

  public SavedHighlighter() {
    super(true);
  }

  @Override
  protected int[] getColors(RegistryKey<World> registryKey, int i, int i1) {
    if (!this.chunkIsHighlit(registryKey, i, i1)) {
      return null;
    }

    XaerosHighlights module = Modules.get().get(XaerosHighlights.class);

    int centerColor = module.savedColor.get().getPacked();
    int sideColor = module.outlineColor.get().getPacked();

    centerColor =
        (centerColor & 255) << 24
            | (centerColor >> 8 & 255) << 16
            | (centerColor >> 16 & 255) << 8
            | 255 * 40 / 100;
    sideColor =
        (sideColor & 255) << 24
            | (sideColor >> 8 & 255) << 16
            | (sideColor >> 16 & 255) << 8
            | 255 * 70 / 100;

    this.resultStore[0] = centerColor;
    this.resultStore[1] = (i1 & 3) == 0 ? sideColor : centerColor;
    this.resultStore[2] = (i & 3) == 3 ? sideColor : centerColor;
    this.resultStore[3] = (i1 & 3) == 3 ? sideColor : centerColor;
    this.resultStore[4] = (i & 3) == 0 ? sideColor : centerColor;
    return this.resultStore;
  }

  @Override
  public Text getChunkHighlightSubtleTooltip(RegistryKey<World> registryKey, int i, int i1) {
    return Text.of("Saved chunk.");
  }

  @Override
  public Text getChunkHighlightBluntTooltip(RegistryKey<World> registryKey, int i, int i1) {
    String str = "Chunk is saved.";

    if (SavingUtility.getNbt(i, i1).getCompound("SpecialData").getBoolean("HasPortal")) {
      str += " - Has Portal.";
    }

    return Text.of(str);
  }

  @Override
  public int calculateRegionHash(RegistryKey<World> registryKey, int i, int i1) {
    return 50779;
  }

  @Override
  public boolean regionHasHighlights(RegistryKey<World> registryKey, int i, int i1) {
    return Modules.get().isActive(XaerosHighlights.class);
  }

  @Override
  public boolean chunkIsHighlit(RegistryKey<World> registryKey, int i, int i1) {
    return SavingUtility.chunkIsPresent(i, i1);
  }

  @Override
  public void addMinimapBlockHighlightTooltips(
      List<Text> list, RegistryKey<World> registryKey, int i, int i1, int i2) {}
}
