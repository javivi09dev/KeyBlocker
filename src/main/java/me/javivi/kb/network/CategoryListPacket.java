package me.javivi.kb.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record CategoryListPacket(List<String> categories) implements CustomPayload {
    
    public static final CustomPayload.Id<CategoryListPacket> PACKET_ID = 
        new CustomPayload.Id<>(Identifier.of("keyblocker", "category_list"));
    
    public static final PacketCodec<RegistryByteBuf, CategoryListPacket> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()), CategoryListPacket::categories,
            CategoryListPacket::new
    );
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
