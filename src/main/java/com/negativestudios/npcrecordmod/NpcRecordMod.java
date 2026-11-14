package com.negativestudios.npcrecordmod;

import com.mojang.logging.LogUtils;
import com.negativestudios.npcrecordmod.network.NetworkHandler;
import com.negativestudios.npcrecordmod.registry.ModEntities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(NpcRecordMod.MOD_ID)
public class NpcRecordMod {
    public static final String MOD_ID = "npcrecordmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NpcRecordMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modBus);
        NetworkHandler.register();
    }
}
