package me.javivi.kb.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KeyBindingManager {
    private static final KeyBindingManager INSTANCE = new KeyBindingManager();
    
    private final ConcurrentHashMap<UUID, Set<String>> playerKeyBindings = new ConcurrentHashMap<>();
    
    private final ConcurrentHashMap<UUID, Set<String>> playerCategories = new ConcurrentHashMap<>();
    
    private final Set<String> allKnownKeyBindings = new HashSet<>();
    
    private final Set<String> allKnownCategories = new HashSet<>();
    
    private KeyBindingManager() {}
    
    public static KeyBindingManager getInstance() {
        return INSTANCE;
    }
    
    public void updatePlayerKeyBindings(UUID playerId, List<String> keyBindings) {
        Set<String> keySet = new HashSet<>(keyBindings);
        playerKeyBindings.put(playerId, keySet);
        
        synchronized (allKnownKeyBindings) {
            allKnownKeyBindings.addAll(keyBindings);
        }
    }
    
    public void updatePlayerCategories(UUID playerId, List<String> categories) {
        Set<String> categorySet = new HashSet<>(categories);
        playerCategories.put(playerId, categorySet);
        
        synchronized (allKnownCategories) {
            allKnownCategories.addAll(categories);
        }
    }
    
    public void removePlayer(UUID playerId) {
        playerKeyBindings.remove(playerId);
        playerCategories.remove(playerId);
    }
    
    public List<String> getAllKnownKeyBindings() {
        synchronized (allKnownKeyBindings) {
            if (allKnownKeyBindings.isEmpty()) {
                return List.of(
                    "key.screenshot",
                    "key.forward",
                    "key.back",
                    "key.left",
                    "key.right",
                    "key.jump",
                    "key.sneak",
                    "key.sprint",
                    "key.inventory",
                    "key.swapHands",
                    "key.drop",
                    "key.use",
                    "key.attack",
                    "key.pickItem",
                    "key.chat",
                    "key.playerlist",
                    "key.command",
                    "key.togglePerspective",
                    "key.smoothCamera",
                    "key.fullscreen",
                    "key.spectatorOutlines"
                );
            }
            return new ArrayList<>(allKnownKeyBindings);
        }
    }
    
    public List<String> getAllKnownCategories() {
        synchronized (allKnownCategories) {     
            if (allKnownCategories.isEmpty()) {
                return List.of(
                    "key.categories.movement",
                    "key.categories.gameplay",
                    "key.categories.inventory",
                    "key.categories.creative",
                    "key.categories.multiplayer",
                    "key.categories.ui",
                    "key.categories.misc"
                );
            }
            return new ArrayList<>(allKnownCategories);
        }
    }
    
    public Set<String> getPlayerKeyBindings(UUID playerId) {
        return playerKeyBindings.getOrDefault(playerId, new HashSet<>());
    }
    
    public Set<String> getPlayerCategories(UUID playerId) {
        return playerCategories.getOrDefault(playerId, new HashSet<>());
    }
}
