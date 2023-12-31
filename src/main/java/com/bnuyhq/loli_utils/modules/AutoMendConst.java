package com.bnuyhq.loli_utils.modules;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import baritone.api.BaritoneAPI;
import com.bnuyhq.loli_utils.utility.InventoryUtils;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoMendConst extends Module {
  private final SettingGroup sgDefault = settings.getDefaultGroup();
  public final Setting<List<Item>> fixableItems =
      sgDefault.add(
          new ItemListSetting.Builder()
              .name("fixable")
              .description("What items to allow mending to fix.")
              .defaultValue(Items.NETHERITE_PICKAXE)
              .filter(Item::isDamageable)
              .build());

  private int stepCounter = -1;
  private FindItemResult result;

  public AutoMendConst() {
    super(Categories.Player, "auto-mend-const", "Auto mending for constantiam.net");
  }

  @Override
  public void onActivate() {
    stepCounter = -1;
    result = null;
    if (BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive()) {
      BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().cancel();
    }
  }

  @Override
  public void onDeactivate() {
    this.onActivate();
  }

  @EventHandler
  public void onTick(TickEvent.Pre event) {
    if (mc.player == null || mc.player.getInventory() == null) return;

    if (stepCounter > 0) {
      if (BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive()) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().cancel();
      }
      stepCounter++;
      processStep();
      return;
    }

    result =
        InvUtils.find(
            itemStack -> fixableItems.get().contains(itemStack.getItem()) && itemStack.isDamaged(),
            9,
            mc.player.getInventory().size());

    if (!result.found() || result.isMainHand() || result.isArmor() || result.isOffhand()) return;

    if (mc.player.getOffHandStack() != null) {
      ItemStack offhand = mc.player.getOffHandStack();

      if (offhand.isDamaged()) {
        if (!BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive()) {
          BaritoneAPI.getProvider()
              .getPrimaryBaritone()
              .getMineProcess()
              .mine(Blocks.NETHER_QUARTZ_ORE);
        }
        return;
      } else {
        if (BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive()) {
          BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().cancel();
        }
      }
    } else {
      if (BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive()) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().cancel();
      }
    }

    stepCounter = 1;
    processStep();
  }

  private void processStep() {
    if (stepCounter == -1 || result == null) {
      stepCounter = -1;
      result = null;
      return;
    }

    switch (stepCounter) {
      case 1:
        InventoryUtils.swapSlot(
            result.isHotbar() ? result.slot() : InventoryUtils.findEmptySlotInHotbar(7), false);
        break;
      case 4:
        if (result.isHotbar()) {
          break;
        }
        Objects.requireNonNull(mc.getNetworkHandler())
            .sendPacket(new PickFromInventoryC2SPacket(result.slot()));
        break;
      case 6:
          assert mc.player != null;
          if (EnchantmentHelper.getLevel(
                Enchantments.FORTUNE, mc.player.getInventory().getStack(result.slot()))
            < 1) {
          // This is vanilla functionality,
          Objects.requireNonNull(mc.getNetworkHandler())
              .sendPacket(
                  new PlayerActionC2SPacket(
                      PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                      BlockPos.ORIGIN,
                      Direction.DOWN));
        }
        break;
      case 12:
        stepCounter = -1;
    }
  }
}
