package com.bnuyhq.loli_utils.events;

public class ChunkLoadEvent {
  public int x, z;
  public static final ChunkLoadEvent INSTANCE = new ChunkLoadEvent();

  public ChunkLoadEvent get(int x, int z) {
    this.x = x;
    this.z = z;
    return this;
  }
}
