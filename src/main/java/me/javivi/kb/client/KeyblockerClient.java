package me.javivi.kb.client;

import me.javivi.kb.Keyblocker;
import me.javivi.kb.network.CategoryListPacket;
import me.javivi.kb.network.KeyBindingListPacket;
import me.javivi.kb.network.NetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KeyblockerClient implements ClientModInitializer {
    private boolean keyBindingsSent = false;
    
    @Override
    public void onInitializeClient() {
        NetworkHandler.registerClientPackets();
        Keyblocker.getInstance().getConfig().loadConfig();
        
        // Enviar KeyBindings cuando el cliente se conecte al servidor
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            keyBindingsSent = false;
        });
        
        // Enviar las KeyBindings y categorías después de que el mundo esté cargado
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (!keyBindingsSent && world.isClient) {
                sendAllKeyBindingsAndCategoriesToServer();
                keyBindingsSent = true;
            }
        });
        
        Keyblocker.LOGGER.info("KeyBlocker client initialized!");
    }
    
    private void sendAllKeyBindingsAndCategoriesToServer() {
        try {
            // Usar reflection para obtener todas las KeyBindings
            Field keysField = KeyBinding.class.getDeclaredField("field_1657"); // KEYS_BY_ID en intermediary
            keysField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            Map<String, KeyBinding> keysById = (Map<String, KeyBinding>) keysField.get(null);
            
            List<String> keyNames = new ArrayList<>(keysById.keySet());
            Set<String> categories = new HashSet<>();
            
            // Extraer todas las categorías únicas
            for (KeyBinding keyBinding : keysById.values()) {
                categories.add(keyBinding.getCategory());
            }
            
            // Enviar KeyBindings
            if (!keyNames.isEmpty()) {
                KeyBindingListPacket keyPacket = new KeyBindingListPacket(keyNames);
                NetworkHandler.sendKeyBindingsToServer(keyPacket);
                
                Keyblocker.LOGGER.info("Sent {} KeyBindings to server", keyNames.size());
            }
            
            // Enviar Categorías
            if (!categories.isEmpty()) {
                CategoryListPacket categoryPacket = new CategoryListPacket(new ArrayList<>(categories));
                NetworkHandler.sendCategoriesToServer(categoryPacket);
                
                Keyblocker.LOGGER.info("Sent {} Categories to server", categories.size());
            }
            
        } catch (Exception e) {
            Keyblocker.LOGGER.error("Failed to send KeyBindings and Categories to server", e);
        }
    }
}
