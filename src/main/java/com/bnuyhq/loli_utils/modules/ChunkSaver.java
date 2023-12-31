package com.bnuyhq.loli_utils.modules;

import com.bnuyhq.loli_utils.events.ChunkLoadEvent;
import com.bnuyhq.loli_utils.events.ChunkUnloadEvent;
import com.bnuyhq.loli_utils.utility.SavingUtility;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.VersionedChunkStorage;

public class ChunkSaver extends Module {
  ExecutorService executorService =
      Executors.newSingleThreadExecutor(
          new ThreadFactoryBuilder().setNameFormat("Chunk Saver").setDaemon(true).build());

  ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
  ScheduledExecutorService backupService = Executors.newSingleThreadScheduledExecutor();

  private final SettingGroup sgDefault = settings.getDefaultGroup();
  public final Setting<String> saveSubDir =
      sgDefault.add(
          new StringSetting.Builder()
              .name("sub-dir")
              .description("The sub-dir to use for saving chunks.")
              .defaultValue("default")
              .build());
  public final Setting<Boolean> specialData =
      sgDefault.add(
          new BoolSetting.Builder()
              .name("special-data")
              .description("Adds easier to parse data, will modify save structure. Risk!")
              .defaultValue(true)
              .build());

  public ChunkSaver() {
    super(
        Categories.Misc,
        "chunk-saver",
        "Writes chunks to files for copy and pasting into a world.");
    scheduledExecutor.scheduleAtFixedRate(this::saveWrap, 30, 30, TimeUnit.SECONDS);
  }

  @EventHandler
  public void onLeave(GameLeftEvent event) {
    SavingUtility.save();

    for (Map.Entry<String, VersionedChunkStorage> entry :
        SavingUtility.VERSIONED_CHUNK_STORAGE_HASH_MAP.entrySet()) {
      try {
        entry.getValue().close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    SavingUtility.VERSIONED_CHUNK_STORAGE_HASH_MAP.clear();
  }

  @EventHandler
  public void onChunkLoad(ChunkLoadEvent event) {
    saveRec(event.x, event.z);
  }

  @EventHandler
  public void onChunkUnload(ChunkUnloadEvent event) {
    saveRec(event.x, event.z);
  }

  public String getSubDir() {
    if (saveSubDir.get().isBlank()) {
      return saveSubDir.getDefaultValue();
    }

    return saveSubDir.get().strip();
  }

  public void saveRec(int x, int z) {
    executorService.execute(() -> saveChunk(x, z));
  }

  public void saveWrap() {
    executorService.execute(SavingUtility::save);
  }

  public void saveChunk(int x, int z) {
    if (mc.isInSingleplayer()) {
      return;
    }

    SavingUtility.makeDefaults(getSubDir());

    assert mc.player != null : "Player is null.";
    assert mc.world != null : "World is null.";

    ChunkPos chunkPos = new ChunkPos(x, z);
    Chunk chunk = mc.world.getChunk(x, z);

    NbtCompound nbtCompound =
        SavingUtility.serialize(mc.player.getWorld(), chunk, chunkPos, specialData.get());
    SavingUtility.VERSIONED_CHUNK_STORAGE_HASH_MAP
        .get(SavingUtility.getCurrentKey(getSubDir()))
        .setNbt(chunkPos, nbtCompound);
  }
}
