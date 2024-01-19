package pictures.cunny.loli_utils.modules;

import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class XaerosHighlights extends Module {
  private final SettingGroup sgDefault = settings.getDefaultGroup();
  public final Setting<SettingColor> outlineColor =
      sgDefault.add(
          new ColorSetting.Builder()
              .name("outline")
              .defaultValue(new Color(145, 29, 224, 187))
              .build());
  public final Setting<SettingColor> savedColor =
      sgDefault.add(
          new ColorSetting.Builder()
              .name("saved")
              .defaultValue(new Color(186, 161, 204, 80))
              .build());

  public XaerosHighlights() {
    super(Categories.Render, "xaeros-highlights", "Adds rendering features to Xaeros map.");
  }
}
