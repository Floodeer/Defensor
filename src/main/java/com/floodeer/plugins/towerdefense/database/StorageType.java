package com.floodeer.plugins.towerdefense.database;

public enum StorageType {

    MYSQL("MYSQL"),
    SQLITE("SQLITE");

    String type;

    StorageType(String type) {
        this.type = type;
    }

    public static String getDriver(StorageType type) {
        if (type == MYSQL) {
            return "com.mysql.jdbc.Driver";
        } else if (type == SQLITE) {
            return "org.sqlite.JDBC";
        }
        return null;
    }

    public static StorageType fromString(String t) {
        for (StorageType type : StorageType.values()) {
            if (t.equalsIgnoreCase(type.toString())) {
                return type;
            }
        }
        return null;
    }
}