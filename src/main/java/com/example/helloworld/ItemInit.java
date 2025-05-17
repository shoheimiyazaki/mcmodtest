package com.example.helloworld;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SnowballItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HelloWorldMod.MODID);

    public static final RegistryObject<Item> HELLO_GEM =
            ITEMS.register("hello_gem",
                    () -> new SnowballItem(
                            new Item.Properties()
                                    .stacksTo(16)      // スタック数だけ指定
                    )
            );

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
