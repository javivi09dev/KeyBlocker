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
import java.util.List;

/**
 * Network packet for syncing complete server configuration to client.
 */
public record SyncConfigPacket(
    List<String> hiddenCategories,
    List<String> hiddenKeybinds,
    List<String> disabledKeys
) implements CustomPayload {
    
    public static final CustomPayload.Id<SyncConfigPacket> PACKET_ID = 
        new CustomPayload.Id<>(Identifier.of(Keyblocker.MOD_ID, "sync_config"));
    
    public static final PacketCodec<RegistryByteBuf, SyncConfigPacket> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()), SyncConfigPacket::hiddenCategories,
            PacketCodecs.STRING.collect(PacketCodecs.toList()), SyncConfigPacket::hiddenKeybinds,
            PacketCodecs.STRING.collect(PacketCodecs.toList()), SyncConfigPacket::disabledKeys,
            SyncConfigPacket::new
    );
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
    
    @Environment(EnvType.CLIENT)
    public void handleClient() {
        Config config = Keyblocker.getInstance().getConfig();
        
        // Overwrite client config with server config
        config.setHiddenCategories(new HashSet<>(this.hiddenCategories));
        config.setHiddenKeybinds(new HashSet<>(this.hiddenKeybinds));
        config.setDisabledKeys(new HashSet<>(this.disabledKeys));
        
        config.saveConfig();
        
        // Apply changes immediately if in controls menu
        if (MinecraftClient.getInstance().currentScreen instanceof KeybindsScreen) {
            config.filterKeyBindings();
        }
        
        Keyblocker.LOGGER.info("§aConfig synced from server: {} hidden categories, {} hidden keys, {} blocked keys", 
            this.hiddenCategories.size(), this.hiddenKeybinds.size(), this.disabledKeys.size());
    }
}
