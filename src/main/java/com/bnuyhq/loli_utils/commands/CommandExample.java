package com.bnuyhq.loli_utils.commands;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class CommandExample extends Command {
  public CommandExample() {
    super("example", "Sends a message.");
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
