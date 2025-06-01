package com.example.helloworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.LivingEntity;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 「モブの足元ではなく、ひとつ上の空気ブロックに LIGHT を置く」ように修正した例
 */
@Mod.EventBusSubscriber(modid = HelloWorldMod.MODID)
public class MobLightHandler {
    // モブ → そのモブにくっつけた最後のライト座標
    private static final Map<LivingEntity, BlockPos> mobToLightPos = new HashMap<>();

    public static void registerMobAsLight(LivingEntity mob) {
        if (mobToLightPos.containsKey(mob)) return;
        // グローイング効果を付与
        mob.setItemInHand(InteractionHand.OFF_HAND,  new ItemStack(Items.TORCH) );
        mobToLightPos.put(mob, null);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        // サーバー側かつティックの最後（END）で処理
        if (event.level.isClientSide)
        {
            Level level = event.level;
            Iterator<Map.Entry<LivingEntity, BlockPos>> iter = mobToLightPos.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<LivingEntity, BlockPos> entry = iter.next();
                LivingEntity mob = entry.getKey();

                if (mobToLightPos.containsKey(mob)) {
                    level.addParticle(
                            ParticleTypes.GLOW,
                            mob.getX(),
                            mob.getY() + mob.getBbHeight() * 0.5,
                            mob.getZ(),
                            0.0, 0.05, 0.0
                    );
                }
            }
            return;
        }

        if (event.phase != TickEvent.Phase.END)
            return;

        Level level = event.level;
        Iterator<Map.Entry<LivingEntity, BlockPos>> iter = mobToLightPos.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<LivingEntity, BlockPos> entry = iter.next();
            LivingEntity mob = entry.getKey();


            if (!mob.isAlive()) {
                BlockPos oldPos = entry.getValue();
                if (oldPos != null && level.getBlockState(oldPos).is(Blocks.LIGHT)) {
                    level.setBlock(oldPos, Blocks.AIR.defaultBlockState(), 3);
                }
                iter.remove();
                continue;
            }

            // モブの「足元の一段上」を座標とする
            BlockPos footPos = mob.blockPosition();      // 例: (x, y, z) where y is floor
            BlockPos lightPos = footPos.above();         // (x, y+1, z) → 空気になっていることが多い

            // 前回置いていたライト座標を回収
            BlockPos prevPos = entry.getValue();
            if (prevPos != null && !prevPos.equals(lightPos)) {
                if (level.getBlockState(prevPos).is(Blocks.LIGHT)) {
                    level.setBlock(prevPos, Blocks.AIR.defaultBlockState(), 3);
                }
            }

            // もし lightPos が空気（Air）ならそこに発光ブロックを置く
            if (level.getBlockState(lightPos).isAir()) {
                level.setBlock(lightPos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);
                entry.setValue(lightPos);
            } else {
                // 空気ではない場合は次回に備えて lightPos を null にしておく
                entry.setValue(null);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        Level level = (Level) event.getLevel();
        mobToLightPos.entrySet().removeIf(entry -> {
            if (entry.getKey().level() == level) {
                BlockPos pos = entry.getValue();
                if (pos != null && level.getBlockState(pos).is(Blocks.LIGHT)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
                return true;
            }
            return false;
        });
    }
}
