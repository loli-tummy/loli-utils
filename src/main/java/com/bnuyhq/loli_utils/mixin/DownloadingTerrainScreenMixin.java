package com.bnuyhq.loli_utils.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(DownloadingTerrainScreen.class)
public abstract class DownloadingTerrainScreenMixin extends Screen {

  protected DownloadingTerrainScreenMixin(Text title) {
    super(title);
  }

  /**
   * @author ViaTi
   * @reason Automatically close this, FUCK YOU!
   */
  @Overwrite
  public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
    this.close();
  }
}
