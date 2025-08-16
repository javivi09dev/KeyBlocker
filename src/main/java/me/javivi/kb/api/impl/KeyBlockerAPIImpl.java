package me.javivi.kb.api.impl;

import me.javivi.kb.Config;
import me.javivi.kb.Keyblocker;
import me.javivi.kb.api.KeyBlockerAPI;
import me.javivi.kb.network.NetworkHandler;
import me.javivi.kb.network.SyncConfigPacket;
import me.javivi.kb.network.UpdateKeysPacket;
import me.javivi.kb.server.KeyBindingManager;
import me.javivi.kb.server.ServerConfig;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class KeyBlockerAPIImpl implements KeyBlockerAPI {
    public static final KeyBlockerAPIImpl INSTANCE = new KeyBlockerAPIImpl();
    
    private final ServerConfig serverConfig = new ServerConfig();
    
    private KeyBlockerAPIImpl() {
        serverConfig.loadConfig();
    }
    
    @Override
    public boolean blockKey(String keyBinding, Collection<ServerPlayerEntity> players) {
        if (keyBinding == null || keyBinding.trim().isEmpty() || players == null || players.isEmpty()) {
            return false;
        }
        
        try {
            UpdateKeysPacket packet = new UpdateKeysPacket(keyBinding, "block", "key");
            for (ServerPlayerEntity player : players) {
                NetworkHandler.sendToClient(packet, player);
            }
            
            Keyblocker.LOGGER.info("API: Blocked key '{}' for {} players", keyBinding, players.size());
            return true;
        } catch (Exception e) {
            Keyblocker.LOGGER.error("API: Failed to block key '{}'", keyBinding, e);
            return false;
        }
    }
    
    @Override
    public boolean unblockKey(String keyBinding, Collection<ServerPlayerEntity> players) {
        if (keyBinding == null || keyBinding.trim().isEmpty() || players == null || players.isEmpty()) {
            return false;
        }
        
        try {
            UpdateKeysPacket packet = new UpdateKeysPacket(keyBinding, "unblock", "key");
            for (ServerPlayerEntity player : players) {
                NetworkHandler.sendToClient(packet, player);
            }
            
            Keyblocker.LOGGER.info("API: Unblocked key '{}' for {} players", keyBinding, players.size());
            return true;
        } catch (Exception e) {
            Keyblocker.LOGGER.error("API: Failed to unblock key '{}'", keyBinding, e);
            return false;
        }
    }
    
    @Override
    public boolean hideKey(String keyBinding, Collection<ServerPlayerEntity> players) {
        if (keyBinding == null || keyBinding.trim().isEmpty() || players == null || players.isEmpty()) {
            return false;
        }
        
        try {
            UpdateKeysPacket packet = new UpdateKeysPacket(keyBinding, "hide", "key");
            for (ServerPlayerEntity player : players) {
                NetworkHandler.sendToClient(packet, player);
            }
            
            Keyblocker.LOGGER.info("API: Hidden key '{}' for {} players", keyBinding, players.size());
            return true;
        } catch (Exception e) {
            Keyblocker.LOGGER.error("API: Failed to hide key '{}'", keyBinding, e);
            return false;
        }
    }
    
    @Override
    public boolean showKey(String keyBinding, Collection<ServerPlayerEntity> players) {
        if (keyBinding == null || keyBinding.trim().isEmpty() || players == null || players.isEmpty()) {
            return false;
        }
        
        try {
            UpdateKeysPacket packet = new UpdateKeysPacket(keyBinding, "unhide", "key");
            for (ServerPlayerEntity player : players) {
                NetworkHandler.sendToClient(packet, player);
            }
            
            Keyblocker.LOGGER.info("API: Showed key '{}' for {} players", keyBinding, players.size());
            return true;
        } catch (Exception e) {
            Keyblocker.LOGGER.error("API: Failed to show key '{}'", keyBinding, e);
            return false;
        }
    }
    
    @Override
    public boolean hideCategory(String category, Collection<ServerPlayerEntity> players) {
        if (category == null || category.trim().isEmpty() || players == null || players.isEmpty()) {
            return false;
        }
        
        try {
            UpdateKeysPacket packet = new UpdateKeysPacket(category, "hide", "category");
            for (ServerPlayerEntity player : players) {
                NetworkHandler.sendToClient(packet, player);
            }
            
            Keyblocker.LOGGER.info("API: Hidden category '{}' for {} players", category, players.size());
            return true;
        } catch (Exception e) {
            Keyblocker.LOGGER.error("API: Failed to hide category '{}'", category, e);
            return false;
        }
    }
    
    @Override
    public boolean showCategory(String category, Collection<ServerPlayerEntity> players) {
        if (category == null || category.trim().isEmpty() || players == null || players.isEmpty()) {
            return false;
        }
        
        try {
            UpdateKeysPacket packet = new UpdateKeysPacket(category, "unhide", "category");
            for (ServerPlayerEntity player : players) {
                NetworkHandler.sendToClient(packet, player);
            }
            
            Keyblocker.LOGGER.info("API: Showed category '{}' for {} players", category, players.size());
            return true;
        } catch (Exception e) {
            Keyblocker.LOGGER.error("API: Failed to show category '{}'", category, e);
            return false;
        }
    }
    
    @Override
    public List<String> getAllAvailableKeys() {
        return KeyBindingManager.getInstance().getAllKnownKeyBindings();
    }
    
    @Override
    public List<String> getAllAvailableCategories() {
        return KeyBindingManager.getInstance().getAllKnownCategories();
    }
    
    @Override
    public boolean isKeyBlocked(String keyBinding, ServerPlayerEntity player) {
        // Note: Client state tracking not implemented yet
        return false;
    }
    
    @Override
    public boolean isKeyHidden(String keyBinding, ServerPlayerEntity player) {
        // Note: Client state tracking not implemented yet
        return false;
    }
    
    @Override
    public boolean isCategoryHidden(String category, ServerPlayerEntity player) {
        // Note: Client state tracking not implemented yet
        return false;
    }
    
    @Override
    public boolean syncConfigToPlayers(Collection<ServerPlayerEntity> players) {
        if (players == null || players.isEmpty()) {
            return false;
        }
        
        try {
            SyncConfigPacket packet = new SyncConfigPacket(
                serverConfig.getHiddenCategories(),
                serverConfig.getHiddenKeybinds(),
                new ArrayList<>(serverConfig.getDisabledKeys())
            );
            
            for (ServerPlayerEntity player : players) {
                NetworkHandler.sendToClient(packet, player);
            }
            
            Keyblocker.LOGGER.info("API: Synced server config to {} players", players.size());
            return true;
        } catch (Exception e) {
            Keyblocker.LOGGER.error("API: Failed to sync config to players", e);
            return false;
        }
    }
    
    @Override
    public Set<String> getServerBlockedKeys() {
        return new HashSet<>(serverConfig.getDisabledKeys());
    }
    
    @Override
    public Set<String> getServerHiddenKeys() {
        return new HashSet<>(serverConfig.getHiddenKeybinds());
    }
    
    @Override
    public Set<String> getServerHiddenCategories() {
        return new HashSet<>(serverConfig.getHiddenCategories());
    }
    
    @Override
    public void setServerBlockedKeys(Set<String> blockedKeys) {
        serverConfig.setDisabledKeys(blockedKeys);
        serverConfig.saveConfig();
        Keyblocker.LOGGER.info("API: Server blocked keys updated: {}", blockedKeys != null ? blockedKeys.size() : 0);
    }
    
    @Override
    public void setServerHiddenKeys(Set<String> hiddenKeys) {
        serverConfig.setHiddenKeybinds(hiddenKeys);
        serverConfig.saveConfig();
        Keyblocker.LOGGER.info("API: Server hidden keys updated: {}", hiddenKeys != null ? hiddenKeys.size() : 0);
    }
    
    @Override
    public void setServerHiddenCategories(Set<String> hiddenCategories) {
        serverConfig.setHiddenCategories(hiddenCategories);
        serverConfig.saveConfig();
        Keyblocker.LOGGER.info("API: Server hidden categories updated: {}", hiddenCategories != null ? hiddenCategories.size() : 0);
    }
}
