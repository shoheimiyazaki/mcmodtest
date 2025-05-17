package com.example.helloworld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = HelloWorldMod.MODID,
        bus   = Mod.EventBusSubscriber.Bus.FORGE  // Forgeバスでリスン
)
public class ForgeEventHandlers {

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // ① 投擲物が SnowballEntity (=雪玉挙動) かチェック
        if (!(event.getEntity() instanceof Snowball snowball)) return;

        // ② 投擲元アイテムが HELLO_GEM かチェック
        if (!snowball.getItem().getItem().equals(ItemInit.HELLO_GEM.get())) return;

        // ③ 着弾がブロックヒットなら、その向き先の座標にトーチを設置
        if (event.getRayTraceResult() instanceof BlockHitResult hit) {
            BlockPos targetPos = hit.getBlockPos().relative(hit.getDirection());
            // 環境によっては置けない場所もあるので、置けるかも確認してもOK
            snowball.level().setBlockAndUpdate(targetPos, Blocks.TORCH.defaultBlockState());
        }
    }
}
