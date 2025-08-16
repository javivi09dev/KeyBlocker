package me.javivi.kb;

import me.javivi.kb.commands.KeyblockerCommand;
import me.javivi.kb.network.NetworkHandler;
import me.javivi.kb.server.KeyBindingManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Keyblocker implements ModInitializer {
    public static final String MOD_ID = "keyblocker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static Keyblocker instance;
    private final Config config = new Config();

    @Override
    public void onInitialize() {
        instance = this;
        
        NetworkHandler.registerPackets();
        
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> 
            KeyblockerCommand.register(dispatcher));
        
        // Clean up player data when they disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            KeyBindingManager.getInstance().removePlayer(handler.getPlayer().getUuid());
        });
        
        config.loadConfig();
        
        LOGGER.info("KeyBlocker mod initialized successfully!");
    }
    
    public static Keyblocker getInstance() {
        return instance;
    }
    
    public Config getConfig() {
        return config;
    }
}
