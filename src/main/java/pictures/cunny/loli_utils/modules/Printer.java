package pictures.cunny.loli_utils.modules;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalNear;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import java.util.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;
import pictures.cunny.loli_utils.utility.*;

public class Printer extends Module {
  public static BlockPos anchoringTo;
  private final SettingGroup sgGeneral = settings.getDefaultGroup();
  private final Setting<Integer> swapDelay =
      sgGeneral.add(
          new IntSetting.Builder()
              .name("switch-delay")
              .description("How long to wait before placing after switching.")
              .defaultValue(2)
              .sliderRange(1, 10)
              .range(1, 10)
              .build());
  private final Setting<Integer> range =
      sgGeneral.add(
          new IntSetting.Builder()
              .name("place-range")
              .description("The range to place blocks.")
              .defaultValue(2)
              .sliderRange(1, 5)
              .range(1, 5)
              .build());
  private final Setting<Integer> delay =
      sgGeneral.add(
          new IntSetting.Builder()
              .name("printing-delay")
              .description("Delay between printing blocks in ticks.")
              .defaultValue(2)
              .sliderRange(0, 20)
              .range(0, 20)
              .build());
  private final Setting<Integer> blocksPerTick =
      sgGeneral.add(
          new IntSetting.Builder()
              .name("blocks/tick")
              .description("How many blocks place per tick.")
              .defaultValue(1)
              .sliderRange(1, 3)
              .range(1, 3)
              .build());
  private final Setting<SortAlgorithm> firstAlgorithm =
      sgGeneral.add(
          new EnumSetting.Builder<SortAlgorithm>()
              .name("first-sorting-mode")
              .description("The blocks you want to place first.")
              .defaultValue(SortAlgorithm.None)
              .build());
  private final Setting<SortingSecond> secondAlgorithm =
      sgGeneral.add(
          new EnumSetting.Builder<SortingSecond>()
              .name("second-sorting-mode")
              .description(
                  "Second pass of sorting eg. place first blocks higher and closest to you.")
              .defaultValue(SortingSecond.None)
              .visible(() -> firstAlgorithm.get().applySecondSorting)
              .build());

  // Color Swapping
  private final SettingGroup sgCS = settings.createGroup("Color Swapping");
  private final Setting<List<Block>> blockExclusion =
      sgCS.add(
          new BlockListSetting.Builder()
              .name("block-exclusion")
              .description("Excludes blocks.")
              .build());

  @SuppressWarnings("unused")
  private final Setting<List<String>> regexExclusion =
      sgCS.add(
          new StringListSetting.Builder()
              .name("regex-exclusion")
              .description("Excludes untranslated names via regex.")
              .defaultValue(List.of("_shulker_box"))
              .onChanged((list) -> exclusionRegexStr = "(" + String.join("|", list) + ")")
              .build());

  // Anchoring
  private final SettingGroup sgAnchor = settings.createGroup("Anchoring");
  private final Setting<Boolean> anchor =
      sgAnchor.add(
          new BoolSetting.Builder()
              .name("anchor")
              .description("Anchors player to placeable blocks.")
              .defaultValue(false)
              .build());
  private final Setting<Integer> yLevel =
      sgAnchor.add(
          new IntSetting.Builder()
              .name("y-level")
              .description("The Y level to scan")
              .defaultValue(64)
              .sliderRange(-64, 320)
              .range(-64, 320)
              .build());
  private final Setting<AnchorMovement> anchorMove =
      sgAnchor.add(
          new EnumSetting.Builder<AnchorMovement>()
              .name("anchor-movement")
              .description("How you move to destinations")
              .defaultValue(AnchorMovement.Vanilla)
              .build());
  private final Setting<Boolean> stuckFix =
      sgAnchor.add(
          new BoolSetting.Builder()
              .name("stuck-fix")
              .description("Attempts to fix becoming stuck or otherwise resolve it.")
              .defaultValue(true)
              .build());
  private final Setting<Integer> stuckMaxTime =
      sgAnchor.add(
          new IntSetting.Builder()
              .name("stuck-max-time")
              .description("Max amount of time to become stuck for in ticks.")
              .defaultValue(60)
              .sliderRange(5, 240)
              .visible(stuckFix::get)
              .build());
  private final Setting<AnchorSortAlgorithm> anchorAlgorithm =
      sgAnchor.add(
          new EnumSetting.Builder<AnchorSortAlgorithm>()
              .name("anchor-sorting-mode")
              .description("The blocks you want to place first.")
              .defaultValue(AnchorSortAlgorithm.ClosestToLastBlock)
              .build());
  private final Setting<Integer> anchorRange =
      sgAnchor.add(
          new IntSetting.Builder()
              .name("anchor-range")
              .description("The range to anchor to blocks.")
              .defaultValue(5)
              .sliderRange(5, 2056)
              .range(5, 2056)
              .build());
  private final Setting<Integer> anchorResetDelay =
      sgAnchor.add(
          new IntSetting.Builder()
              .name("anchor-reset-delay")
              .description("Delay between resetting the anchor.")
              .defaultValue(10)
              .sliderRange(5, 1200)
              .range(5, 1200)
              .build());

