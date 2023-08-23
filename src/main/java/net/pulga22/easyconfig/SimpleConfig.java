package net.pulga22.easyconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.pulga22.easyconfig.enums.ConfigType;
import net.pulga22.easyconfig.enums.SyncOption;
import net.pulga22.easyconfig.enums.SyncTimeOption;
import net.pulga22.easyconfig.packets.ECPackets;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class SimpleConfig<T> {

    private final File configFile;
    private final Class<T> pattern;
    private final boolean autoSave;
    private final SyncOption syncOption;
    private final HashSet<String> syncValues;
    private final HashSet<SyncTimeOption> syncTimeOptions;
    private final Queue<Map.Entry<String, ?>> valuesToSave = new LinkedList<>();
    private final HashMap<String, Boolean> booleans = new HashMap<>(0);
    private final HashMap<String, Integer> integers = new HashMap<>(0);
    private final HashMap<String, Float> floats = new HashMap<>(0);
    private final HashMap<String, Double> doubles = new HashMap<>(0);
    private final HashMap<String, String> strings = new HashMap<>(0);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private T config;
    private MinecraftServer server;

    private SimpleConfig(File configFile, Class<T> pattern, boolean autoSave, SyncOption syncOption, HashSet<String> syncValues, HashSet<SyncTimeOption> syncTimeOptions) {
        this.configFile = configFile;
        this.pattern = pattern;
        this.autoSave = autoSave;
        this.syncOption = syncOption;
        this.syncValues = syncValues;
        this.syncTimeOptions = syncTimeOptions;
    }

    private void load(){
        try {
            boolean isNew = this.configFile.createNewFile();
            if (isNew){
                this.config = this.pattern.getDeclaredConstructor().newInstance();
                this.save();
            } else {
                try(FileReader reader = new FileReader(this.configFile)){
                    this.config = gson.fromJson(reader, this.pattern);
                }
            }
            this.initContainers();
        } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e){
            throw new RuntimeException("Something went wrong loading the configuration.");
        }
    }

    public void save(){
        while (!this.valuesToSave.isEmpty()){
            try {
                Map.Entry<String, ?> entry = this.valuesToSave.poll();
                Field field = this.pattern.getField(entry.getKey());
                field.setAccessible(true);
                field.set(this.config, entry.getValue());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Something went wrong saving a value to the configuration.");
            }
        }
        try (FileWriter writer = new FileWriter(this.configFile)){
            gson.toJson(this.config, writer);
        } catch (IOException e){
            throw new RuntimeException("Something went wrong saving the configuration file.");
        }
    }

    private void initContainers(){
        Arrays.stream(this.pattern.getDeclaredFields()).forEach(field -> {
            String fieldName = field.getName();
            ConfigType configType = TypesUtil.getTypeByClass(field.getType());
            if (configType == null) return;
            this.addToContainers(configType, fieldName);
        });
    }

    private void addToContainers(ConfigType type, String name){
        try {
            Object value = this.pattern.getField(name).get(this.config);
            switch (type){
                case BOOLEAN -> this.booleans.put(name, (Boolean) value);
                case INT -> this.integers.put(name, (Integer) value);
                case FLOAT -> this.floats.put(name, (Float) value);
                case DOUBLE -> this.doubles.put(name, (Double) value);
                case STRING -> this.strings.put(name, (String) value);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Something went wrong adding the " + name + " field to the containers.");
        }
    }

    public Boolean getBoolean(String key){
        return this.booleans.get(key);
    }

    public Integer getInteger(String key){
        return this.integers.get(key);
    }

    public Float getFloat(String key){
        return this.floats.get(key);
    }

    public Double getDouble(String key){
        return this.doubles.get(key);
    }

    public String getString(String key){
        return this.strings.get(key);
    }

    @Nullable
    public Object get(String key){
        ConfigType configType = this.getTypeByKey(key);
        if (configType == null) return null;
        return switch (configType){
            case BOOLEAN -> this.getBoolean(key);
            case INT -> this.getInteger(key);
            case FLOAT -> this.getFloat(key);
            case DOUBLE -> this.getDouble(key);
            case STRING -> this.getString(key);
        };
    }

    public void setBoolean(String key, Boolean value){
        if (!this.booleans.containsKey(key)) return;
        this.booleans.put(key, value);
        this.postSet(key, value);
    }

    public void setInteger(String key, Integer value){
        if (!this.integers.containsKey(key)) return;
        this.integers.put(key, value);
        this.postSet(key, value);
    }

    public void setFloat(String key, Float value){
        if (!this.floats.containsKey(key)) return;
        this.floats.put(key, value);
        this.postSet(key, value);
    }

    public void setDouble(String key, Double value){
        if (!this.doubles.containsKey(key)) return;
        this.doubles.put(key, value);
        this.postSet(key, value);
    }

    public void setString(String key, String value){
        if (!this.strings.containsKey(key)) return;
        this.strings.put(key, value);
        this.postSet(key, value);
    }

    public void set(String key, Object value){
        ConfigType configType = TypesUtil.getTypeByClass(value.getClass());
        if (configType == null) return;
        switch (configType){
            case BOOLEAN -> this.setBoolean(key, (Boolean) value);
            case INT -> this.setInteger(key, (Integer) value);
            case FLOAT -> this.setFloat(key, (Float) value);
            case DOUBLE -> this.setDouble(key, (Double) value);
            case STRING -> this.setString(key, (String) value);
        }
    }

    private void postSet(String key, Object value){
        this.valuesToSave.add(new AbstractMap.SimpleEntry<>(key, value));
        boolean should = true;
        if (this.server == null) should = Configurations.setServer(this);
        if (should && FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER
                && this.syncTimeOptions.contains(SyncTimeOption.ON_CONFIG_CHANGE)
                && this.syncOption != SyncOption.NONE
                && this.syncValues.contains(key)) this.sync(key, value);
        if (this.autoSave) this.save();
    }

    private void sync(String key, Object value){
        ConfigType configType = TypesUtil.getTypeByClass(value.getClass());
        if (configType == null) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(this.pattern.getName());
        buf.writeString(key);
        buf.writeEnumConstant(configType);
        switch (configType){
            case BOOLEAN -> buf.writeBoolean((Boolean) value);
            case INT -> buf.writeInt((Integer) value);
            case FLOAT -> buf.writeFloat((Float) value);
            case DOUBLE -> buf.writeDouble((Double) value);
            case STRING -> buf.writeString((String) value);
        }
        PlayerLookup.all(this.server).forEach(player -> {
            ServerPlayNetworking.send(player, ECPackets.SYNC_ONE, buf);
        });
    }

    public boolean serverReady(){
        return this.server != null;
    }

    public void setServer(MinecraftServer server){
        this.server = server;
    }

    public SyncOption getSyncOption() {
        return syncOption;
    }

    public HashSet<String> getSyncValues() {
        return syncValues;
    }

    public HashSet<SyncTimeOption> getSyncTimeOptions() {
        return syncTimeOptions;
    }

    public HashMap<String, Boolean> getBooleans() {
        return booleans;
    }

    public HashMap<String, Integer> getIntegers() {
        return integers;
    }

    public HashMap<String, Float> getFloats() {
        return floats;
    }

    public HashMap<String, Double> getDoubles() {
        return doubles;
    }

    public HashMap<String, String> getStrings() {
        return strings;
    }

    @Nullable
    public ConfigType getTypeByKey(String key){
        if (this.booleans.containsKey(key)) return ConfigType.BOOLEAN;
        if (this.integers.containsKey(key)) return ConfigType.INT;
        if (this.floats.containsKey(key)) return ConfigType.FLOAT;
        if (this.doubles.containsKey(key)) return ConfigType.DOUBLE;
        if (this.strings.containsKey(key)) return ConfigType.STRING;
        return null;
    }

    public static <T> Builder<T> builder(String modID, Class<T> pattern){
        return new Builder<>(modID, pattern);
    }

    public static class Builder<T>{

        private final File configFile;
        private final Class<T> pattern;
        private boolean autoSave = false;
        private SyncOption syncOption = SyncOption.NONE;
        private final HashSet<String> syncValues = new HashSet<>();
        private final HashSet<SyncTimeOption> syncTimeOptions = new HashSet<>();

        public Builder(String modID, Class<T> pattern){
            this.configFile = FabricLoader.getInstance().getConfigDir().resolve(modID + ".json").toFile();
            this.checkPattern(pattern);
            this.pattern = pattern;
        }

        private void checkPattern(Class<?> pattern){
            int classModifiers = pattern.getModifiers();
            if (Modifier.isAbstract(classModifiers)){
                throw new RuntimeException("Pattern class cannot be abstract.");
            }
            if (Modifier.isPrivate(classModifiers)){
                throw new RuntimeException("Pattern class should be public.");
            }
            if (Modifier.isInterface(classModifiers)){
                throw new RuntimeException("Pattern class cannot be an interface.");
            }
            try {
                Object testObject = pattern.getDeclaredConstructor().newInstance();
                Arrays.stream(pattern.getDeclaredFields()).forEach(field -> {
                    ConfigType configType = TypesUtil.getTypeByClass(field.getType());
                    if (configType == null){
                        throw new RuntimeException("Field \"" + field.getName() + "\" is not of an admitted type.");
                    }
                    if (field.getType().isPrimitive()){
                        throw new RuntimeException("Field \"" + field.getName() + "\" cannot be of a primitive type.");
                    }
                    if (Modifier.isStatic(field.getModifiers())){
                        throw new RuntimeException("Field \"" + field.getName() + "\" cannot be static.");
                    }
                    try {
                        field.setAccessible(true);
                        Object value = field.get(testObject);
                        if (value == null){
                            throw new RuntimeException("Field \"" + field.getName() + "\" needs to be initialized.");
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Something went wrong checking the field \"" + field.getName() + "\".");
                    }
                });
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Something went wrong checking the pattern " + pattern.getName() + " while creating an instance.");
            }
        }

        public Builder<T> autoSave(){
            this.autoSave = true;
            return this;
        }

        public Builder<T> syncOption(SyncOption syncOption){
            this.syncOption = syncOption;
            return this;
        }

        public Builder<T> syncTime(SyncTimeOption... timeOption){
            Arrays.stream(timeOption).sequential().forEach(this.syncTimeOptions::add);
            return this;
        }

        public SimpleConfig<T> build(){
            HashSet<String> values = new HashSet<>();
            if (this.syncOption == SyncOption.ALL_S2C){
                for (Field declaredField : this.pattern.getDeclaredFields()) {
                    values.add(declaredField.getName());
                }
                if (this.syncTimeOptions.isEmpty()){
                    this.syncTimeOptions.add(SyncTimeOption.ON_SERVER_JOIN);
                }
            } else if (this.syncOption == SyncOption.SOME_S2C) {
                values = this.syncValues;
                if (this.syncTimeOptions.isEmpty()){
                    this.syncTimeOptions.add(SyncTimeOption.ON_SERVER_JOIN);
                }
            }
            SimpleConfig<T> simpleConfig = new SimpleConfig<>(configFile, pattern, this.autoSave, this.syncOption, values, this.syncTimeOptions);
            Configurations.addConfig(this.pattern, simpleConfig);
            simpleConfig.load();
            return simpleConfig;
        }

    }
}
