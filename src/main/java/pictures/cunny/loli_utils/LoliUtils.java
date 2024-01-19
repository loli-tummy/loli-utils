package pictures.cunny.loli_utils;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.hud.elements.MeteorTextHud;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pictures.cunny.loli_utils.commands.MapartCopy;
import pictures.cunny.loli_utils.modules.*;
import pictures.cunny.loli_utils.utility.Dependencies;
import pictures.cunny.loli_utils.utility.MathUtils;

public class LoliUtils extends MeteorAddon {
  public static final Logger LOGGER = LoggerFactory.getLogger("Loli Utils");
  public static final HudGroup HUD_GROUP = new HudGroup("Loli Utils");

  @Override
  public void onInitialize() {
    LOGGER.info("Loli Utils loading :3");

    Modules.get().add(new AutoMendConst());
    Modules.get().add(new ChunkAnalyzer());
    Modules.get().add(new ChunkSaver());
    Modules.get().add(new PacketPlace());
    Modules.get().add(new XaerosHighlights());

    if (Dependencies.LITEMATICA.isLoaded()) {
      Modules.get().add(new Printer());
    }

    // Commands
    Commands.add(new MapartCopy());

    MeteorStarscript.ss.set(
        "map_pos",
        new ValueMap()
            .set(
                "x",
                () ->
                    Value.number(
                        mc.player == null ? 0 : MathUtils.toMapQuad(mc.player.getBlockX())))
            .set(
                "z",
                () ->
                    Value.number(
                        mc.player == null ? 0 : MathUtils.toMapQuad(mc.player.getBlockZ()))));

    MeteorStarscript.ss.set(
        "is_day",
        (starscript, i) ->
            Value.bool(mc.world != null && ((double) mc.world.getTimeOfDay() / 12000L) % 2 == 0));

    MeteorTextHud.INFO.addPreset(
        "Map Quad",
        textHud -> {
          textHud.text.set("#1{map_pos.x}, {map_pos.z}");
          textHud.updateDelay.set(0);
        });
  }

  @Override
  public String getPackage() {
    return "pictures.cunny.loli_utils";
  }
}
