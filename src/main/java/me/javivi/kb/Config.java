package me.javivi.kb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Config {
    private List<String> hiddenCategories = new ArrayList<>();
    private List<String> hiddenKeybinds = new ArrayList<>();
    private Set<String> disabledKeys = new CopyOnWriteArraySet<>();
    public boolean consoleLogs = true;
    
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("keyblocker.json");
    
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
    
    public boolean isKeyDisabled(String keyName) {
        return this.disabledKeys != null && this.disabledKeys.contains(keyName);
    }
    
    @Environment(EnvType.CLIENT)
    public boolean isCategoryHidden(String category) {
        return hiddenCategories.contains(category);
    }
    
    @Environment(EnvType.CLIENT)
    public boolean isKeybindHidden(String keybind) {
        return hiddenKeybinds.contains(keybind);
    }
    
    @Environment(EnvType.CLIENT)
    public boolean isKeybindHidden(KeyBinding keyBinding) {
        return isCategoryHidden(keyBinding.getCategory()) || isKeybindHidden(keyBinding.getTranslationKey());
    }
    
    public void saveConfig() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            if (this.hiddenCategories == null) this.hiddenCategories = new ArrayList<>();
            if (this.hiddenKeybinds == null) this.hiddenKeybinds = new ArrayList<>();
            if (this.disabledKeys == null) this.disabledKeys = new CopyOnWriteArraySet<>();
            
            Files.writeString(CONFIG_FILE, gson.toJson(this));
        } catch (IOException e) {
            Keyblocker.LOGGER.error("Error saving config: " + e.getMessage());
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
                Config config = gson.fromJson(jsonContent, Config.class);
                this.hiddenCategories = config.hiddenCategories != null ? config.hiddenCategories : new ArrayList<>();
                this.hiddenKeybinds = config.hiddenKeybinds != null ? config.hiddenKeybinds : new ArrayList<>();
                this.consoleLogs = config.consoleLogs;
                this.disabledKeys = config.disabledKeys != null ? new CopyOnWriteArraySet<>(config.disabledKeys) : new CopyOnWriteArraySet<>();
                Keyblocker.LOGGER.info("Config loaded from: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            Keyblocker.LOGGER.error("Error loading config: " + e.getMessage());
            this.hiddenCategories = new ArrayList<>();
            this.hiddenKeybinds = new ArrayList<>();
            this.disabledKeys = new CopyOnWriteArraySet<>();
            this.consoleLogs = false;
            saveConfig();
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void filterKeyBindings() {
        
    }
}
