package me.javivi.kb.client;

import me.javivi.kb.Keyblocker;
import me.javivi.kb.network.CategoryListPacket;
import me.javivi.kb.network.KeyBindingListPacket;
import me.javivi.kb.network.NetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;      
import java.util.Set;

public class KeyblockerClient implements ClientModInitializer {
    private boolean keyBindingsSent = false;
    
    @Override
    public void onInitializeClient() {
        NetworkHandler.registerClientPackets();
        Keyblocker.getInstance().getConfig().loadConfig();

        KeyBindingScreenHandler.getInstance().initialize();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            keyBindingsSent = false;
        });

        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (!keyBindingsSent && world.isClient) {
                sendAllKeyBindingsAndCategoriesToServer();
                keyBindingsSent = true;
            }
        });

        Keyblocker.LOGGER.info("KeyBlocker client initialized with mixin-based keybinding interception!");
    }
    
    private void sendAllKeyBindingsAndCategoriesToServer() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.options == null) {
                return;
            }

            List<String> keyBindingKeys = new ArrayList<>();
            Set<String> categoryKeys = new HashSet<>();

            for (KeyBinding keyBinding : client.options.allKeys) {
                if (keyBinding != null && keyBinding.getTranslationKey() != null) {
                    keyBindingKeys.add(keyBinding.getTranslationKey());
                    if (keyBinding.getCategory() != null) {
                        categoryKeys.add(keyBinding.getCategory());
                    }
                }
            }

            if (!keyBindingKeys.isEmpty()) {
                KeyBindingListPacket keyPacket = new KeyBindingListPacket(keyBindingKeys);
                NetworkHandler.sendKeyBindingsToServer(keyPacket);

                Keyblocker.LOGGER.info("Sent {} KeyBindings to server", keyBindingKeys.size());
            }

            if (!categoryKeys.isEmpty()) {
                List<String> categoryList = new ArrayList<>(categoryKeys);
                CategoryListPacket categoryPacket = new CategoryListPacket(categoryList);
                NetworkHandler.sendCategoriesToServer(categoryPacket);

                Keyblocker.LOGGER.info("Sent {} Categories to server", categoryList.size());
            }

        } catch (Exception e) {
            Keyblocker.LOGGER.error("Failed to send KeyBindings and Categories to server", e);
        }
    }
}