  // Auto Return
  private final SettingGroup sgAutoReturn = settings.createGroup("Auto Return");
  private final Setting<Boolean> autoReturn =
      sgAutoReturn.add(
          new BoolSetting.Builder()
              .name("auto-return")
              .description("Return to a set position once out of materials.")
              .defaultValue(true)
              .build());
  private final Setting<ReturnMovement> returnMove =
      sgAutoReturn.add(
          new EnumSetting.Builder<ReturnMovement>()
              .name("return-movement")
              .description("How you return to the 'home' position.")
              .defaultValue(ReturnMovement.Baritone)
              .build());
  private final Setting<Integer> takeOffPitch =
      sgAutoReturn.add(
          new IntSetting.Builder()
              .name("take-off-pitch")
              .description("Pitch for taking off.")
              .defaultValue(-9)
              .sliderRange(-30, 30)
              .visible(() -> returnMove.get() == ReturnMovement.Elytra)
              .build());
  private final Setting<Double> takeOffOffset =
      sgAutoReturn.add(
          new DoubleSetting.Builder()
              .name("take-off-offset")
              .description("Y value for offsetting while taking off.")
              .defaultValue(0.72)
              .sliderRange(0, 3)
              .visible(() -> returnMove.get() == ReturnMovement.Elytra)
              .build());
  private final Setting<Double> closeWingsDistance =
      sgAutoReturn.add(
          new DoubleSetting.Builder()
              .name("close-wings-distance")
              .description("The distance from return point to close wings")
              .defaultValue(0.5)
              .sliderRange(0, 5)
              .visible(() -> returnMove.get() == ReturnMovement.Elytra)
              .build());
  private final Setting<Double> horizontalSpeed =
      sgAutoReturn.add(
          new DoubleSetting.Builder()
              .name("horizontal-speed")
              .description("The horizontal speed.")
              .sliderRange(0.1, 10.0)
              .defaultValue(1.4)
              .visible(() -> returnMove.get() == ReturnMovement.Elytra)
              .build());
  private final Setting<Double> verticalSpeed =
      sgAutoReturn.add(
          new DoubleSetting.Builder()
              .name("vertical-speed")
              .description("The vertical speed.")
              .sliderRange(0.1, 10.0)
              .defaultValue(0.4)
              .visible(() -> returnMove.get() == ReturnMovement.Elytra)
              .build());
  private final Setting<Boolean> stayAbove =
      sgAutoReturn.add(
          new BoolSetting.Builder()
              .name("stay-above")
              .description("Attempts to stay over a defined height.")
              .defaultValue(true)
              .visible(() -> returnMove.get() == ReturnMovement.Elytra)
              .build());
  private final Setting<Double> stayAboveDistance =
      sgAutoReturn.add(
          new DoubleSetting.Builder()
              .name("stay-above-distance")
              .description("Stay above a certain distance over the blocks")
              .sliderRange(0, 3)
              .defaultValue(0.7)
              .visible(() -> returnMove.get() == ReturnMovement.Elytra)
              .build());
  private final Setting<BlockPos> returnPos =
      sgAutoReturn.add(
          new BlockPosSetting.Builder()
              .name("return-pos")
              .description("The return 'home' position.")
              .defaultValue(BlockPos.ORIGIN)
              .build());

