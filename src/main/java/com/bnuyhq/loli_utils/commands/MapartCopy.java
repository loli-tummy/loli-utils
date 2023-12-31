package com.bnuyhq.loli_utils.commands;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.block.MapColor;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.command.CommandSource;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;

public class MapartCopy extends Command {
  private final Path mapPath = Path.of(MeteorClient.FOLDER.getPath()).resolve("maps");
  private NativeImageBackedTexture texture;

  public MapartCopy() {
    super("copy-map", "Copies a map as an image.");

    try {
      Files.createDirectories(mapPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void build(LiteralArgumentBuilder<CommandSource> builder) {
    builder.then(
        literal("maps")
            .then(
                argument("x", IntegerArgumentType.integer())
                    .then(
                        argument("y", IntegerArgumentType.integer())
                            .executes(
                                context -> {
                                  texture =
                                      new NativeImageBackedTexture(
                                          context.getArgument("x", Integer.class) * 128,
                                          context.getArgument("y", Integer.class) * 128,
                                          true);
                                  return SINGLE_SUCCESS;
                                }))));

    builder.then(
        argument("x", IntegerArgumentType.integer())
            .then(
                argument("y", IntegerArgumentType.integer())
                    .executes(
                        context -> {
                          if (texture == null) return 0;
                          int x = context.getArgument("x", Integer.class),
                              y = context.getArgument("y", Integer.class);
                          if (mc.player != null
                              && mc.player.getMainHandStack().getItem() == Items.FILLED_MAP) {
                            MapState mapState =
                                FilledMapItem.getMapState(
                                    FilledMapItem.getMapId(mc.player.getMainHandStack()), mc.world);
                            for (int i = 0; i < 128; ++i) {
                              for (int j = 0; j < 128; ++j) {
                                int k = j + i * 128;
                                Objects.requireNonNull(this.texture.getImage())
                                    .setColor(
                                        j + (x * 128),
                                        i + (y * 128),
                                        MapColor.getRenderColor(
                                            Objects.requireNonNull(mapState).colors[k]));
                              }
                            }
                          }
                          return SINGLE_SUCCESS;
                        })));

    builder.then(
        literal("save")
            .executes(
                context -> {
                  if (mc.player != null && texture != null) {
                    saveMap(Objects.requireNonNull(texture.getImage()).hashCode(), texture);
                  }
                  return SINGLE_SUCCESS;
                }));

    builder.then(
        literal("single")
            .executes(
                context -> {
                  if (mc.player != null
                      && mc.player.getMainHandStack().getItem() == Items.FILLED_MAP) {
                    int mapId = FilledMapItem.getMapId(mc.player.getMainHandStack());
                    saveMap(mapId);
                  }
                  return SINGLE_SUCCESS;
                }));
  }

  public void saveMap(int id, NativeImageBackedTexture texture) {
    try {
      Objects.requireNonNull(texture.getImage())
          .writeTo(
              mapPath.resolve(
                  id
                      + "_"
                      + (mc.getCurrentServerEntry() != null && !mc.isInSingleplayer()
                          ? mc.getCurrentServerEntry().address
                          : "OFFLINE")
                      + ".png"));
    } catch (IOException e) {
      return;
    }
    this.texture = null;
  }

  public void saveMap(int id) {
    MapRenderer.MapTexture texture =
        mc.gameRenderer.getMapRenderer().getMapTexture(id, FilledMapItem.getMapState(id, mc.world));
    try {
      Objects.requireNonNull(texture.texture.getImage())
          .writeTo(
              mapPath.resolve(
                  id
                      + "_"
                      + (mc.getCurrentServerEntry() != null && !mc.isInSingleplayer()
                          ? mc.getCurrentServerEntry().address
                          : "OFFLINE")
                      + "_single"
                      + ".png"));
    } catch (IOException ignored) {
    }
  }
}
