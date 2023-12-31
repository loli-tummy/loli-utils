package com.bnuyhq.loli_utils.commands;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.Script;
import net.minecraft.command.CommandSource;

public class InfoMsg extends Command {
  public InfoMsg() {
    super("info", "Sends a string as an info message.");
  }

  @Override
  public void build(LiteralArgumentBuilder<CommandSource> builder) {
    builder.then(
        argument("msg", StringArgumentType.greedyString())
            .executes(
                context -> {
                  String msg = context.getArgument("msg", String.class);
                  Script script = MeteorStarscript.compile(msg);

                  if (script != null) {
                    String message = MeteorStarscript.run(script);
                    info(message);
                  }
                  return SINGLE_SUCCESS;
                }));
  }
}
