package com.example.helloworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Blocks;

public class LightSnowballEntity extends Snowball {
    private BlockPos prevLightPos;

    public LightSnowballEntity(EntityType<? extends Snowball> type, Level world) {
        super(type, world);
    }

    /*
    // ❷ シューターから直接生まれるとき用のコンストラクタを追加
    public LightSnowballEntity(EntityType<? extends Snowball> type, LivingEntity shooter, Level world) {
        super(type, world);

        // 1) オーナーをセット
        this.setOwner(shooter);

        // 2) 初期位置をプレイヤーの目線あたりに揃える
        //    （Snowball(Level, LivingEntity) が内部でやっているのと同じロジックをマニュアルで）
        double px = shooter.getX();
        double py = shooter.getEyeY() - 0.1D; // 少し手前に出す
        double pz = shooter.getZ();
        this.setPos(px, py, pz);

        // 3) 初速と向きをセット（デフォルトのバニラ雪玉と同じ値を使う）
        //    第4引数 1.5F が速さ multiplier、第5引数 1.0F が散布角度のきつさ
        this.shootFromRotation(
                shooter,
                shooter.getXRot(),
                shooter.getYRot(),
                0.0F,   // 垂直オフセット（通常は 0 で OK）
                1.5F,   // 初速 multiplier
                1.0F    // 散布（0.0F だと真っすぐ。1.0F がバニラ雪玉と同等）
        );

        // 4) **ここで必ずアイテム情報を入れておく**
        //    ThrownItemRenderer は getItem() の内容を使って描画する。
        this.setItem(new ItemStack(HelloWorldMod.HELLO_GEM.get()));
    }

     */

    public LightSnowballEntity(EntityType<? extends Snowball> type, LivingEntity shooter, Level world) {
        super(type, world);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        this.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, 1.5F, 1.0F);
        this.setItem(new ItemStack(HelloWorldMod.HELLO_GEM.get()));
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && isAlive()) {
            BlockPos pos = blockPosition();
            if (!pos.equals(prevLightPos)) {
                if (prevLightPos != null) {
                    level().setBlock(prevLightPos, Blocks.AIR.defaultBlockState(), 3);
                }
                level().setBlock(pos, Blocks.LIGHT.defaultBlockState()
                        .setValue(LightBlock.LEVEL, 15), 3);
                prevLightPos = pos;
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);


        // 既存の光源後片付けもそのまま呼ぶ


        // ★サーバー側かつ「ブロックに当たったとき」だけ実行
        if(!level().isClientSide) {
            if (result instanceof BlockHitResult hit) {

                BlockPos hitPos = hit.getBlockPos();
                BlockPos torchPos = hitPos.relative(hit.getDirection());

                // ── ここで着弾音を鳴らす ──
                // 第一引数：聞くプレイヤーを null にすると、範囲内のすべてに聞こえる
                // 第二〜四引数：音を鳴らす座標。hitPos でも torchPos でも可。好みで調整してください。
                level().playSound(
                        null,
                        hitPos.getX() + 0.5,  // 中心付近で鳴るように
                        hitPos.getY() + 0.5,
                        hitPos.getZ() + 0.5,
                        SoundEvents.ANVIL_HIT, // ここを好みの SoundEvent に置き換え
                        SoundSource.PLAYERS,        // 音源のカテゴリ
                        1.0F,                       // ボリューム（0.0〜1.0）
                        2.0F                        // ピッチ（0.5〜2.0 くらいが一般的）
                );

                // 着弾面の手前に松明を設置
                //BlockPos torchPos = hit.getBlockPos().relative(hit.getDirection());
                level().setBlockAndUpdate(torchPos, Blocks.TORCH.defaultBlockState());


            }
            else if (result instanceof EntityHitResult entityHit)
            {
                Entity target = entityHit.getEntity();
                if (target instanceof LivingEntity mob) {
                    // MobLightHandler に「このモブを光源化するように登録」する
                    MobLightHandler.registerMobAsLight((LivingEntity) mob);
                }
            }

            if ( prevLightPos != null)
            {
                level().setBlock(prevLightPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }



    }

    @Override
    public void remove(RemovalReason reason) {
        if (level() instanceof ClientLevel client && prevLightPos != null) {
            client.removeBlock(prevLightPos,true);
        }
        super.remove(reason);
    }

    @Override
    protected Item getDefaultItem() {
        // 何があっても HELLO_GEM のテクスチャを描画させる
        return new ItemStack(HelloWorldMod.HELLO_GEM.get()).getItem();
    }
    @Override
    public ItemStack getItem() {
        // DataManager の内容に関わらず常に HELLO_GEM の ItemStack を返す
        return new ItemStack(HelloWorldMod.HELLO_GEM.get());
    }
}
