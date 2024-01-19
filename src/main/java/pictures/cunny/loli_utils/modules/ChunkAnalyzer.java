package pictures.cunny.loli_utils.modules;

import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import pictures.cunny.loli_utils.LoliUtils;
import pictures.cunny.loli_utils.events.ChunkLoadEvent;
import pictures.cunny.loli_utils.utility.ChunkUtils;

public class ChunkAnalyzer extends Module {
  public final ObjectBigArrayBigList<ChunkPos> checkBlocksChunks =
      new ObjectBigArrayBigList<>(300000);
  public final ObjectBigArrayBigList<ChunkPos> blockUpdateChunks =
      new ObjectBigArrayBigList<>(300000);
  public final ObjectBigArrayBigList<ChunkPos> preChunks = new ObjectBigArrayBigList<>(300000);
  public final ObjectBigArrayBigList<ChunkPos> entityIdChunks = new ObjectBigArrayBigList<>(300000);
  public final ObjectBigArrayBigList<BlockPos> cachedGateways = new ObjectBigArrayBigList<>();
  private int loadDistance = 10;
  private double renderOffset = 0;
  private boolean isGoingUp = true;

  private final SettingGroup sgOutlines = settings.createGroup("Outlines");
  public final Setting<Boolean> preRender =
      sgOutlines.add(
          new BoolSetting.Builder()
              .name("pre-render-chunks")
              .description("Shows the render for chunk pre-render of another outline.")
              .defaultValue(true)
              .build());
  public final Setting<Boolean> checkBlocks =
      sgOutlines.add(
          new BoolSetting.Builder()
              .name("check-blocks")
              .description(
                  "Checks for certain blocks, designed for 1.12 updated to newer versions.")
              .defaultValue(true)
              .build());
  public final Setting<List<Block>> blockList =
      sgOutlines.add(
          new BlockListSetting.Builder()
              .name("blocks")
              .description("Blocks to check for.")
              .defaultValue(Blocks.NETHER_GOLD_ORE, Blocks.COPPER_ORE)
              .build());
  public final Setting<Boolean> blockUpdates =
      sgOutlines.add(
          new BoolSetting.Builder()
              .name("block-updates")
              .description("Check for Liquid/Falling block updates.")
              .build());
  public final Setting<Integer> blockUpdatesMin =
      sgOutlines.add(
          new IntSetting.Builder()
              .name("min-updates")
              .description("The minimum amount of updates.")
              .sliderRange(1, 200)
              .defaultValue(4)
              .build());

  private final SettingGroup sgLogic = settings.createGroup("Logic");
  public final Setting<Boolean> notifyOnRD =
      sgLogic.add(
          new BoolSetting.Builder()
              .name("load-distance-notifier")
              .description("Notify you in chat when load-distance is updated.")
              .build());

  private final SettingGroup sgCache = settings.createGroup("Cache");
  public final Setting<Integer> cacheSize =
      sgCache.add(
          new IntSetting.Builder()
              .name("cache-size")
              .description("The maximum cache size.")
              .sliderRange(128, 3000000)
              .defaultValue(1024)
              .build());
  public final Setting<Boolean> cullByDistance =
      sgCache.add(
          new BoolSetting.Builder()
              .name("cull-by-distance")
              .description("Culls chunks by Chebyshev Distance from the player.")
              .defaultValue(false)
              .build());
  public final Setting<Boolean> autoAdjust =
      sgCache.add(
          new BoolSetting.Builder()
              .name("auto-adjust")
              .description("Automatically adjusts the distance based on load distance.")
              .defaultValue(false)
              .visible(cullByDistance::get)
              .build());
  public final Setting<Integer> cullDistance =
      sgCache.add(
          new IntSetting.Builder()
              .name("cull-distance")
              .description("The cull distance by chunk position.")
              .sliderRange(6, 128)
              .defaultValue(12)
              .visible(() -> !autoAdjust.get() && cullByDistance.get())
              .build());
  public final Setting<Boolean> clearOnLeave =
      sgCache.add(
          new BoolSetting.Builder()
              .name("clear-on-leave")
              .description("Clears caches when you leave the game.")
              .defaultValue(false)
              .build());
  public final Setting<Boolean> clearOnToggle =
      sgCache.add(
          new BoolSetting.Builder()
              .name("clear-on-toggle")
              .description("Clears caches when you toggle the module.")
              .defaultValue(true)
              .build());

