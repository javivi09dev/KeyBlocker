package me.javivi.kb.api;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Public API for other mods to dynamically control which keys and categories 
 * are blocked or hidden for specific players.
 */
public interface KeyBlockerAPI {
    
    static KeyBlockerAPI getInstance() {
        return me.javivi.kb.api.impl.KeyBlockerAPIImpl.INSTANCE;
    }
    
    /**
     * Blocks a key completely 
     */
    boolean blockKey(String keyBinding, Collection<ServerPlayerEntity> players);
    
    boolean unblockKey(String keyBinding, Collection<ServerPlayerEntity> players);
    
    /**
     * Hides a key from menu but keeps functionality intact.
     */
    boolean hideKey(String keyBinding, Collection<ServerPlayerEntity> players);
    
    boolean showKey(String keyBinding, Collection<ServerPlayerEntity> players);
    
    /**
     * Hides an entire category of keys (e.g., "key.categories.movement").
     */
    boolean hideCategory(String category, Collection<ServerPlayerEntity> players);
    
    boolean showCategory(String category, Collection<ServerPlayerEntity> players);
    
    /**
     * Gets all keybindings known by the server (including mod keys).
     */
    List<String> getAllAvailableKeys();
    
    /**
     * Gets all categories known by the server (including mod categories).
     */
    List<String> getAllAvailableCategories();
    
    boolean isKeyBlocked(String keyBinding, ServerPlayerEntity player);
    
    boolean isKeyHidden(String keyBinding, ServerPlayerEntity player);
    
    boolean isCategoryHidden(String category, ServerPlayerEntity player);
    
    /**
     * Syncs server configuration to players, overwriting their local config.
     */
    boolean syncConfigToPlayers(Collection<ServerPlayerEntity> players);
    
    Set<String> getServerBlockedKeys();
    
    Set<String> getServerHiddenKeys();
    
    Set<String> getServerHiddenCategories();
    
    void setServerBlockedKeys(Set<String> blockedKeys);
    
    void setServerHiddenKeys(Set<String> hiddenKeys);
    
    void setServerHiddenCategories(Set<String> hiddenCategories);
}
