package me.javivi.kb.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.javivi.kb.Keyblocker;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Server configuration for KeyBlocker.
 * Used to sync states with clients.
 */
public class ServerConfig {
    private List<String> hiddenCategories = new ArrayList<>();
    private List<String> hiddenKeybinds = new ArrayList<>();
    private Set<String> disabledKeys = new CopyOnWriteArraySet<>();
    
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("keyblocker-server.json");
    
    public List<String> getHiddenCategories() {
        return hiddenCategories;
    }
    
    public void setHiddenCategories(Set<String> hiddenCategories) {
        this.hiddenCategories = new ArrayList<>(hiddenCategories);
    }
    
    public List<String> getHiddenKeybinds() {
        return hiddenKeybinds;
    }
    
    public void setHiddenKeybinds(Set<String> hiddenKeybinds) {
        this.hiddenKeybinds = new ArrayList<>(hiddenKeybinds);
    }
    
    public Set<String> getDisabledKeys() {
        return this.disabledKeys;
    }
    
    public void setDisabledKeys(Set<String> disabledKeys) {
        this.disabledKeys = new CopyOnWriteArraySet<>(disabledKeys);
    }
    
    public void addHiddenCategory(String category) {
        if (!hiddenCategories.contains(category)) {
            hiddenCategories.add(category);
        }
    }
    
    public void removeHiddenCategory(String category) {
        hiddenCategories.remove(category);
    }
    
    public void addHiddenKeybind(String keybind) {
        if (!hiddenKeybinds.contains(keybind)) {
            hiddenKeybinds.add(keybind);
        }
    }
    
    public void removeHiddenKeybind(String keybind) {
        hiddenKeybinds.remove(keybind);
    }
    
    public void addDisabledKey(String key) {
        disabledKeys.add(key);
    }
    
    public void removeDisabledKey(String key) {
        disabledKeys.remove(key);
    }
    
    public void saveConfig() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            // Ensure lists are not null
            if (this.hiddenCategories == null) this.hiddenCategories = new ArrayList<>();
            if (this.hiddenKeybinds == null) this.hiddenKeybinds = new ArrayList<>();
            if (this.disabledKeys == null) this.disabledKeys = new CopyOnWriteArraySet<>();
            
            Files.writeString(CONFIG_FILE, gson.toJson(this));
            Keyblocker.LOGGER.info("Server config saved to: " + CONFIG_FILE);
        } catch (IOException e) {
            Keyblocker.LOGGER.error("Error saving server config: " + e.getMessage());
        }
    }
    
    public void loadConfig() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            if (!Files.exists(CONFIG_FILE)) {
                saveConfig();
                return;
            }
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonContent = Files.readString(CONFIG_FILE);
            
            if (jsonContent.isEmpty()) {
                saveConfig();
            } else {
                ServerConfig config = gson.fromJson(jsonContent, ServerConfig.class);
                this.hiddenCategories = config.hiddenCategories != null ? config.hiddenCategories : new ArrayList<>();
                this.hiddenKeybinds = config.hiddenKeybinds != null ? config.hiddenKeybinds : new ArrayList<>();
                this.disabledKeys = config.disabledKeys != null ? new CopyOnWriteArraySet<>(config.disabledKeys) : new CopyOnWriteArraySet<>();
                
                Keyblocker.LOGGER.info("Server config loaded from: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            Keyblocker.LOGGER.error("Error loading server config: " + e.getMessage());
            this.hiddenCategories = new ArrayList<>();
            this.hiddenKeybinds = new ArrayList<>();
            this.disabledKeys = new CopyOnWriteArraySet<>();
            saveConfig();
        }
    }
}