  private final SettingGroup sgRendering = settings.createGroup("Rendering");
  public final Setting<Double> contraction =
      sgRendering.add(
          new DoubleSetting.Builder()
              .name("contraction")
              .description("The rate of contraction.")
              .sliderRange(0, 5)
              .decimalPlaces(4)
              .defaultValue(0.085)
              .build());
  public final Setting<Double> strokeOffset =
      sgRendering.add(
          new DoubleSetting.Builder()
              .name("stroke-offset")
              .description("The offset for stroke lines.")
              .sliderRange(0, 0.15)
              .decimalPlaces(4)
              .defaultValue(0.085)
              .build());
  private final Setting<RenderMode> renderModePlacing =
      sgRendering.add(
          new EnumSetting.Builder<RenderMode>()
              .name("placing-render")
              .description("The mode for rendering placed blocks.")
              .defaultValue(RenderMode.Static)
              .build());
  public final Setting<SettingColor> placingColor =
      sgRendering.add(
          new ColorSetting.Builder()
              .name("placing-color")
              .defaultValue(new Color(255, 59, 59, 255))
              .build());
  public final Setting<SettingColor> placingStrokeColor =
      sgRendering.add(
          new ColorSetting.Builder()
              .name("placing-stroke")
              .defaultValue(new Color(255, 59, 59, 255))
              .build());
  private final Setting<RenderMode> renderModeDestination =
      sgRendering.add(
          new EnumSetting.Builder<RenderMode>()
              .name("destination-render")
              .description("The mode for rendering placed blocks.")
              .defaultValue(RenderMode.Static)
              .build());
  public final Setting<SettingColor> destinationColor =
      sgRendering.add(
          new ColorSetting.Builder()
              .name("destination-color")
              .defaultValue(new Color(255, 59, 59, 255))
              .build());
  public final Setting<SettingColor> destinationStrokeColor =
      sgRendering.add(
          new ColorSetting.Builder()
              .name("destination-stroke")
              .defaultValue(new Color(255, 59, 59, 255))
              .build());
  private final Setting<Integer> fadeTime =
      sgRendering.add(
          new IntSetting.Builder()
              .name("fade-time")
              .description("Time for the rendering to fade, in ticks.")
              .defaultValue(3)
              .range(1, 1000)
              .sliderRange(1, 20)
              .build());
  private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
  private final List<BlockPos> toSort = new ArrayList<>(), anchorToSort = new ArrayList<>();
  private final List<MapColor> containedColors = new ArrayList<>();
  private int lastSwap = 0;
  private String exclusionRegexStr = "";
  // Render
  private final List<Pair<RenderWrap, BlockPos>> placeFading = new ArrayList<>();
  private boolean pauseTillRefilled = false;
  private int timer;
  private int anchorResetTimer;
  private int lastJump = 0;
  private BlockPos returnTo;
  private BlockPos currentStuckPos = BlockPos.ORIGIN;
  private int stuckTime = 0;
  private double renderOffset = 0;
  private boolean isGoingUp = true;

  public Printer() {
    super(Categories.World, "printer", "Automatically prints blocks in a schematic.");

    MapColorCache.init();
  }

  @Override
  public void onActivate() {
    onDeactivate();
  }

  @Override
  public void onDeactivate() {
    placeFading.clear();
    anchorToSort.clear();
    toSort.clear();
    pauseTillRefilled = false;
    if (baritone.getPathingBehavior().hasPath()) baritone.getPathingBehavior().cancelEverything();
    mc.options.forwardKey.setPressed(false);
  }

