package net.pulga22.easyconfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.pulga22.easyconfig.packets.ECSyncBunchS2CPacket;
import net.pulga22.easyconfig.packets.ECSyncOneS2CPacket;
import net.pulga22.easyconfig.packets.ECPackets;

public class EasyConfigClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ECPackets.SYNC_ALL, ECSyncBunchS2CPacket::onClient);
        ClientPlayNetworking.registerGlobalReceiver(ECPackets.SYNC_ONE, ECSyncOneS2CPacket::onClient);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientPlayNetworking.send(ECPackets.REQUEST_SYNC, PacketByteBufs.empty()));
    }

}
