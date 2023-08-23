package net.pulga22.easyconfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.pulga22.easyconfig.enums.ConfigType;
import net.pulga22.easyconfig.packets.ECRequestSyncC2SPacket;
import net.pulga22.easyconfig.packets.ECPackets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class EasyConfig implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("easyconfig");

	@Override
	public void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(ECPackets.REQUEST_SYNC, ECRequestSyncC2SPacket::onServer);
		Arrays.stream(ConfigType.values()).forEach(value -> {
			TypesUtil.addType(value.equivalent, value);
		});
		ServerLifecycleEvents.SERVER_STARTED.register(Configurations::setSERVER);
		LOGGER.info("Hello Fabric world!");
	}
}