
package com.suveren.feudal.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FeudalSavedData extends SavedData {
    private static final String NAME = "suveren_data";

    public static FeudalSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(FeudalSavedData::load, FeudalSavedData::new, NAME);
    }

    private final Map<UUID, Set<Long>> claims = new HashMap<>();
    private final Map<Long, UUID> chunkOwner = new HashMap<>();
    private final Map<UUID, List<BlockPos>> granaries = new HashMap<>();
    private final Map<UUID, List<BlockPos>> armories = new HashMap<>();
    public int banditCount = 0;

    public void claim(UUID owner, long chunk) {
        claims.computeIfAbsent(owner, k -> new HashSet<>()).add(chunk);
        chunkOwner.put(chunk, owner);
        setDirty();
    }

    public void unclaim(long chunk) {
        UUID o = chunkOwner.remove(chunk);
        if (o != null) {
            Set<Long> s = claims.get(o);
            if (s != null) s.remove(chunk);
        }
        setDirty();
    }

    public boolean isClaimed(long chunk) { return chunkOwner.containsKey(chunk); }

    @Nullable
    public UUID getChunkOwner(long chunk) { return chunkOwner.get(chunk); }

    public List<BlockPos> getGranaries(UUID o) { return granaries.computeIfAbsent(o, k -> new ArrayList<>()); }
    public List<BlockPos> getArmories(UUID o) { return armories.computeIfAbsent(o, k -> new ArrayList<>()); }

    public void addGranary(UUID o, BlockPos p) {
        List<BlockPos> l = getGranaries(o);
        if (!l.contains(p)) { l.add(p.immutable()); setDirty(); }
    }
    public void addArmory(UUID o, BlockPos p) {
        List<BlockPos> l = getArmories(o);
        if (!l.contains(p)) { l.add(p.immutable()); setDirty(); }
    }
    public void removeGranary(UUID o, BlockPos p) { getGranaries(o).remove(p); setDirty(); }
    public void removeArmory(UUID o, BlockPos p) { getArmories(o).remove(p); setDirty(); }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag owners = new ListTag();
        for (Map.Entry<UUID, Set<Long>> e : claims.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("Owner", e.getKey());
            long[] arr = e.getValue().stream().mapToLong(Long::longValue).toArray();
            t.putLongArray("Chunks", arr);
            owners.add(t);
        }
        tag.put("Claims", owners);

        ListTag stocks = new ListTag();
        for (UUID o : granaries.keySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("Owner", o);
            t.putLongArray("Granaries", granaries.get(o).stream().mapToLong(BlockPos::asLong).toArray());
            t.putLongArray("Armories", armories.getOrDefault(o, new ArrayList<>()).stream().mapToLong(BlockPos::asLong).toArray());
            stocks.add(t);
        }
        tag.put("Stocks", stocks);
        tag.putInt("Bandits", banditCount);
        return tag;
    }

    public static FeudalSavedData load(CompoundTag tag) {
        FeudalSavedData d = new FeudalSavedData();
        ListTag owners = tag.getList("Claims", Tag.TAG_COMPOUND);
        for (int i = 0; i < owners.size(); i++) {
            CompoundTag t = owners.getCompound(i);
            UUID o = t.getUUID("Owner");
            Set<Long> set = new HashSet<>();
            for (long c : t.getLongArray("Chunks")) { set.add(c); d.chunkOwner.put(c, o); }
            d.claims.put(o, set);
        }
        ListTag stocks = tag.getList("Stocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < stocks.size(); i++) {
            CompoundTag t = stocks.getCompound(i);
            UUID o = t.getUUID("Owner");
            List<BlockPos> g = new ArrayList<>();
            for (long l : t.getLongArray("Granaries")) g.add(BlockPos.of(l));
            d.granaries.put(o, g);
            List<BlockPos> a = new ArrayList<>();
            for (long l : t.getLongArray("Armories")) a.add(BlockPos.of(l));
            d.armories.put(o, a);
        }
        d.banditCount = tag.getInt("Bandits");
        return d;
    }
}