  // Outline Rendering
  private final SettingGroup sgRender = settings.createGroup("Outlines Rendering");
  public final Setting<RenderMode> renderMode =
      sgRender.add(
          new EnumSetting.Builder<RenderMode>()
              .name("render-mode")
              .description("How to render the outlines.")
              .defaultValue(RenderMode.Static)
              .build());
  public final Setting<Double> bouncePeak =
      sgRender.add(
          new DoubleSetting.Builder()
              .name("bounce-height")
              .description("Max height to bounce to.")
              .sliderRange(1, 20)
              .visible(() -> renderMode.get() != RenderMode.Static)
              .defaultValue(3.5)
              .build());
  public final Setting<Double> bounceRate =
      sgRender.add(
          new DoubleSetting.Builder()
              .name("bounce-rate")
              .description("The rate to bounce per tick.")
              .sliderRange(0, 5)
              .decimalPlaces(8)
              .visible(() -> renderMode.get() != RenderMode.Static)
              .defaultValue(0.085)
              .build());
  public final Setting<SettingColor> checkColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("check")
              .defaultValue(new Color(187, 115, 236, 255))
              .build());
  public final Setting<SettingColor> checkStrokeColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("check-stroke")
              .visible(() -> renderMode.get() == RenderMode.Stroke)
              .defaultValue(new Color(187, 115, 236, 255))
              .build());
  public final Setting<Integer> checkYStart =
      sgRender.add(
          new IntSetting.Builder()
              .name("check-y")
              .description("Y Level to start rendering check-blocks the outline.")
              .sliderRange(-64, 320)
              .defaultValue(-58)
              .build());
  public final Setting<SettingColor> updateColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("update")
              .defaultValue(new Color(55, 112, 234, 255))
              .build());
  public final Setting<SettingColor> updateStrokeColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("update-stroke")
              .visible(() -> renderMode.get() == RenderMode.Stroke)
              .defaultValue(new Color(55, 112, 234, 255))
              .build());
  public final Setting<Integer> liquidYStart =
      sgRender.add(
          new IntSetting.Builder()
              .name("update-y")
              .description("Y Level to start rendering the block-updates outline.")
              .sliderRange(-64, 320)
              .defaultValue(-64)
              .build());
  public final Setting<SettingColor> preColor =
      sgRender.add(
          new ColorSetting.Builder().name("pre").defaultValue(new Color(255, 59, 59, 255)).build());
  public final Setting<SettingColor> preStrokeColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("pre-stroke")
              .visible(() -> renderMode.get() == RenderMode.Stroke)
              .defaultValue(new Color(255, 59, 59, 255))
              .build());
  public final Setting<Integer> preYStart =
      sgRender.add(
          new IntSetting.Builder()
              .name("pre-y")
              .description("Y Level to start rendering the pre-chunk outline.")
              .sliderRange(-64, 320)
              .defaultValue(-64)
              .build());

  public ChunkAnalyzer() {
    super(Categories.Render, "chunk-analyzer", "Returns a visual analysis of chunk data.");
  }

  @Override
  public void onDeactivate() {
    if (!clearOnToggle.get()) return;
    clear();
  }

  @EventHandler
  public void onLeave(GameLeftEvent event) {
    if (!clearOnLeave.get()) return;
    clear();
  }

  private void clear() {
    checkBlocksChunks.clear();
    blockUpdateChunks.clear();
    preChunks.clear();
  }

  @EventHandler
  public void onPacketReceived(PacketEvent.Receive event) {
    if (event.packet instanceof ChunkDataS2CPacket packet) {
      add(packet.getChunkX(), packet.getChunkZ(), ChunkType.PRE);
    }

    if (event.packet instanceof BlockEntityUpdateS2CPacket packet) {
      if (packet.getBlockEntityType() == BlockEntityType.END_GATEWAY) {
        if (cachedGateways.contains(packet.getPos())) return;
        cachedGateways.add(packet.getPos());
        if (Objects.requireNonNull(packet.getNbt()).contains("age")) {
          long age = packet.getNbt().getLong("age");
          if (age > 200) {
            ChunkPos chunkPos = new ChunkPos(packet.getPos());
            add(chunkPos.x, chunkPos.z, ChunkType.BLOCK_UPDATES);
            info("End gateway is " + age + " ticks old.");
          }
        }
      }
    }

    if (blockUpdates.get() && event.packet instanceof ChunkDeltaUpdateS2CPacket packet) {
      ObjectIntPair<ChunkPos> entry = ChunkUtils.getPacketUpdates(packet);
      if (entry.valueInt() >= blockUpdatesMin.get())
        add(entry.key().x, entry.key().z, ChunkType.BLOCK_UPDATES);
    }

    if (event.packet instanceof ChunkLoadDistanceS2CPacket packet) {
      this.loadDistance = packet.getDistance();
      if (notifyOnRD.get()) info("Render Distance was updated to: " + packet.getDistance());
    }
  }

  @EventHandler
  private void onChunkLoaded(ChunkLoadEvent event) {
    if (!checkBlocks.get()) return;

    if (ChunkUtils.containsBlocks(event.x, event.z, blockList.get())) {
      add(event.x, event.z, ChunkType.CHECK_BLOCKS);
    }
  }

  @EventHandler
  public void onRender3D(Render3DEvent event) {
    renderChunks(event.renderer);
  }

  @EventHandler
  public void onTick(TickEvent.Pre event) {
    double bounceTop = bouncePeak.get();

    switch (renderMode.get()) {
      case Static -> renderOffset = 0;

      case Breath, Stroke -> {
        if (renderOffset <= 0) {
          isGoingUp = true;
        } else if (renderOffset >= bounceTop) {
          isGoingUp = false;
        }

        renderOffset += isGoingUp ? bounceRate.get() : -bounceRate.get();
      }

      case Bounce -> {
        if (renderOffset <= -bounceTop) {
          isGoingUp = true;
        } else if (renderOffset >= bounceTop) {
          isGoingUp = false;
        }

        renderOffset += isGoingUp ? bounceRate.get() : -bounceRate.get();
      }
    }

    if (cullByDistance.get()) {
      assert mc.player != null;
      checkBlocksChunks.removeIf(
          pos -> pos.getChebyshevDistance(mc.player.getChunkPos()) >= getCullDistance());
      blockUpdateChunks.removeIf(
          pos -> pos.getChebyshevDistance(mc.player.getChunkPos()) >= getCullDistance());
      preChunks.removeIf(
          pos -> pos.getChebyshevDistance(mc.player.getChunkPos()) >= getCullDistance());
    }
  }

  public int getCullDistance() {
    if (autoAdjust.get()) return loadDistance * 4;
    return cullDistance.get();
  }

  public void renderChunks(Renderer3D renderer) {
    try {
      for (ChunkPos pos : checkBlocksChunks) {
        renderChunk(
            renderer,
            pos.getStartX(),
            checkYStart.get(),
            pos.getStartZ(),
            pos.getEndX(),
            checkYStart.get() + 2,
            pos.getEndZ(),
            ChunkType.CHECK_BLOCKS);
      }

      for (ChunkPos pos : blockUpdateChunks) {
        renderChunk(
            renderer,
            pos.getStartX(),
            liquidYStart.get(),
            pos.getStartZ(),
            pos.getEndX(),
            liquidYStart.get() + 2,
            pos.getEndZ(),
            ChunkType.BLOCK_UPDATES);
      }

      for (ChunkPos pos : preChunks) {
        if (!preRender.get()) break;
        renderChunk(
            renderer,
            pos.getStartX(),
            preYStart.get(),
            pos.getStartZ(),
            pos.getEndX(),
            preYStart.get() + 2,
            pos.getEndZ(),
            ChunkType.PRE);
      }
    } catch (Exception ignored) {
    }
  }

  public void add(int x, int z, ChunkType type) {
    try {
      ChunkPos pos = new ChunkPos(x, z);
      switch (type) {
        case PRE -> {
          if (preChunks.contains(pos)
              || checkBlocksChunks.contains(pos)
              || blockUpdateChunks.contains(pos)
              || entityIdChunks.contains(pos)) return;
          if (preChunks.size64() >= cacheSize.get()) preChunks.remove(0);
          preChunks.add(pos);
        }

        case CHECK_BLOCKS -> {
          preChunks.remove(pos);
          if (checkBlocksChunks.contains(pos)) return;
          if (checkBlocksChunks.size64() >= cacheSize.get()) checkBlocksChunks.remove(0);
          checkBlocksChunks.add(pos);
        }

        case BLOCK_UPDATES -> {
          preChunks.remove(pos);
          if (blockUpdateChunks.contains(pos)) return;
          if (blockUpdateChunks.size64() >= cacheSize.get()) blockUpdateChunks.remove(0);
          blockUpdateChunks.add(pos);
        }
      }
    } catch (Exception e) {
      LoliUtils.LOGGER.error("An exception was thrown while updating chunks.\n", e.getCause());
    }
  }

  public void renderChunk(
      Renderer3D renderer,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      ChunkType type) {
    Color color =
        switch (type) {
          case CHECK_BLOCKS -> checkColor.get();
          case BLOCK_UPDATES -> updateColor.get();
          case PRE -> preColor.get();
        };

    int origAlpha = color.a;

    switch (renderMode.get()) {
      case Static -> {
        renderer.boxLines(x1, y1, z1, x2, y2, z2, color, 0);
        color.a(60);
        renderer.boxSides(x1, y1, z1, x2, y2, z2, color, 0);
        color.a(origAlpha);
      }

      case Breath -> {
        renderer.boxLines(x1, y1, z1, x2, y2 + renderOffset, z2, color, 0);
        color.a(60);
        renderer.boxSides(x1, y1, z1, x2, y2 + renderOffset, z2, color, 0);
        color.a(origAlpha);
      }

      case Bounce -> {
        renderer.boxLines(x1, y1 + renderOffset, z1, x2, y2 + renderOffset, z2, color, 0);
        color.a(60);
        renderer.boxSides(x1, y1 + renderOffset, z1, x2, y2 + renderOffset, z2, color, 0);
        color.a(origAlpha);
      }

      case Stroke -> {
        renderer.boxLines(x1, y1, z1, x2, y2 + bouncePeak.get(), z2, color, 0);
        color.a(60);
        renderer.boxSides(x1, y1, z1, x2, y2 + bouncePeak.get(), z2, color, 0);
        color.a(140);
        renderer.boxLines(
            x1 - 0.15,
            y1 + renderOffset,
            z1 + 0.15,
            x2 + 0.15,
            y1 + renderOffset + 1,
            z2 + 0.15,
            switch (type) {
              case CHECK_BLOCKS -> checkStrokeColor.get();
              case BLOCK_UPDATES -> updateStrokeColor.get();
              case PRE -> preStrokeColor.get();
            },
            0);
        color.a(origAlpha);
      }
    }
  }

  public enum RenderMode {
    Bounce,
    Breath,
    Stroke,
    Static
  }

  public enum ChunkType {
    CHECK_BLOCKS,
    BLOCK_UPDATES,
    PRE
  }
}
