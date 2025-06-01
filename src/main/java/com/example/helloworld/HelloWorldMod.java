package com.example.helloworld;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * HELLOWORLD モッド本体。投てき時に SnowballEntity を LightSnowballEntity に置き換えます。
 */
@Mod(HelloWorldMod.MODID)
public class HelloWorldMod
{
    public static final String MODID = "helloworld";
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    /**
     * カスタム雪玉アイテム。通常の SnowballItem と同じですが、
     * ProjectileLaunchEvent で置き換えを行うため、内部オーバーライドは不要です。
     */
    public static final RegistryObject<Item> HELLO_GEM = ITEMS.register
            ("hello_gem",
            () -> new LightSnowballItem(new Item.Properties().stacksTo(64))
            //        () -> new SnowballItem(new Item.Properties().stacksTo(64))
            );

    // ── エンティティ登録用 DeferredRegister ────────────────────────────
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    /**
     * LightSnowballEntity の EntityType を登録。
     */
    public static final RegistryObject<EntityType<LightSnowballEntity>> LIGHT_SNOWBALL =
            ENTITIES.register("light_snowball", () ->
                    EntityType.Builder.<LightSnowballEntity>of(LightSnowballEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build(MODID + ":light_snowball")
            );

    public HelloWorldMod(FMLJavaModLoadingContext context)
    {
        System.out.println("Hello World Mod Loaded!");

        // アイテムとエンティティの両方を登録
        IEventBus modEventBus = context.getModEventBus();
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
    }
    public HelloWorldMod() {
        // ここでコンテキストを取得して、アイテム・エンティティを登録する
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus modEventBus = context.getModEventBus();

        System.out.println("Hello World Mod Loaded!");

        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
    }

    // ── クライアント専用イベント ─────────────────────────────────
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents
    {
        @SubscribeEvent
        public static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                event.accept(HELLO_GEM.get());
            }
        }
    }

    // ── 汎用 Forge バスイベント ───────────────────────────────────
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class CommonEvents
    {
        @SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event)
        {
            if (!(event.getEntity() instanceof LightSnowballEntity snowball))
                return;
            if (!snowball.getItem().getItem().equals(HELLO_GEM.get()))
                return;
            if (event.getRayTraceResult() instanceof BlockHitResult hit)
            {
                BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
                snowball.level().setBlockAndUpdate(pos, Blocks.TORCH.defaultBlockState());
            }
        }

        @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
        public final class ClientSetup
        {
            @SubscribeEvent
            public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
                // Snowball と同じ 3D アイテム飛翔体レンダラーを使う
                event.registerEntityRenderer(HelloWorldMod.LIGHT_SNOWBALL.get(),
                        ctx -> new ThrownItemRenderer<>(ctx, 1.0f, true));
            }
        }


    }

}
