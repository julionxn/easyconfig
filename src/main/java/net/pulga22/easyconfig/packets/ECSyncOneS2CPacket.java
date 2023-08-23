package net.pulga22.easyconfig.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.pulga22.easyconfig.Configurations;
import net.pulga22.easyconfig.SimpleConfig;
import net.pulga22.easyconfig.enums.ConfigType;

public class ECSyncOneS2CPacket {
    public static void onClient(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                PacketByteBuf buf, PacketSender packetSender) {

        SimpleConfig<?> config = Configurations.getConfig(buf.readString());
        if (config == null) return;
        String key = buf.readString();
        ConfigType configType = buf.readEnumConstant(ConfigType.class);
        switch (configType){
            case BOOLEAN -> config.setBoolean(key, buf.readBoolean());
            case INT -> config.setInteger(key, buf.readInt());
            case FLOAT -> config.setFloat(key, buf.readFloat());
            case DOUBLE -> config.setDouble(key, buf.readDouble());
            case STRING -> config.setString(key, buf.readString());
        }

    }
}
