package net.pulga22.easyconfig.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.pulga22.easyconfig.Configurations;
import net.pulga22.easyconfig.SimpleConfig;
import net.pulga22.easyconfig.enums.ConfigType;
import org.apache.commons.lang3.SerializationUtils;

import java.util.HashMap;

public class ECSyncBunchS2CPacket {
    public static void onClient(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                PacketByteBuf buf, PacketSender packetSender) {

        String name = buf.readString();
        ConfigType configType = buf.readEnumConstant(ConfigType.class);
        SimpleConfig<?> config = Configurations.getConfig(name);
        if (config == null) return;
        switch (configType){
            case BOOLEAN -> {
                HashMap<String, Boolean> toSave = SerializationUtils.deserialize(buf.readByteArray());
                toSave.forEach(config::setBoolean);
            }
            case INT -> {
                HashMap<String, Integer> toSave = SerializationUtils.deserialize(buf.readByteArray());
                toSave.forEach(config::setInteger);
            }
            case FLOAT -> {
                HashMap<String, Float> toSave = SerializationUtils.deserialize(buf.readByteArray());
                toSave.forEach(config::setFloat);
            }
            case DOUBLE -> {
                HashMap<String, Double> toSave = SerializationUtils.deserialize(buf.readByteArray());
                toSave.forEach(config::setDouble);
            }
            case STRING -> {
                HashMap<String, String> toSave = SerializationUtils.deserialize(buf.readByteArray());
                toSave.forEach(config::setString);
            }
        }
    }
}
