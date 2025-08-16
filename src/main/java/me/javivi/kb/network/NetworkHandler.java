package me.javivi.kb.network;

import me.javivi.kb.Keyblocker;
import me.javivi.kb.server.KeyBindingManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class NetworkHandler {
    public static final Identifier UPDATE_KEYS_PACKET_ID = Identifier.of(Keyblocker.MOD_ID, "update_keys");
    
    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(UpdateKeysPacket.PACKET_ID, UpdateKeysPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncConfigPacket.PACKET_ID, SyncConfigPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(KeyBindingListPacket.PACKET_ID, KeyBindingListPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(CategoryListPacket.PACKET_ID, CategoryListPacket.PACKET_CODEC);
        
        // Server handlers for receiving keybinding and category data from clients
        ServerPlayNetworking.registerGlobalReceiver(KeyBindingListPacket.PACKET_ID, (packet, context) -> {
            context.server().execute(() -> {
                KeyBindingManager.getInstance().updatePlayerKeyBindings(
                    context.player().getUuid(), 
                    packet.keyBindings()
                );
                Keyblocker.LOGGER.info("Received {} KeyBindings from player {}", 
                    packet.keyBindings().size(), context.player().getName().getString());
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(CategoryListPacket.PACKET_ID, (packet, context) -> {
            context.server().execute(() -> {
                KeyBindingManager.getInstance().updatePlayerCategories(
                    context.player().getUuid(), 
                    packet.categories()
                );
                Keyblocker.LOGGER.info("Received {} Categories from player {}", 
                    packet.categories().size(), context.player().getName().getString());
            });
        });
        
        Keyblocker.LOGGER.info("Network packets registered");
    }
    
    @Environment(EnvType.CLIENT)
    public static void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(UpdateKeysPacket.PACKET_ID, (packet, context) -> {
            context.client().execute(() -> {
                packet.handleClient();
            });
        });
        
        ClientPlayNetworking.registerGlobalReceiver(SyncConfigPacket.PACKET_ID, (packet, context) -> {
            context.client().execute(() -> {
                packet.handleClient();
            });
        });
        
        Keyblocker.LOGGER.info("Network packet handlers registered");
    }
    
    public static void sendToClient(UpdateKeysPacket packet, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, packet);
    }
    
    public static void sendToClient(SyncConfigPacket packet, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, packet);
    }
    
    @Environment(EnvType.CLIENT)
    public static void sendKeyBindingsToServer(KeyBindingListPacket packet) {
        ClientPlayNetworking.send(packet);
    }
    
    @Environment(EnvType.CLIENT)
    public static void sendCategoriesToServer(CategoryListPacket packet) {
        ClientPlayNetworking.send(packet);
    }
}