  @EventHandler
  private void onTick(TickEvent.Post event) {
    if (mc.player == null || mc.world == null) {
      placeFading.clear();
      return;
    }

    renderingTick();

    WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

    if (worldSchematic == null) {
      placeFading.clear();
      toggle();
      return;
    }

    containedColors.clear();

    for (ItemStack stack : mc.player.getInventory().main) {
      if (InventoryUtils.IS_BLOCK.test(stack)
          && !stack.getItem().getTranslationKey().matches(exclusionRegexStr)
          && blockExclusion.get().stream()
              .noneMatch((block -> stack.getItem() == block.asItem()))) {
        containedColors.add(MapColorCache.getColor(stack));
      }
    }

    if (pauseTillRefilled && autoReturn.get()) {
      placeFading.clear();
      if (mc.player.getInventory().getEmptySlot() == -1) {
        returnMovement(returnTo);
        if (MathUtils.xzDistanceBetween(returnTo, mc.player.getBlockPos()) <= 0.35) {
          if (mc.player.isFallFlying()) {
            mc.player.stopFallFlying();
          }

          pauseTillRefilled = false;

          mc.options.forwardKey.setPressed(false);
        }
      } else {
        returnMovement(returnPos.get());
        if (MathUtils.xzDistanceBetween(returnPos.get(), mc.player.getBlockPos()) <= 0.35) {
          if (mc.player.isFallFlying()) {
            mc.player.stopFallFlying();
          }

          mc.options.forwardKey.setPressed(false);
        }
      }
      return;
    }

    if (anchor.get()) {

      if (stuckFix.get()) {
        if (currentStuckPos.equals(mc.player.getBlockPos())) {
          stuckTime++;

          if (stuckTime >= stuckMaxTime.get()) {
            List<BlockPos> autoStuckFix = new ArrayList<>();
            BlockIterator.register(
                16,
                2,
                (pos, blockState) -> {
                  if (!blockState.isAir()) {
                    autoStuckFix.add(pos);
                  }
                });

            BlockIterator.after(
                () -> {
                  autoStuckFix.sort(SortAlgorithm.Furthest.algorithm);

                  anchoringTo = autoStuckFix.get(0);
                });
          }
        } else {
          stuckTime = 0;
          currentStuckPos = mc.player.getBlockPos();
        }
      }

      if (anchoringTo != null && BlockUtils.hasEntitiesInside(anchoringTo)) {
        anchoringTo = null;
      }

      if ((anchoringTo != null && BlockUtils.isNotAir(anchoringTo))
          || anchorResetTimer >= anchorResetDelay.get()) {
        anchoringTo = null;
        anchorResetTimer = 0;
        switch (anchorMove.get()) {
          case Vanilla -> mc.options.forwardKey.setPressed(false);
          case Baritone -> {
            if (baritone.getPathingBehavior().hasPath())
              baritone.getPathingBehavior().cancelEverything();
          }
        }
      } else if (anchoringTo == null || anchoringTo.isWithinDistance(mc.player.getBlockPos(), 2)) {
        switch (anchorMove.get()) {
          case Vanilla -> mc.options.forwardKey.setPressed(false);
          case Baritone -> {
            if (baritone.getPathingBehavior().hasPath())
              baritone.getPathingBehavior().cancelEverything();
          }
        }
      } else if (anchoringTo != null) {
        switch (anchorMove.get()) {
          case Vanilla -> {
            mc.player.setYaw((float) Rotations.getYaw(anchoringTo));
            mc.options.forwardKey.setPressed(true);
          }
          case Baritone -> {
            if (!baritone.getPathingBehavior().hasPath()) {
              baritoneTo(anchoringTo, 2);
            }
          }
        }
      }

      if (anchoringTo == null) {
        EntityUtils.customY = yLevel.get();
        BlockIteratorR.register(
            anchorRange.get(),
            1,
            (pos, blockState) -> {
              BlockState required = worldSchematic.getBlockState(pos);

              // && !pos.isWithinDistance(mc.player.getBlockPos(), 1)

              if (blockState.isAir() && !required.isAir() && !BlockUtils.hasEntitiesInside(pos)) {
                if (containedColors.contains(
                    MapColorCache.getColor(required.getBlock().asItem()))) {
                  anchorToSort.add(pos.toImmutable());
                }
              }
            });

        BlockIteratorR.after(
            () -> {
              anchorToSort.sort(anchorAlgorithm.get().secondaryAlgorithm.algorithm);
              anchorToSort.sort(anchorAlgorithm.get().algorithm);

              if (!anchorToSort.isEmpty()) {
                anchoringTo = anchorToSort.get(0);
              } else if (!pauseTillRefilled) {
                info("No block to anchor to, going to return position.");
                returnTo = mc.player.getBlockPos();
                pauseTillRefilled = true;
              }
              EntityUtils.resetY();
            });
        anchorToSort.clear();
      }

      anchorResetTimer++;
    }

    placeFading.forEach(
        s -> {
          if (renderModePlacing.get() == RenderMode.Breath) {
            s.getLeft().breath(s.getLeft().breath() - 1);
          } else {
            s.getLeft().fadeTime(s.getLeft().fadeTime() - 1);
          }
        });
    placeFading.removeIf(
        s -> s.getLeft().fadeTime() <= 0 || s.getLeft().breath() * contraction.get() <= -1);

    toSort.clear();

    if (timer >= delay.get()) {
      if (mc.player.isUsingItem()) return;

      BlockIterator.register(
          range.get() + 1,
          3,
          (pos, blockState) -> {
            BlockState required = worldSchematic.getBlockState(pos);

            if (mc.player.getBlockPos().isWithinDistance(pos, range.get())
                && blockState.isReplaceable()
                && !required.isAir()
                && blockState.getBlock() != required.getBlock()
                && DataManager.getRenderLayerRange().isPositionWithinRange(pos)
                && !mc.player
                    .getBoundingBox()
                    .intersects(Vec3d.of(pos), Vec3d.of(pos).add(1, 1, 1))) {

              if (containedColors.contains(MapColorCache.getColor(required.getBlock().asItem()))) {
                toSort.add(new BlockPos(pos));
              }
            }
          });

      BlockIterator.after(
          () -> {
            if (firstAlgorithm.get() != SortAlgorithm.None) {
              if (firstAlgorithm.get().applySecondSorting) {
                if (secondAlgorithm.get() != SortingSecond.None) {
                  toSort.sort(secondAlgorithm.get().algorithm);
                }
              }
              toSort.sort(firstAlgorithm.get().algorithm);
            }

            var ref =
                new Object() {
                  int placed = 0;
                };

            for (BlockPos pos : toSort) {
              if (timer < delay.get() || ref.placed >= blocksPerTick.get()) break;

              if (!mc.player.getBlockPos().isWithinDistance(pos, range.get())) continue;

              BlockState state = worldSchematic.getBlockState(pos);
              Item item = state.getBlock().asItem();

              FindItemResult itemResult =
                  InvUtils.find(
                      (stack) ->
                          MapColorCache.getColor(stack) == MapColorCache.getColor(item)
                              && !stack.getItem().getTranslationKey().matches(exclusionRegexStr)
                              && blockExclusion.get().stream()
                                  .noneMatch((block -> stack.getItem() == block.asItem())));
              if (!itemResult.found()) continue;

              Hand hand = Hand.MAIN_HAND;

              if (itemResult.isOffhand()) hand = Hand.OFF_HAND;

              if (lastSwap > 0) {
                lastSwap--;
                break;
              }

              if (MapColorCache.getColor(mc.player.getInventory().getMainHandStack())
                      != MapColorCache.getColor(item)
                  && hand != Hand.OFF_HAND) {
                lastSwap = swapDelay.get();
                if (itemResult.isHotbar()) {
                  InventoryUtils.swapSlot(itemResult.slot(), false);
                } else {
                  InventoryUtils.swapSlot(InventoryUtils.findEmptySlotInHotbar(7), false);
                  Objects.requireNonNull(mc.getNetworkHandler())
                      .sendPacket(new PickFromInventoryC2SPacket(itemResult.slot()));
                }

                break;
              }

              if (BlockUtils.canPlace(pos)) {
                if (BlockUtils.placeBlock(hand, itemResult, pos)) {
                  if (placeFading.stream().noneMatch((pair) -> pair.getRight().equals(pos)))
                    placeFading.add(
                        new Pair<>(new RenderWrap(fadeTime.get(), 0), new BlockPos(pos)));
                  timer = 0;
                  ref.placed++;
                }
              }
            }
          });

    } else timer++;
  }

