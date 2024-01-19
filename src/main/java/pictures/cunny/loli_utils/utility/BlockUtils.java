package pictures.cunny.loli_utils.utility;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BlockUtils {

  public static Direction[] getDirections() {
    return Direction.values();
  }

  public static boolean isReplaceable(BlockPos pos) {
    return mc.player != null && mc.player.getWorld().getBlockState(pos).isAir()
        || mc.player.getWorld().getBlockState(pos).isReplaceable();
  }

  public static boolean isNotAir(BlockPos pos) {
    return mc.player == null || !mc.player.getWorld().getBlockState(pos).isAir();
  }

  public static boolean canPlace(BlockPos pos) {
    assert mc.player != null;

    List<Entity> entities =
        mc.player
            .getWorld()
            .getOtherEntities(
                null,
                new Box(pos.add(3, 3, 3), pos.add(-3, -3, -3)),
                entity -> {
                  if (EntityUtils.canPlaceIn(entity)) {
                    return false;
                  }

                  return entity.collidesWithStateAtPos(pos, Blocks.BEDROCK.getDefaultState());
                });

    return isReplaceable(pos) && entities.isEmpty();
  }

  public static boolean hasEntitiesInside(BlockPos pos) {
    assert mc.player != null;
    List<Entity> entities =
        mc.player
            .getWorld()
            .getOtherEntities(
                null,
                new Box(pos.add(3, 3, 3), pos.add(-3, -3, -3)),
                entity -> {
                  if (EntityUtils.canPlaceIn(entity)) {
                    return false;
                  }

                  return entity.collidesWithStateAtPos(pos, Blocks.BEDROCK.getDefaultState());
                });
    return !entities.isEmpty();
  }

  public static Direction getPlaceDirection(BlockPos pos) {
    for (Direction direction : getDirections()) {
      if (isNotAir(pos.offset(direction))) return direction;
    }
    return Direction.UP;
  }

  public static boolean shouldAirPlace(BlockPos pos) {
    for (Direction direction : getDirections()) {
      if (!BlockUtils.isReplaceable(pos.offset(direction))) return false;
    }
    return true;
  }

  public static Map.Entry<Float, Float> getRotation(boolean raytrace, BlockPos pos) {
    assert mc.player != null;

    if (raytrace) {
      Optional<Rotation> rotation = RotationUtils.reachable(mc.player, pos, 4.5);

      if (rotation.isPresent()) {
        if (canRaycast(pos, rotation.get().getPitch(), rotation.get().getYaw())) {
          return Map.entry(rotation.get().getYaw(), rotation.get().getPitch());
        }
      }

      for (Direction direction : Direction.values()) {
        Vec3d vec3d =
            new Vec3d(
                (double) pos.getX() + ((double) direction.getOpposite().getOffsetX() * 0.5),
                (double) pos.getY() + ((double) direction.getOpposite().getOffsetY() * 0.5),
                (double) pos.getZ() + ((double) direction.getOpposite().getOffsetZ() * 0.5));
        double yaw = Rotations.getYaw(vec3d), pitch = Rotations.getPitch(vec3d);

        rotation = RotationUtils.reachable(mc.player, pos.offset(direction), 4.5);

        if (rotation.isPresent()) {
          if (canRaycast(pos, rotation.get().getPitch(), rotation.get().getYaw())) {
            return Map.entry(rotation.get().getYaw(), rotation.get().getPitch());
          }
        }

        if (canRaycast(pos, pitch, yaw)) {
          return Map.entry((float) yaw, (float) pitch);
        } else {
          yaw = Rotations.getYaw(pos.offset(direction));
          pitch = Rotations.getPitch(pos.offset(direction));
          if (canRaycast(pos, pitch, yaw)) {
            return Map.entry((float) yaw, (float) pitch);
          } else {
            vec3d =
                new Vec3d(
                    (double) pos.getX() + ((double) direction.getOffsetX() * 0.5),
                    (double) pos.getY() + ((double) direction.getOffsetY() * 0.5),
                    (double) pos.getZ() + ((double) direction.getOffsetZ() * 0.5));
            yaw = Rotations.getYaw(vec3d);
            pitch = Rotations.getPitch(vec3d);
            if (canRaycast(pos, pitch, yaw)) {
              return Map.entry((float) yaw, (float) pitch);
            } else {
              BlockPos pos1 = pos.offset(direction);
              vec3d =
                  new Vec3d(
                      (double) pos1.getX() + ((double) direction.getOffsetX() * 0.5),
                      (double) pos1.getY() + ((double) direction.getOffsetY() * 0.5),
                      (double) pos1.getZ() + ((double) direction.getOffsetZ() * 0.5));
              yaw = Rotations.getYaw(vec3d);
              pitch = Rotations.getPitch(vec3d);
              if (canRaycast(pos, pitch, yaw)) {
                return Map.entry((float) yaw, (float) pitch);
              } else {
                vec3d =
                    new Vec3d(
                        (double) pos1.getX()
                            + ((double) direction.getOpposite().getOffsetX() * 0.5),
                        (double) pos1.getY()
                            + ((double) direction.getOpposite().getOffsetY() * 0.5),
                        (double) pos1.getZ()
                            + ((double) direction.getOpposite().getOffsetZ() * 0.5));
                yaw = Rotations.getYaw(vec3d);
                pitch = Rotations.getPitch(vec3d);
                if (canRaycast(pos, pitch, yaw)) {
                  return Map.entry((float) yaw, (float) pitch);
                }
              }
            }
          }
        }
      }

      if (canRaycast(pos, Rotations.getYaw(pos), Rotations.getPitch(pos))) {
        return Map.entry((float) Rotations.getYaw(pos), (float) Rotations.getPitch(pos));
      }

      return Map.entry(
          (float) Rotations.getYaw(clickOffset(pos)), (float) Rotations.getPitch(clickOffset(pos)));
    }
    return Map.entry(
        (float) Rotations.getYaw(clickOffset(pos)), (float) Rotations.getPitch(clickOffset(pos)));
  }

  public static boolean canRaycast(BlockPos pos, double pitch, double yaw) {
    assert mc.player != null;

    FakePlayerEntity fakePlayerEntity = new FakePlayerEntity(mc.player, "", 0, false);
    fakePlayerEntity.setPitch((float) pitch);
    fakePlayerEntity.setYaw((float) yaw);
    fakePlayerEntity.setHeadYaw((float) yaw);
    fakePlayerEntity.setBodyYaw((float) yaw);
    fakePlayerEntity.calculateDimensions();

    HitResult pHitResult = fakePlayerEntity.raycast(4, 1.0f, false);

    if (pHitResult.getType() != HitResult.Type.BLOCK) {
      return false;
    }

    BlockHitResult hitResult = (BlockHitResult) pHitResult;

    return hitResult.getBlockPos().offset(hitResult.getSide()).equals(pos)
        || hitResult.getBlockPos().equals(pos);
  }

  public static Vec3d clickOffset(BlockPos pos) {
    return new Vec3d(
        (double) pos.getX() + ((double) getPlaceDirection(pos).getOffsetX() * 0.5),
        (double) pos.getY() + ((double) getPlaceDirection(pos).getOffsetY() * 0.5),
        (double) pos.getZ() + ((double) getPlaceDirection(pos).getOffsetZ() * 0.5));
    // return new Vec3d(pos.getX() + Math.min(0.9f, Math.random()), pos.getY() + Math.min(0.9f,
    // Math.random()), pos.getZ() + Math.min(0.9f, Math.random()));
  }

  public static BlockHitResult getBlockHitResult(
      boolean raytrace, BlockPos pos, Direction direction) {
    if (raytrace) {
      assert mc.player != null;

      FakePlayerEntity fakePlayerEntity = new FakePlayerEntity(mc.player, "", 0, false);
      Map.Entry<Float, Float> rot = getRotation(true, pos);
      fakePlayerEntity.setPitch(rot.getValue());
      fakePlayerEntity.setYaw(rot.getKey());
      fakePlayerEntity.setHeadYaw(rot.getKey());
      fakePlayerEntity.setBodyYaw(rot.getKey());
      fakePlayerEntity.calculateDimensions();

      return (BlockHitResult) fakePlayerEntity.raycast(4.5, 1.0f, false);
    }
    return new BlockHitResult(
        clickOffset(pos),
        direction == null ? getPlaceDirection(pos).getOpposite() : direction,
        pos.offset(direction == null ? getPlaceDirection(pos) : direction.getOpposite()),
        false);
  }

  public static boolean placeBlock(Hand hand, FindItemResult itemResult, BlockPos pos) {
    assert mc.player != null : "Player has not joined the game.";
    assert mc.interactionManager != null : "Interaction Manager is not defined.";
    assert mc.getNetworkHandler() != null : "Network Handler is not defined.";

    Direction dir = getPlaceDirection(pos);

    boolean isCarpet =
        mc.player
            .getInventory()
            .getStack(itemResult.slot())
            .getItem()
            .getTranslationKey()
            .endsWith("carpet");

    if (isCarpet) {
      dir = Direction.UP;
      Map.Entry<Float, Float> rot = getRotation(false, pos);

      if (BlockUtils.isReplaceable(pos.offset(Direction.DOWN))) return false;

      if (!mc.player.handSwinging) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));

      mc.getNetworkHandler()
          .sendPacket(
              new PlayerMoveC2SPacket.LookAndOnGround(
                  rot.getKey(), rot.getValue(), mc.player.isOnGround()));
      mc.interactionManager.interactBlock(mc.player, hand, getBlockHitResult(false, pos, dir));
      return true;
    }

    Map.Entry<Float, Float> rot = getRotation(true, pos);

    if (canRaycast(pos, rot.getValue(), rot.getKey())) {
      if (!mc.player.handSwinging) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
      mc.getNetworkHandler()
          .sendPacket(
              new PlayerMoveC2SPacket.LookAndOnGround(
                  rot.getKey(), rot.getValue(), mc.player.isOnGround()));
      // mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(hand,
      // getBlockHitResult(true, pos, dir), 0));
      mc.interactionManager.interactBlock(mc.player, hand, getBlockHitResult(true, pos, dir));
      return true;
    }

    return false;
  }
}
