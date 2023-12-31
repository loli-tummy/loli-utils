package com.bnuyhq.loli_utils.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class ReGodMode extends Module {
  public ReGodMode() {
    super(
        Categories.Player,
        "re-god-mode",
        "Allows you to become invincible through various methods.");
    this.runInMainMenu = true;
  }

  @EventHandler
  public void onPacketReceived(PacketEvent.Receive event) {
    if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
      event.cancel();
    } else if (event.packet instanceof GameStateChangeS2CPacket packet) {
      if (packet.getReason() == GameStateChangeS2CPacket.GAME_WON) {
        event.cancel();
      }
    }
  }

  @EventHandler
  public void onPacketSend(PacketEvent.Send event) {
    if (event.packet instanceof TeleportConfirmC2SPacket) {
      event.cancel();
    } else if (event.packet instanceof ClientStatusC2SPacket packet) {
      if (packet.getMode() == ClientStatusC2SPacket.Mode.PERFORM_RESPAWN) {
        event.cancel();
      }
    }
  }
}
