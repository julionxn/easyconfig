package net.pulga22.easyconfig.enums;

public enum ConfigType {
    BOOLEAN("Boolean"),
    INT("Integer"),
    FLOAT("Float"),
    DOUBLE("Double"),
    STRING("String");

    public final String equivalent;

    ConfigType(String equivalent) {
        this.equivalent = equivalent;
    }
}
