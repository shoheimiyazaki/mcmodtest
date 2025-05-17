package com.example.helloworld;

import net.minecraftforge.fml.common.Mod;

@Mod("helloworld")
public class HelloWorldMod {
    public static final String MODID = "helloworld";

    public HelloWorldMod()
    {
        System.out.println("Hello World Mod Loaded!");
        ItemInit.register();

    }
}
