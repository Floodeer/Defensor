package com.floodeer.plugins.towerdefense.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

public interface Database {

    void connect() throws SQLException;

    void close();

    void createTables() throws IOException, SQLException;

    AtomicReference<Connection> getConnection();

    Executor getExecutor();

    boolean checkConnection();

    boolean columnExists(String column);

    void addColumn(String value) throws IOException, SQLException;

}
