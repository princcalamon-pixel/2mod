
package com.suveren.feudal.entity;

import com.suveren.feudal.util.CrownHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/** Skupni osnovni razred za podloznikse (vitez, kmet): lastnistvo, lakota, zvestoba. */
public abstract class SubjectEntity extends PathfinderMob {
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(SubjectEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected int food = 5000;
    protected int starveTimer = 0;
    protected int greetCooldown = 0;

    protected SubjectEntity(net.minecraft.world.entity.EntityType<? extends PathfinderMob> type,
                            net.minecraft.world.level.Level level) {
        super(type, level);
        this.setPersistenceRequired(); // zvesti za vedno
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER, Optional.empty());
    }

    public Optional<UUID> getOwnerUUID() { return this.entityData.get(DATA_OWNER); }

    public void setOwnerUUID(UUID id) { this.entityData.set(DATA_OWNER, Optional.of(id)); }

    @Nullable
    public Player getOwner() {
        if (!(this.level() instanceof ServerLevel sl)) return null;
        return getOwnerUUID().map(u -> sl.getServer().getPlayerList().getPlayer(u)).orElse(null);
    }

    public boolean isOwnedBy(Player p) {
        return getOwnerUUID().map(u -> u.equals(p.getUUID())).orElse(false);
    }

    /** Poslusajo te SAMO, ce imas krono na glavi. */
    public boolean obeys(Player p) { return isOwnedBy(p) && CrownHelper.isCrowned(p); }

    public int getFood() { return food; }
    public void addFood(int f) { food = Math.min(6000, food + f); }

    /** Gibanje z logiko konja: ce je cilj dalec, zajame konja v blizini. */
    public void travelMove(double x, double y, double z, double speed, double arriveDist) {
        double dxz = Math.sqrt(this.distanceToSqr(x, this.getY(), z));
        if (this.getVehicle() instanceof net.minecraft.world.entity.animal.horse.Horse horse) {
            if (dxz > 6) {
                if (this.tickCount % 20 == 0) horse.getNavigation().moveTo(x, y, z, Math.max(speed, 1.25));
                return;
            }
            this.stopRiding();
        }
        if (dxz <= arriveDist) {
            this.getNavigation().stop();
            return;
        }
        if (dxz > 24) tryMountHorse();
        this.getNavigation().moveTo(x, y, z, speed);
    }

    private void tryMountHorse() {
        if (this.isPassenger()) return;
        var horses = this.level().getEntitiesOfClass(
                net.minecraft.world.entity.animal.horse.Horse.class,
                this.getBoundingBox().inflate(12),
                h -> h.isAlive() && h.isAdult() && !h.isVehicle());
        if (!horses.isEmpty()) this.startRiding(horses.get(0));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (--food < 0) food = 0;
        if (food == 0) {
            if (++starveTimer >= 60) {
                starveTimer = 0;
                this.hurt(this.damageSources().starve(), 1.0f);
            }
        } else {
            starveTimer = 0;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        getOwnerUUID().ifPresent(u -> t.putUUID("Owner", u));
        t.putInt("Food", food);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        if (t.hasUUID("Owner")) setOwnerUUID(t.getUUID("Owner"));
        food = t.getInt("Food");
    }
}
