package me.javivi.kb.network;

import me.javivi.kb.Config;
import me.javivi.kb.Keyblocker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public record UpdateKeysPacket(String key, String action, String type) implements CustomPayload {
    public static final CustomPayload.Id<UpdateKeysPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(Keyblocker.MOD_ID, "update_keys"));
    
    public static final PacketCodec<RegistryByteBuf, UpdateKeysPacket> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, UpdateKeysPacket::key,
            PacketCodecs.STRING, UpdateKeysPacket::action,
            PacketCodecs.STRING, UpdateKeysPacket::type,
            UpdateKeysPacket::new
    );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
    
    @Environment(EnvType.CLIENT)
    public void handleClient() {
        Config config = Keyblocker.getInstance().getConfig();
        
        if ("category".equals(this.type)) {
            // Manejar categorías
            Set<String> hiddenCategories = new HashSet<>(config.getHiddenCategories());
            
            switch (this.action) {
                case "hide" -> {
                    hiddenCategories.add(this.key);
                    Keyblocker.LOGGER.info("Hiding category: " + this.key);
                }
                case "unhide" -> {
                    hiddenCategories.remove(this.key);
                    Keyblocker.LOGGER.info("Showing category: " + this.key);
                }
            }
            
            config.setHiddenCategories(hiddenCategories);
        } else {
            // Manejar keys individuales
            Set<String> hiddenKeybinds = new HashSet<>(config.getHiddenKeybinds());
            Set<String> disabledKeys = new HashSet<>(config.getDisabledKeys());
            
            switch (this.action) {
                case "hide" -> {
                    hiddenKeybinds.add(this.key);
                    Keyblocker.LOGGER.info("Hiding key: " + this.key);
                }
                case "unhide" -> {
                    hiddenKeybinds.remove(this.key);
                    Keyblocker.LOGGER.info("Showing key: " + this.key);
                }
                case "block" -> {
                    disabledKeys.add(this.key);
                    hiddenKeybinds.add(this.key);
                    Keyblocker.LOGGER.info("Blocking key: " + this.key);
                }
                case "unblock" -> {
                    disabledKeys.remove(this.key);
                    hiddenKeybinds.remove(this.key);
                    Keyblocker.LOGGER.info("Unblocking key: " + this.key);
                }
            }
            
            config.setHiddenKeybinds(hiddenKeybinds);
            config.setDisabledKeys(disabledKeys);
        }
        
        config.saveConfig();
        
        if (MinecraftClient.getInstance().currentScreen instanceof KeybindsScreen) {
            config.filterKeyBindings();
        }
    }
}
