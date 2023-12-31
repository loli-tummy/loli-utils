package com.bnuyhq.loli_utils.commands;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class FriendColor extends Command {
  public FriendColor() {
    super("friend-color", "Modifies the friend color.");
  }

  @Override
  public void build(LiteralArgumentBuilder<CommandSource> builder) {
    builder.executes(
        context -> {
          info("hi");
          return SINGLE_SUCCESS;
        });
  }
}
