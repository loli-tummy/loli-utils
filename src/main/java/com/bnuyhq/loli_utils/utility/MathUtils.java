package com.bnuyhq.loli_utils.utility;

import net.minecraft.util.math.MathHelper;

public class MathUtils {
  public static double toMapQuad(double v) {
    int j = MathHelper.floor((v + 64.0) / (double) 128);
    int l = j * 128 + 128 / 2 - 64;
    return (double) l / 128;
  }
}
