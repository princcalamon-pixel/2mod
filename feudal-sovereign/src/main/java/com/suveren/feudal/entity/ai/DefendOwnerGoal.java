
package com.suveren.feudal.entity.ai;

import com.suveren.feudal.entity.FeudalKnightEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

/** Vitez napade sovraznika, ki napada njegovega kralja. */
public class DefendOwnerGoal extends TargetGoal {
    private final FeudalKnightEntity knight;
    private LivingEntity target;

    public DefendOwnerGoal(FeudalKnightEntity knight) {
        super(knight, false, false);
        this.knight = knight;
    }

    @Override
    public boolean canUse() {
        this.target = null;
        Player owner = knight.getOwner();
        if (owner == null) return false;
        AABB box = knight.getBoundingBox().inflate(24);
        List<Entity> list = knight.level().getEntities(knight, box,
                e -> e instanceof Mob m && m.getTarget() == owner);
        if (list.isEmpty()) return false;
        list.sort(Comparator.comparingDouble(knight::distanceToSqr));
        this.target = (LivingEntity) list.get(0);
        return true;
    }

    @Override
    public void start() {
        knight.setTarget(target);
        super.start();
    }
}
