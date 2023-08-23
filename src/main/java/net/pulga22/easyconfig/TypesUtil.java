package net.pulga22.easyconfig;

import net.pulga22.easyconfig.enums.ConfigType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TypesUtil {

    private static final HashMap<String, ConfigType> TYPES = new HashMap<>();

    public static void addType(String classType, ConfigType configType){
        TYPES.put(classType, configType);
    }

    @Nullable
    public static ConfigType getTypeByClass(Class<?> classType){
        return TYPES.get(classType.getSimpleName());
    }

}
