package com.coredex.gazetteer;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gazetteer implements ModInitializer{
    public static final String MOD_ID = "gazetteer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize(){
        MidnightConfig.init(MOD_ID, GazetteerConfig.class);
        ClientCommandRegistrationCallback.EVENT.register(new GazetteerCommand());
    }
}
