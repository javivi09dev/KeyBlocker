package me.javivi.kb.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record KeyBindingListPacket(List<String> keyBindings) implements CustomPayload {
    
    public static final CustomPayload.Id<KeyBindingListPacket> PACKET_ID = 
        new CustomPayload.Id<>(Identifier.of("keyblocker", "keybinding_list"));
    
    public static final PacketCodec<RegistryByteBuf, KeyBindingListPacket> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()), KeyBindingListPacket::keyBindings,
            KeyBindingListPacket::new
    );
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
