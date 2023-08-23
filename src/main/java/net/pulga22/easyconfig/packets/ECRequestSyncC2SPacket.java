package net.pulga22.easyconfig.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pulga22.easyconfig.Configurations;
import net.pulga22.easyconfig.SimpleConfig;
import net.pulga22.easyconfig.enums.ConfigType;
import net.pulga22.easyconfig.enums.SyncOption;
import net.pulga22.easyconfig.enums.SyncTimeOption;
import org.apache.commons.lang3.SerializationUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ECRequestSyncC2SPacket {

    public static void onServer(MinecraftServer server, ServerPlayerEntity serverPlayer,
                                ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {

        server.execute(() -> Configurations.getConfigs().forEach((name, simpleConfig) -> {
            HashSet<SyncTimeOption> syncTimeOptions = simpleConfig.getSyncTimeOptions();
            if (!syncTimeOptions.contains(SyncTimeOption.ON_SERVER_JOIN)) return;
            SyncOption syncOption = simpleConfig.getSyncOption();
            if (syncOption == SyncOption.NONE) return;
            if (syncOption == SyncOption.ALL_S2C){
                syncAll(name, serverPlayer, simpleConfig);
            } else if (syncOption == SyncOption.SOME_S2C) {
                syncSome(name, serverPlayer, simpleConfig, simpleConfig.getSyncValues());
            }
        }));
    }

    private static void syncAll(String name, ServerPlayerEntity player, SimpleConfig<?> simpleConfig){
        syncBunch(name, player, ConfigType.BOOLEAN, simpleConfig.getBooleans());
        syncBunch(name, player, ConfigType.INT, simpleConfig.getIntegers());
        syncBunch(name, player, ConfigType.FLOAT, simpleConfig.getFloats());
        syncBunch(name, player, ConfigType.DOUBLE, simpleConfig.getDoubles());
        syncBunch(name, player, ConfigType.STRING, simpleConfig.getStrings());
    }

    private static void syncSome(String name, ServerPlayerEntity player, SimpleConfig<?> simpleConfig, HashSet<String> syncValues){
        syncBunch(name, player, ConfigType.BOOLEAN, filterMap(syncValues, simpleConfig.getBooleans()));
        syncBunch(name, player, ConfigType.INT, filterMap(syncValues, simpleConfig.getIntegers()));
        syncBunch(name, player, ConfigType.FLOAT, filterMap(syncValues, simpleConfig.getFloats()));
        syncBunch(name, player, ConfigType.DOUBLE, filterMap(syncValues, simpleConfig.getDoubles()));
        syncBunch(name, player, ConfigType.STRING, filterMap(syncValues, simpleConfig.getStrings()));
    }

    private static <T> HashMap<String, T> filterMap(HashSet<String> acceptedValues, HashMap<String, T> map){
        HashMap<String, T> filteredMap = new HashMap<>();
        for (Map.Entry<String, T> entry : map.entrySet()) {
            if (acceptedValues.contains(entry.getKey())) {
                filteredMap.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredMap;
    }

    private static void syncBunch(String name, ServerPlayerEntity player, ConfigType configType, HashMap<String, ?> syncValues){
        if (syncValues.isEmpty()) return;
        byte[] data = SerializationUtils.serialize(syncValues);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(name);
        buf.writeEnumConstant(configType);
        buf.writeByteArray(data);
        ServerPlayNetworking.send(player, ECPackets.SYNC_ALL, buf);
    }
}
