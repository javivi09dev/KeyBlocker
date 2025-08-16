package me.javivi.kb.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KeyBindingManager {
    private static final KeyBindingManager INSTANCE = new KeyBindingManager();
    
    // Almacena las KeyBindings por jugador (UUID -> Set de nombres de teclas)
    private final ConcurrentHashMap<UUID, Set<String>> playerKeyBindings = new ConcurrentHashMap<>();
    
    // Almacena las categorías por jugador (UUID -> Set de nombres de categorías)
    private final ConcurrentHashMap<UUID, Set<String>> playerCategories = new ConcurrentHashMap<>();
    
    // Cache global de todas las KeyBindings conocidas
    private final Set<String> allKnownKeyBindings = new HashSet<>();
    
    // Cache global de todas las categorías conocidas
    private final Set<String> allKnownCategories = new HashSet<>();
    
    private KeyBindingManager() {}
    
    public static KeyBindingManager getInstance() {
        return INSTANCE;
    }
    
    public void updatePlayerKeyBindings(UUID playerId, List<String> keyBindings) {
        Set<String> keySet = new HashSet<>(keyBindings);
        playerKeyBindings.put(playerId, keySet);
        
        // Actualizar el cache global
        synchronized (allKnownKeyBindings) {
            allKnownKeyBindings.addAll(keyBindings);
        }
    }
    
    public void updatePlayerCategories(UUID playerId, List<String> categories) {
        Set<String> categorySet = new HashSet<>(categories);
        playerCategories.put(playerId, categorySet);
        
        // Actualizar el cache global
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
            return new ArrayList<>(allKnownKeyBindings);
        }
    }
    
    public List<String> getAllKnownCategories() {
        synchronized (allKnownCategories) {
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
