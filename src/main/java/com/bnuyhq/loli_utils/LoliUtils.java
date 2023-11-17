package com.bnuyhq.loli_utils;

import com.bnuyhq.loli_utils.commands.MapartCopy;
import com.bnuyhq.loli_utils.modules.ChunkAnalyzer;
import com.bnuyhq.loli_utils.modules.ChunkSaver;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.hud.elements.MeteorTextHud;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class LoliUtils extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("LoliUtils");
    public static final HudGroup HUD_GROUP = new HudGroup("Loli Utils");

    @Override
    public void onInitialize() {
        LOG.info("Loli Utils loading :3");
        Modules.get().add(new ChunkAnalyzer());
        Modules.get().add(new ChunkSaver());

        // Commands
        Commands.add(new MapartCopy());

        MeteorStarscript.ss.set("map_pos", new ValueMap()
            .set("x", (starscript, si) -> {
                if (mc.player == null) return Value.string("0");
                int i = 128;
                int j = MathHelper.floor((mc.player.getBlockX() + 64.0) / (double) i);
                int l = j * i + i / 2 - 64;
                return Value.string(Integer.toString(l / 128));
            })
            .set("z", (starscript, si) -> {
                if (mc.player == null) return Value.string("0");
                int i = 128;
                int j = MathHelper.floor((mc.player.getBlockZ() + 64.0) / (double) i);
                int l = j * i + i / 2 - 64;
                return Value.string(Integer.toString(l / 128));
            })
        );

        MeteorTextHud.INFO.addPreset("Map Quadrant", textHud -> {
            textHud.text.set("#1{map_pos.x}, {map_pos.z}");
            textHud.updateDelay.set(0);
        });
    }

    @Override
    public String getPackage() {
        return "com.bnuyhq.loli_utils";
    }
}
