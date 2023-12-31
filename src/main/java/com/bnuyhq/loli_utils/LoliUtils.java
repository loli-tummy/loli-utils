package com.bnuyhq.loli_utils;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import com.bnuyhq.loli_utils.commands.MapartCopy;
import com.bnuyhq.loli_utils.modules.*;
import com.bnuyhq.loli_utils.utility.MathUtils;
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

public class LoliUtils extends MeteorAddon {
  public static final Logger LOG = LoggerFactory.getLogger("Loli Utils");
  public static final HudGroup HUD_GROUP = new HudGroup("Loli Utils");

  @Override
  public void onInitialize() {
    LOG.info("Loli Utils loading :3");

    Modules.get().add(new AutoMendConst());
    Modules.get().add(new ChunkAnalyzer());
    Modules.get().add(new ChunkSaver());
    Modules.get().add(new PacketPlace());
    Modules.get().add(new XaerosHighlights());
    Modules.get().add(new ReGodMode());

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

    MeteorTextHud.INFO.addPreset(
        "Map Quad",
        textHud -> {
          textHud.text.set("#1{map_pos.x}, {map_pos.z}");
          textHud.updateDelay.set(0);
        });
  }

  @Override
  public String getPackage() {
    return "com.bnuyhq.loli_utils";
  }
}
