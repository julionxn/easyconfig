package net.pulga22.easyconfig.packets;

import net.minecraft.util.Identifier;

public class ECPackets {

    public static final Identifier REQUEST_SYNC = getIdentifier("request_sync");
    public static final Identifier SYNC_ALL = getIdentifier("sync_all");
    public static final Identifier SYNC_ONE = getIdentifier("sync_one");

    private static Identifier getIdentifier(String name){
        return new Identifier("easyconfig", name);
    }

}
