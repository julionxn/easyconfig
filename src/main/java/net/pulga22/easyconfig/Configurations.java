package net.pulga22.easyconfig;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Configurations {

    private static MinecraftServer SERVER;
    private static final HashMap<String, SimpleConfig<?>> configs = new HashMap<>();

    public static boolean setServer(SimpleConfig<?> config){
        if (SERVER == null) return false;
        if (config.serverReady()) return true;
        config.setServer(SERVER);
        return true;
    }

    public static void addConfig(Class<?> pattern, SimpleConfig<?> simpleConfig){
        configs.put(pattern.getName(), simpleConfig);
    }

    @Nullable
    public static SimpleConfig<?> getConfig(String name){
        return configs.get(name);
    }

    public static HashMap<String, SimpleConfig<?>> getConfigs(){
        return configs;
    }

    protected static void setSERVER(MinecraftServer server){
        SERVER = server;
    }

}
