
package com.suveren.feudal.util;

import com.suveren.feudal.world.FeudalSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class StockpileHelper {

    @Nullable
    public static BlockPos findGranary(ServerLevel sl, UUID owner, BlockPos near, double maxDist) {
        return find(sl, owner, near, maxDist, false);
    }

    @Nullable
    public static BlockPos findArmory(ServerLevel sl, UUID owner, BlockPos near, double maxDist) {
        return find(sl, owner, near, maxDist, true);
    }

    @Nullable
    private static BlockPos find(ServerLevel sl, UUID owner, BlockPos near, double maxDist, boolean armory) {
        if (owner == null) return null;
        FeudalSavedData d = FeudalSavedData.get(sl);
        List<BlockPos> list = armory ? d.getArmories(owner) : d.getGranaries(owner);
        BlockPos best = null;
        double bestDist = maxDist * maxDist;
        Iterator<BlockPos> it = list.iterator();
        while (it.hasNext()) {
            BlockPos p = it.next();
            if (!(sl.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity)) {
                it.remove();
                d.setDirty();
                continue;
            }
            double dist = p.distSqr(near);
            if (dist < bestDist) { bestDist = dist; best = p; }
        }
        return best;
    }

    @Nullable
    public static Container getContainer(ServerLevel sl, BlockPos pos) {
        BlockState state = sl.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chest) {
            return ChestBlock.getContainer(chest, state, sl, pos, true);
        }
        BlockEntity be = sl.getBlockEntity(pos);
        return be instanceof Container c ? c : null;
    }

    /** Vzame hrano iz skrinje; vrne kalorije. */
    public static int eat(Container c) {
        for (int i = 0; i < c.getContainerSize(); i++) {
            ItemStack st = c.getItem(i);
            if (st.is(Items.BREAD)) {
                st.shrink(1);
                c.setChanged();
                return 1400;
            }
        }
        for (int i = 0; i < c.getContainerSize(); i++) {
            ItemStack st = c.getItem(i);
            if (st.is(Items.WHEAT)) {
                st.shrink(1);
                c.setChanged();
                return 700;
            }
        }
        return 0;
    }

    /** Vzame najboljsi mozni mec iz skrinje (diamant > zelezo > kamen > zlato > les). */
    @Nullable
    public static ItemStack takeBestSword(ServerLevel sl, Container c) {
        int bestSlot = -1;
        int bestRank = -1;
        for (int i = 0; i < c.getContainerSize(); i++) {
            ItemStack st = c.getItem(i);
            if (!(st.getItem() instanceof SwordItem)) continue;
            int rank;
            if (st.is(Items.DIAMOND_SWORD)) rank = 5;
            else if (st.is(Items.IRON_SWORD)) rank = 4;
            else if (st.is(Items.STONE_SWORD)) rank = 3;
            else if (st.is(Items.GOLDEN_SWORD)) rank = 2;
            else rank = 1;
            if (rank > bestRank) { bestRank = rank; bestSlot = i; }
        }
        if (bestSlot < 0) return null;
        ItemStack sword = c.getItem(bestSlot).copy();
        c.setItem(bestSlot, ItemStack.EMPTY);
        c.setChanged();
        return sword;
    }
}
