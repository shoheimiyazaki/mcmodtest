package com.example.helloworld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;

public class LightSnowballEntity extends Snowball {
    private BlockPos prevLightPos;

    public LightSnowballEntity(EntityType<? extends Snowball> type, Level world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        // クライアントでのみ動作
        if (level() instanceof ClientLevel client && this.isAlive()) {
            BlockPos p = this.blockPosition();
            if (prevLightPos == null || !prevLightPos.equals(p)) {
                if (prevLightPos != null) client.removeBlock(prevLightPos, true);

                level().setBlock(p, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);
                prevLightPos = p;
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ClientLevel client && prevLightPos != null) {
            client.removeBlock(prevLightPos,true);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (level() instanceof ClientLevel client && prevLightPos != null) {
            client.removeBlock(prevLightPos,true);
        }
        super.remove(reason);
    }
}
