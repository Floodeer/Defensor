package com.floodeer.plugins.towerdefense.database;

public interface MultiCallback<T, V> {

    void onCall(T resultA, V resultB);
}
