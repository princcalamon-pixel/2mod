
package com.suveren.feudal.item;

import com.suveren.feudal.util.CrownHelper;
import com.suveren.feudal.world.FeudalSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;

/** Listina: desni klik na skrinjo jo doloci za zitnico ali orozarno. */
public class DeedItem extends Item {
    private final boolean armory;

    public DeedItem(Properties props, boolean armory) {
        super(props);
        this.armory = armory;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!(level.getBlockState(ctx.getClickedPos()).getBlock() instanceof ChestBlock)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!CrownHelper.isCrowned(player)) {
            player.sendSystemMessage(Component.literal("§cBrez krone listine ne veljajo!"));
            return InteractionResult.CONSUME;
        }
        FeudalSavedData d = FeudalSavedData.get((net.minecraft.server.level.ServerLevel) level);
        if (armory) {
            d.addArmory(player.getUUID(), ctx.getClickedPos());
            player.sendSystemMessage(Component.literal("§8Skrinja dolocena za §7OROZARNO§8. Vitezi si bodo tu jemali mece."));
        } else {
            d.addGranary(player.getUUID(), ctx.getClickedPos());
            player.sendSystemMessage(Component.literal("§eSkrinja dolocena za §6ZITNICO§e. Podlozniki bodo tu jedli in oddajali davek."));
        }
        player.getItemInHand(ctx.getHand()).shrink(1);
        return InteractionResult.CONSUME;
    }
}
