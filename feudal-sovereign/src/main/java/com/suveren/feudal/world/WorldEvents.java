
package com.suveren.feudal.world;

import com.suveren.feudal.FeudalMod;
import com.suveren.feudal.block.HeraldicBannerBlock;
import com.suveren.feudal.entity.BanditEntity;
import com.suveren.feudal.entity.ModEntities;
import com.suveren.feudal.util.CrownHelper;
import com.suveren.feudal.util.KnightNames;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ServerLifecycleHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = FeudalMod.MODID)
public class WorldEvents {
    private static int tickCounter = 0;
    private static final int BANDIT_CAP = 30;

    /** Grbovna zastava = zahtevaev chunk ozemlja (samo s krono). */
    @SubscribeEvent
    public static void onBannerPlaced(BlockEvent.EntityPlaceEvent e) {
        if (!(e.getState().getBlock() instanceof HeraldicBannerBlock)) return;
        if (!(e.getEntity() instanceof Player p)) return;
        if (p.level().isClientSide || !(p.level() instanceof ServerLevel sl)) return;

        if (!CrownHelper.isCrowned(p)) {
            p.sendSystemMessage(Component.literal("§cPotrebujes krono, da oznacis ozemlje!"));
            e.setCanceled(true);
            return;
        }
        FeudalSavedData d = FeudalSavedData.get(sl);
        long chunk = ChunkPos.asLong(e.getPos().getX() >> 4, e.getPos().getZ() >> 4);
        UUID prev = d.getChunkOwner(chunk);
        if (prev != null && !prev.equals(p.getUUID())) {
            p.sendSystemMessage(Component.literal("§cTa zemlja ze pripada drugemu gospodarju!"));
            e.setCanceled(true);
            return;
        }
        d.claim(p.getUUID(), chunk);
        p.sendSystemMessage(Component.literal("§6Kraljestvo razsirjeno! §7(chunk: "
                + e.getPos().getX() / 16 + ", " + e.getPos().getZ() / 16 + ")"));
    }

    @SubscribeEvent
    public static void onBannerBroken(BlockEvent.BreakEvent e) {
        if (!(e.getState().getBlock() instanceof HeraldicBannerBlock)) return;
        ServerLevel sl = (ServerLevel) e.getLevel();
        FeudalSavedData d = FeudalSavedData.get(sl);
        long chunk = ChunkPos.asLong(e.getPos().getX() >> 4, e.getPos().getZ() >> 4);
        UUID owner = d.getChunkOwner(chunk);
        if (owner != null) {
            if (!owner.equals(e.getPlayer().getUUID())) {
                e.getPlayer().sendSystemMessage(Component.literal("§cNe mores uniciti tujega grba!"));
                e.setCanceled(true);
                return;
            }
            d.unclaim(chunk);
            e.getPlayer().sendSystemMessage(Component.literal("§7Ozemlje izgubljeno."));
        }
    }

    /** Razbojniki: nocni napadi v neoznacenih dezelah. */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (++tickCounter % 400 != 0) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        for (ServerLevel sl : server.getAllLevels()) {
            for (Player p : sl.players()) {
                if (p.isSpectator()) continue;
                FeudalSavedData d = FeudalSavedData.get(sl);
                if (d.banditCount >= BANDIT_CAP) continue;
                long day = sl.getDayTime() % 24000;
                if (day < 13000 || day > 23000) continue; // samo ponoci
                if (d.isClaimed(p.chunkPosition().toLong())) continue; // v svoji dezeli si varen
                if (sl.random.nextFloat() >= 0.35f) continue;

                int n = 2 + sl.random.nextInt(3);
                for (int i = 0; i < n; i++) {
                    double angle = sl.random.nextDouble() * Math.PI * 2;
                    double r = 26 + sl.random.nextDouble() * 8;
                    int x = p.getBlockX() + (int) (Math.cos(angle) * r);
                    int z = p.getBlockZ() + (int) (Math.sin(angle) * r);
                    if (d.isClaimed(ChunkPos.asLong(x >> 4, z >> 4))) continue;
                    int y = sl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BanditEntity b = ModEntities.BANDIT.get().create(sl);
                    if (b == null) continue;
                    b.moveTo(x + 0.5, y, z + 0.5, sl.random.nextFloat() * 360, 0);
                    if (i == 0) {
                        b.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
                        b.setCustomName(Component.literal(KnightNames.bandit(sl.random)));
                    }
                    sl.addFreshEntity(b);
                    d.banditCount++;
                }
                if (n > 0) {
                    d.setDirty();
                    p.sendSystemMessage(Component.literal("§4Opozorilo: §cRazbojniki napadajo iz teme!"));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getEntity() instanceof BanditEntity && e.getEntity().level() instanceof ServerLevel sl) {
            FeudalSavedData d = FeudalSavedData.get(sl);
            if (d.banditCount > 0) d.banditCount--;
            d.setDirty();
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent e) {
        if (e.getEntity() instanceof BanditEntity && e.getEntity().level().random.nextFloat() < 0.3f) {
            e.getDrops().add(new ItemEntity(e.getEntity().level(),
                    e.getEntity().getX(), e.getEntity().getY(), e.getEntity().getZ(),
                    new ItemStack(Items.GOLD_NUGGET, 1 + e.getEntity().level().random.nextInt(2))));
        }
    }
}
