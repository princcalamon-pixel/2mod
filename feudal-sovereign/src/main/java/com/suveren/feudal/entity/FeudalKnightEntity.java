
package com.suveren.feudal.entity;

import com.suveren.feudal.entity.ai.DefendOwnerGoal;
import com.suveren.feudal.entity.ai.KnightGoals;
import com.suveren.feudal.util.CrownHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class FeudalKnightEntity extends SubjectEntity {
    private static final EntityDataAccessor<Integer> DATA_STANCE =
            SynchedEntityData.defineId(FeudalKnightEntity.class, EntityDataSerializers.INT);

    public enum Stance { FOLLOW, GUARD, HOLD, PATROL }

    private BlockPos guardPos;

    public FeudalKnightEntity(EntityType<? extends FeudalKnightEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SubjectEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STANCE, 0);
    }

    public Stance getStance() {
        return Stance.values()[Mth.clamp(this.entityData.get(DATA_STANCE), 0, Stance.values().length - 1)];
    }

    public void setStance(Stance s) { this.entityData.set(DATA_STANCE, s.ordinal()); }

    public void cycleStance() {
        setStance(Stance.values()[(getStance().ordinal() + 1) % Stance.values().length]);
    }

    public BlockPos getGuardPos() { return guardPos; }
    public void setGuardPos(BlockPos p) { this.guardPos = p.immutable(); }

    public static String stanceLabel(Stance s) {
        return switch (s) {
            case FOLLOW -> "sledi";
            case GUARD -> "straza";
            case HOLD -> "ostani";
            case PATROL -> "patrolja";
        };
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, true));
        this.goalSelector.addGoal(3, KnightGoals.openGate(this));
        this.goalSelector.addGoal(4, KnightGoals.stockpile(this));
        this.goalSelector.addGoal(5, KnightGoals.stance(this));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new DefendOwnerGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        // Stej s konja v boju.
        if (this.isPassenger() && this.getTarget() != null && this.distanceTo(this.getTarget()) < 7) {
            this.stopRiding();
        }
        // Pozdrav kralju s krono.
        if (greetCooldown > 0) greetCooldown--;
        else if (this.tickCount % 40 == 0) {
            Player o = getOwner();
            if (o != null && CrownHelper.isCrowned(o) && this.distanceTo(o) < 4) {
                o.sendSystemMessage(Component.literal("§7" + getName().getString()
                        + ": »Vasa volja, moj kralj.«"));
                greetCooldown = 2400;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putInt("Stance", getStance().ordinal());
        if (guardPos != null) t.putLong("Guard", guardPos.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        setStance(Stance.values()[Mth.clamp(t.getInt("Stance"), 0, Stance.values().length - 1)]);
        if (t.contains("Guard")) guardPos = BlockPos.of(t.getLong("Guard"));
    }
}
