// src/main/java/com/example/helloworld/LightSnowballItem.java
package com.example.helloworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.Level;

public class LightSnowballItem extends SnowballItem {

    public LightSnowballItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 投擲時の処理をオーバーライドして LightSnowballEntity を生成する
        ItemStack stack = player.getItemInHand(hand);

        // 1) サーバー側でのみ実際にエンティティをスポーン
        if (!level.isClientSide) {
            // ❶ 「EntityType + LivingEntity + Level」を受け取るコンストラクタを使う
            LightSnowballEntity light = new LightSnowballEntity(
                    HelloWorldMod.LIGHT_SNOWBALL.get(),
                    (LivingEntity) player,
                    level
            );
            // super(type, world) → setOwner / setPos / shootFromRotation はコンストラクタ内で完了

            // ❷ ワールドにスポーン
            level.addFreshEntity(light);

            // ❸ 投擲サウンド
            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SNOWBALL_THROW,
                    SoundSource.PLAYERS,
                    0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
            );
        }

        // 2) 投擲統計を加算
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));

        // 3) クリエイティブモード以外ではスタックを 1 減らす
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        // 4) 成功を返す（クライアント／サーバー双方でリターン）
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }


    @Override
    public Projectile asProjectile(Level level, net.minecraft.core.Position position, ItemStack stack, net.minecraft.core.Direction direction) {
        // ※ ここで渡される引数の型名や順序は MCP 名（リマップ後）によって変わることがあります。
        //    IntelliJ のシグネチャを必ず確認して、下記を適宜合わせてください。

        // LightSnowballEntity の (EntityType, LivingEntity, Level) コンストラクタを呼ぶ
        if (position instanceof net.minecraft.world.entity.LivingEntity shooter) {
            LightSnowballEntity light = new LightSnowballEntity(
                    HelloWorldMod.LIGHT_SNOWBALL.get(),
                    shooter,
                    level
            );
            // 投擲アイテムの表示用として ItemStack をセット
            light.setItem(stack);
            return light;
        }

        // 通常はここに来ませんが、一応バニラと同じ fallback を返しておく
        Snowball fallback = new Snowball(level, position.x(), position.y(), position.z());
        fallback.setItem(stack);
        return fallback;
    }
}
