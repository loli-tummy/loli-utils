package pictures.cunny.loli_utils.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ProgressScreen.class)
public class ProgressScreenMixin extends Screen {

  protected ProgressScreenMixin(Text title) {
    super(title);
  }

  /**
   * @author ViaTi
   * @reason It uh closes the screen? FUCK YOU!
   */
  @Overwrite
  public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
    this.close();
  }
}
