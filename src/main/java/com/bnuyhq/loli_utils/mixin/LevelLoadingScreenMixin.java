package com.bnuyhq.loli_utils.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenMixin extends Screen {
  protected LevelLoadingScreenMixin(Text title) {
    super(title);
  }

  /**
   * @author ViaTi
   * @reason UGH STOP ASKING ME STUPID QUESTIONS ! I FUCKING HATE YOU ! YOU AREN'T REAL !
   */
  @Overwrite
  public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
    this.close();
  }
}
