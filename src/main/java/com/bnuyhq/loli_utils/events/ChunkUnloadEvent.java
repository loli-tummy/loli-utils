package com.bnuyhq.loli_utils.events;

public class ChunkUnloadEvent {
    public int x, z;
    public static final ChunkUnloadEvent INSTANCE = new ChunkUnloadEvent();

    public ChunkUnloadEvent get(int x, int z) {
        this.x = x;
        this.z = z;
        return this;
    }
}
