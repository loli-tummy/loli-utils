package pictures.cunny.loli_utils.utility;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MathUtils {
  public static double toMapQuad(double v) {
    int j = MathHelper.floor((v + 64.0) / (double) 128);
    int l = j * 128 + 128 / 2 - 64;
    return (double) l / 128;
  }

  public static double xzDistanceBetween(Vec3d s, Vec3d e) {
    var dx = s.x - e.x;
    var dz = s.z - e.z;
    return Math.sqrt(dx * dx + dz * dz);
  }

  public static double xzDistanceBetween(Vec3d s, BlockPos e) {
    var dx = s.x - e.getX();
    var dz = s.z - e.getZ();
    return Math.sqrt(dx * dx + dz * dz);
  }

  public static double xzDistanceBetween(BlockPos s, BlockPos e) {
    var dx = s.getX() - e.getX();
    var dz = s.getZ() - e.getZ();
    return Math.sqrt(dx * dx + dz * dz);
  }
}
