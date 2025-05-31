package com.example.helloworld;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HelloWorldMod.MODID)
public class HelloWorldMod
{
    public static final String MODID = "helloworld";

    // ── アイテム登録用 DeferredRegister ────────────────────────────
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // カスタム雪玉アイテム
    public static final RegistryObject<Item> HELLO_GEM =
                    ITEMS.register
                            (
                            "hello_gem",
                            () -> new SnowballItem(new Item.Properties().stacksTo(64)
                            )
    );

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    // LightSnowballEntity の EntityType
    public static final RegistryObject<EntityType<LightSnowballEntity>> LIGHT_SNOWBALL =
            ENTITIES.register("light_snowball", () ->
                    EntityType.Builder.<LightSnowballEntity>of(LightSnowballEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build(MODID + ":light_snowball")
            );
    
    public HelloWorldMod() {
        System.out.println("Hello World Mod Loaded!");
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
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
        public static void onProjectileImpact(ProjectileImpactEvent event) {
            if (!(event.getEntity() instanceof Snowball snowball)) return;
            if (!snowball.getItem().getItem().equals(HELLO_GEM.get())) return;
            if (event.getRayTraceResult() instanceof BlockHitResult hit) {
                BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
                snowball.level().setBlockAndUpdate(pos, Blocks.TORCH.defaultBlockState());
            }
        }
    }
}