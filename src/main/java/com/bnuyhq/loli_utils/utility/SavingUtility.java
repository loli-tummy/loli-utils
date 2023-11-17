package com.bnuyhq.loli_utils.utility;

import com.bnuyhq.loli_utils.LoliUtils;
import com.mojang.serialization.Codec;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.SharedConstants;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.ZipCompressor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ReadableContainer;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.world.ChunkSerializer.*;

public class SavingUtility {
    private static final Path savingPath = Path.of(MeteorClient.FOLDER.getPath()).resolve("chunk_saves");
    private static final Path backupsPath = Path.of(MeteorClient.FOLDER.getPath()).resolve("_BACKUPS");
    public static final Map<String, VersionedChunkStorage> VERSIONED_CHUNK_STORAGE_HASH_MAP = new HashMap<>();
    private static final NbtCompound EMPTY_COMPOUND = new NbtCompound();

    public static void makeDefaults(String subdir) {
        if (mc.player == null || mc.player.getWorld() == null || VERSIONED_CHUNK_STORAGE_HASH_MAP.containsKey(getCurrentKey(subdir)) || mc.getCurrentServerEntry() == null) {
            return;
        }

        Path path = savingPath.resolve(getCurrentKey(subdir));
        Path path2 = backupsPath.resolve(mc.getCurrentServerEntry().address);

        try {
            Files.createDirectories(path);
            Files.createDirectories(path2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        VERSIONED_CHUNK_STORAGE_HASH_MAP.put(getCurrentKey(subdir), new VersionedChunkStorage(path, mc.getDataFixer(), true));
    }

    public static String getCurrentKey(String subdir) {
        assert mc.player != null;
        return Objects.requireNonNull(mc.getCurrentServerEntry()).address + "/" + subdir + "/" + mc.player.getWorld().getRegistryKey().getValue();
    }

    public static void save() {
        if (mc.isInSingleplayer()) {
            return;
        }

        for (Map.Entry<String, VersionedChunkStorage> entry : SavingUtility.VERSIONED_CHUNK_STORAGE_HASH_MAP.entrySet()) {
            long timeNS = System.nanoTime();
            entry.getValue().completeAll();
            ((StorageIoWorker) entry.getValue().getWorker()).writeResult();

            LoliUtils.LOG.info("<Chunk Saver> Took {}ns to save {}", System.nanoTime() - timeNS, entry.getKey());
        }
    }

    public static void backup() {
        if (mc.isInSingleplayer()) {
            return;
        }

        ZipCompressor zipCompressor = new ZipCompressor(backupsPath.resolve(Objects.requireNonNull(mc.getCurrentServerEntry()).address).resolve(System.currentTimeMillis() + ".zip"));

        zipCompressor.copyAll(savingPath.resolve(mc.getCurrentServerEntry().address));
        zipCompressor.close();
    }

    public static synchronized NbtCompound serialize(World world, Chunk chunk, ChunkPos chunkPos, boolean specialData) {
        NbtCompound nbtCompound3;
        NbtCompound nbtCompound = new NbtCompound();
        NbtCompound specialDataNbt = new NbtCompound();
        if (specialData) {
            for (ChunkSection section : chunk.getSectionArray()) {
                if (!specialDataNbt.getBoolean("HasPortal"))
                    specialDataNbt.putBoolean("HasPortal", section.hasAny(blockState -> blockState.getBlock() == Blocks.NETHER_PORTAL));
                if (!specialDataNbt.getBoolean("HasObsidian"))
                    specialDataNbt.putBoolean("HasObsidian", section.hasAny(blockState -> blockState.getBlock() == Blocks.OBSIDIAN));
            }
        }
        nbtCompound.putInt("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
        nbtCompound.putInt(X_POS_KEY, chunkPos.x);
        nbtCompound.putInt("yPos", chunk.getBottomSectionCoord());
        nbtCompound.putInt(Z_POS_KEY, chunkPos.z);
        nbtCompound.putLong("LastUpdate", world.getTime());
        nbtCompound.putLong("InhabitedTime", 0);
        nbtCompound.putString("Status", "full");

        ChunkSection[] chunkSections = chunk.getSectionArray();
        NbtList nbtList = new NbtList();
        Registry<Biome> registry = world.getRegistryManager().get(RegistryKeys.BIOME);
        Codec<ReadableContainer<RegistryEntry<Biome>>> codec = ChunkSerializer.createCodec(registry);
        boolean bl = chunk.isLightOn();

        for (int i = world.getBottomSectionCoord() - 1; i < (world.getBottomSectionCoord() - 1) + (world.countVerticalSections() + 2); ++i) {
            int j = chunk.sectionCoordToIndex(i);
            boolean bl2 = j >= 0 && j < chunkSections.length;
            NbtCompound nbtCompound2 = new NbtCompound();
            if (bl2) {
                ChunkSection chunkSection = chunkSections[j];
                nbtCompound2.put("block_states", CODEC.encodeStart(NbtOps.INSTANCE, chunkSection.getBlockStateContainer()).getOrThrow(false, LoliUtils.LOG::info));
                nbtCompound2.put("biomes", codec.encodeStart(NbtOps.INSTANCE, chunkSection.getBiomeContainer()).getOrThrow(false, LoliUtils.LOG::info));
            }
            if (nbtCompound2.isEmpty()) continue;
            nbtCompound2.putByte("Y", (byte) i);
            nbtList.add(nbtCompound2);
        }
        nbtCompound.put(SECTIONS_KEY, nbtList);
        if (bl) {
            nbtCompound.putBoolean(IS_LIGHT_ON_KEY, true);
        }
        NbtList nbtList2 = new NbtList();
        NbtList nbtList3 = new NbtList();
        NbtCompound nbtCompound5 = new NbtCompound();
        List<BlockEntityType<?>> blockEntityTypes = new ArrayList<>();
        for (BlockPos blockPos : chunk.getBlockEntityPositions()) {
            nbtCompound3 = chunk.getPackedBlockEntityNbt(blockPos);
            if (nbtCompound3 == null) continue;
            nbtList2.add(nbtCompound3);
            if (specialData) {
                BlockEntityType<?> entityType = Objects.requireNonNull(chunk.getBlockEntity(blockPos)).getType();
                if (!blockEntityTypes.contains(entityType)) {
                    blockEntityTypes.add(entityType);
                    nbtList3.add(NbtString.of(Objects.requireNonNull(BlockEntityType.getId(entityType)).toShortTranslationKey()));
                }

                if (entityType == BlockEntityType.SIGN) {
                    List<String> content = new ArrayList<>();

                    SignBlockEntity entity = (SignBlockEntity) chunk.getBlockEntity(blockPos);

                    if (!entity.getFrontText().hasText(mc.player)) {
                        for (Text text : entity.getFrontText().getMessages(false)) {
                            content.add(text.getString());
                        }
                    }

                    if (!entity.getBackText().hasText(mc.player)) {
                        for (Text text : entity.getBackText().getMessages(false)) {
                            content.add(text.getString());
                        }
                    }

                    nbtCompound5.putString(blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ(), String.join("[<EOL>]", content));
                }
            }
        }
        nbtCompound.put("block_entities", nbtList2);

        nbtCompound.put("PostProcessing", ChunkSerializer.toNbt(chunk.getPostProcessingLists()));
        NbtCompound nbtCompound4 = new NbtCompound();
        for (Map.Entry<Heightmap.Type, Heightmap> entry : chunk.getHeightmaps()) {
            if (!chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) continue;
            nbtCompound4.put(entry.getKey().getName(), new NbtLongArray(entry.getValue().asLongArray()));
        }
        nbtCompound.put(HEIGHTMAPS_KEY, nbtCompound4);

        nbtCompound.put("structures", EMPTY_COMPOUND);

        if (specialData) {
            specialDataNbt.put("SignText", nbtCompound5);
            specialDataNbt.put("BlockEntities", nbtList3);
            nbtCompound.put("SpecialData", specialDataNbt);
        }

        return nbtCompound;
    }
}