  @EventHandler
  private void onRender(Render3DEvent event) {
    placeFading.forEach(
        s ->
            renderBlock(
                event.renderer,
                s.getRight().getX(),
                s.getRight().getY(),
                s.getRight().getZ(),
                s.getRight().getX() + 1,
                s.getRight().getY() + 1,
                s.getRight().getZ() + 1,
                RenderType.Placing,
                renderModePlacing.get(),
                s.getLeft().breath()));

    if (anchoringTo != null) {
      renderBlock(
          event.renderer,
          anchoringTo.getX(),
          anchoringTo.getY(),
          anchoringTo.getZ(),
          anchoringTo.getX() + 1,
          anchoringTo.getY() + 1,
          anchoringTo.getZ() + 1,
          RenderType.Destination,
          renderModeDestination.get(),
          0);
    }
  }

  public void returnMovement(BlockPos pos) {
    assert mc.player != null : "Player is null";
    assert mc.getNetworkHandler() != null : "Network Handler is null";

    switch (returnMove.get()) {
      case Baritone -> {
        if (!baritone.getPathingBehavior().hasPath()) {
          baritoneTo(pos, 0);
        }
      }

      case Vanilla -> {
        mc.player.setYaw((float) Rotations.getYaw(pos));
        mc.options.forwardKey.setPressed(true);
      }

      case Elytra -> {
        if (lastJump >= 20) {
          lastJump = 0;
        }

        if (MathUtils.xzDistanceBetween(mc.player.getPos(), pos) <= closeWingsDistance.get()) {
          if (mc.player.isFallFlying()) {
            mc.getNetworkHandler()
                .sendPacket(
                    new ClientCommandC2SPacket(
                        mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            mc.player.stopFallFlying();
          } else {
            mc.player.setYaw((float) Rotations.getYaw(pos));
            mc.options.forwardKey.setPressed(true);
          }
          return;
        }

        if (!mc.player.isFallFlying()) {
          mc.player.setPitch(takeOffPitch.get());

          if (lastJump == 0) {
            mc.player.jump();
          }

          if (lastJump == 6) {
            mc.player.setJumping(false);
            mc.player.setSprinting(true);
            mc.getNetworkHandler()
                .sendPacket(
                    new ClientCommandC2SPacket(
                        mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            mc.player.setPos(
                mc.player.getX(), mc.player.getY() + takeOffOffset.get(), mc.player.getZ());
          }
        } else {
          anchorTo(pos);
        }

        lastJump++;
      }
    }
  }

  public void anchorTo(BlockPos pos) {
    assert mc.player != null : "Player is null";
    assert mc.world != null : "World is null";

    double dx = pos.getX() - mc.player.getBlockPos().getX();
    double dz = pos.getZ() - mc.player.getBlockPos().getZ();

    if (stayAbove.get()) {
      double blockHeight = returnTo.getY() + stayAboveDistance.get();

      if (mc.player.getY() < blockHeight) {
        mc.player.setVelocity(0, verticalSpeed.get(), 0);
        return;
      }
    }

    // normalize the direction vector
    double length = Math.sqrt(dx * dx + dz * dz);
    dx /= length;
    dz /= length;

    // set the velocity of the entity to move towards the target position
    mc.player.setVelocity(dx * horizontalSpeed.get(), 0, dz * horizontalSpeed.get());
  }

  private void baritoneTo(BlockPos pos, int radius) {
    if (baritone.getPathingBehavior().hasPath()) baritone.getPathingBehavior().cancelEverything();
    baritone.getCustomGoalProcess().setGoalAndPath(new GoalNear(pos, radius));
  }

  public void renderingTick() {
    if (renderModePlacing.get() != RenderMode.Static
        || renderModeDestination.get() != RenderMode.Static) {
      if (renderOffset <= 0) {
        isGoingUp = true;
      } else if (renderOffset >= 1) {
        isGoingUp = false;
      }

      renderOffset += isGoingUp ? contraction.get() : -contraction.get();
    } else {
      renderOffset = 0;
    }
  }

  public void renderBlock(
      Renderer3D renderer,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      RenderType type,
      RenderMode mode,
      int breath) {
    Color color =
        switch (type) {
          case Placing -> placingColor.get();
          case Destination -> destinationColor.get();
        };

    int origAlpha = color.a;

    switch (mode) {
      case Static -> {
        renderer.boxLines(x1, y1, z1, x2, y2, z2, color, 0);
        color.a(60);
        renderer.boxSides(x1, y1, z1, x2, y2, z2, color, 0);
        color.a(origAlpha);
      }

      case Breath -> {
        renderer.boxLines(x1, y1, z1, x2, y2 + (breath * contraction.get()), z2, color, 0);
        color.a(60);
        renderer.boxSides(x1, y1, z1, x2, y2 + (breath * contraction.get()), z2, color, 0);
        color.a(origAlpha);
      }

      case Stroke -> {
        renderer.boxLines(x1, y1, z1, x2, y2, z2, color, 0);
        color.a(60);
        renderer.boxSides(x1, y1, z1, x2, y2, z2, color, 0);
        color.a(140);
        renderer.boxLines(
            x1 - strokeOffset.get(),
            y1 + renderOffset,
            z1 - strokeOffset.get(),
            x2 + strokeOffset.get(),
            y1 + renderOffset,
            z2 + strokeOffset.get(),
            switch (type) {
              case Placing -> placingStrokeColor.get();
              case Destination -> destinationStrokeColor.get();
            },
            0);
        renderer.boxLines(
            x1 - strokeOffset.get(),
            y1 + Math.min(Math.max(0, renderOffset / 1.7), 1),
            z1 - strokeOffset.get(),
            x2 + strokeOffset.get(),
            y1 + renderOffset,
            z2 + strokeOffset.get(),
            switch (type) {
              case Placing -> placingStrokeColor.get();
              case Destination -> destinationStrokeColor.get();
            },
            0);
        color.a(origAlpha);
      }
    }
  }

  public enum AnchorMovement {
    Baritone,
    Vanilla
  }

  public enum ReturnMovement {
    Baritone,
    Elytra,
    Vanilla
  }

  @SuppressWarnings("unused")
  public enum AnchorSortAlgorithm {
    RSNorth(
        SortAlgorithm.Closest,
        Comparator.comparingDouble(
            value -> {
              ChunkPos chunk = new ChunkPos(value);
              assert MeteorClient.mc.player != null;
              float altDist = (chunk.x) - MeteorClient.mc.player.getChunkPos().x;
              return chunk.z + ((double) MathHelper.sqrt(altDist * altDist) / 64);
            })),
    RSEast(
        SortAlgorithm.Closest,
        Comparator.comparingDouble(
            value -> {
              ChunkPos chunk = new ChunkPos(value);
              assert MeteorClient.mc.player != null;
              float altDist = (chunk.z) - MeteorClient.mc.player.getChunkPos().z;
              return chunk.x * -1 + ((double) MathHelper.sqrt(altDist * altDist) / 64);
            })),
    RSSouth(
        SortAlgorithm.Closest,
        Comparator.comparingDouble(
            value -> {
              ChunkPos chunk = new ChunkPos(value);
              assert MeteorClient.mc.player != null;
              float altDist = (chunk.x) - MeteorClient.mc.player.getChunkPos().x;
              return chunk.z * -1 + ((double) MathHelper.sqrt(altDist * altDist) / 64);
            })),
    RSWest(
        SortAlgorithm.Closest,
        Comparator.comparingDouble(
            value -> {
              ChunkPos chunk = new ChunkPos(value);
              assert MeteorClient.mc.player != null;
              float altDist = (chunk.z) - MeteorClient.mc.player.getChunkPos().z;
              return chunk.x + ((double) MathHelper.sqrt(altDist * altDist) / 64);
            })),
    ClosestToLastBlock(
        SortAlgorithm.None,
        Comparator.comparingDouble(
            value ->
                MeteorClient.mc.player != null
                    ? Utils.squaredDistance(
                        anchoringTo != null ? anchoringTo.getX() : MeteorClient.mc.player.getX(),
                        anchoringTo != null ? anchoringTo.getY() : MeteorClient.mc.player.getY(),
                        anchoringTo != null ? anchoringTo.getZ() : MeteorClient.mc.player.getZ(),
                        value.getX() + 0.5,
                        value.getY() + 0.5,
                        value.getZ() + 0.5)
                    : 0));

    final SortAlgorithm secondaryAlgorithm;
    final Comparator<BlockPos> algorithm;

    AnchorSortAlgorithm(SortAlgorithm secondaryAlgorithm, Comparator<BlockPos> algorithm) {
      this.secondaryAlgorithm = secondaryAlgorithm;
      this.algorithm = algorithm;
    }
  }

  @SuppressWarnings("unused")
  public enum SortAlgorithm {
    None(false, (a, b) -> 0),
    TopDown(true, Comparator.comparingInt(value -> value.getY() * -1)),
    DownTop(true, Comparator.comparingInt(Vec3i::getY)),
    Closest(
        false,
        Comparator.comparingDouble(
            value ->
                MeteorClient.mc.player != null
                    ? Utils.squaredDistance(
                        MeteorClient.mc.player.getX(),
                        MeteorClient.mc.player.getY(),
                        MeteorClient.mc.player.getZ(),
                        value.getX() + 0.5,
                        value.getY() + 0.5,
                        value.getZ() + 0.5)
                    : 0)),
    ClosestToLastBlock(
        false,
        Comparator.comparingDouble(
            value ->
                MeteorClient.mc.player != null
                    ? Utils.squaredDistance(
                        anchoringTo != null ? anchoringTo.getX() : MeteorClient.mc.player.getX(),
                        anchoringTo != null ? anchoringTo.getY() : MeteorClient.mc.player.getY(),
                        anchoringTo != null ? anchoringTo.getZ() : MeteorClient.mc.player.getZ(),
                        value.getX() + 0.5,
                        value.getY() + 0.5,
                        value.getZ() + 0.5)
                    : 0)),
    Furthest(
        false,
        Comparator.comparingDouble(
            value ->
                MeteorClient.mc.player != null
                    ? (Utils.squaredDistance(
                            MeteorClient.mc.player.getX(),
                            MeteorClient.mc.player.getY(),
                            MeteorClient.mc.player.getZ(),
                            value.getX() + 0.5,
                            value.getY() + 0.5,
                            value.getZ() + 0.5))
                        * -1
                    : 0));

    final boolean applySecondSorting;
    final Comparator<BlockPos> algorithm;

    SortAlgorithm(boolean applySecondSorting, Comparator<BlockPos> algorithm) {
      this.applySecondSorting = applySecondSorting;
      this.algorithm = algorithm;
    }
  }

  public enum RenderType {
    Placing,
    Destination
  }

  public enum RenderMode {
    Breath,
    Stroke,
    Static
  }

  @SuppressWarnings("unused")
  public enum SortingSecond {
    None(SortAlgorithm.None.algorithm),
    Nearest(SortAlgorithm.Closest.algorithm),
    Furthest(SortAlgorithm.Furthest.algorithm);

    final Comparator<BlockPos> algorithm;

    SortingSecond(Comparator<BlockPos> algorithm) {
      this.algorithm = algorithm;
    }
  }
}
