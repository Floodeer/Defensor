package com.floodeer.plugins.towerdefense.database;

import com.floodeer.plugins.towerdefense.Defensor;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class MySQL implements Database {

    private final String connectionUri;
    private final String username;
    private final String password;

    private AtomicReference<Connection> connection;
    private ExecutorService pool;

    public MySQL(String hostname, String database, String username, String password, int port)
            throws ClassNotFoundException, SQLException {
        connectionUri = String.format("jdbc:mysql://%s:%d/%s", hostname, port, database);
        this.username = username;
        this.password = password;
        try {
            pool = Executors.newCachedThreadPool();
            connection = new AtomicReference<>();
            Class.forName("com.mysql.jdbc.Driver");
            connect();
        } catch (SQLException sqlException) {
            close();
        }
    }

    @Override
    public void connect() throws SQLException {
        if (connection.get() != null) {
            try {
                connection.get().createStatement().execute("SELECT 1;");
            } catch (SQLException sqlException) {
                if (sqlException.getSQLState().equals("08S01")) {
                    try {
                        connection.get().close();
                    } catch (SQLException ignored) {
                    }
                }
            }
        }
        if (connection.get() == null || connection.get().isClosed())
            connection.set(DriverManager.getConnection(connectionUri, username, password));
    }

    @Override
    public void close() {
        try {
            if (connection.get() != null && !connection.get().isClosed()) {
                connection.get().close();
            }

        } catch (SQLException ignored) {

        }
        connection.set(null);
    }

    @Override
    public AtomicReference<Connection> getConnection() {
        return connection;
    }

    @Override
    public Executor getExecutor() {
        return pool;
    }

    @Override
    public boolean checkConnection() {
        try {
            connect();
        } catch (SQLException ex) {
            close();
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void createTables() throws IOException, SQLException {
        URL resource = Resources.getResource(Defensor.class, "/tables.sql");
        String[] databaseStructure = Resources.toString(resource, Charsets.UTF_8).split(";");

        if (databaseStructure.length == 0) {
            return;
        }

        Statement statement = null;

        try {
            connection.get().setAutoCommit(false);
            statement = connection.get().createStatement();
            for (String query : databaseStructure) {
                query = query.trim();

                if (query.isEmpty()) {
                    continue;
                }
                statement.execute(query);
            }
            connection.get().commit();
        } finally {
            connection.get().setAutoCommit(true);
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
        }
    }

    @Override
    public boolean columnExists(String column) {
        Connection conn = getConnection().get();
        DatabaseMetaData metadata;
        try {
            metadata = conn.getMetaData();
            ResultSet resultSet;
            resultSet = metadata.getTables(null, null, "defensor_player", null);
            if (resultSet.next()) {
                resultSet = metadata.getColumns(null, null, "defensor_player", column);
                if (!resultSet.next()) {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void addColumn(String value) throws IOException, SQLException {
        Statement statement = null;
        Connection connection = this.connection.get();
        try {
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            String query = "ALTER TABLE " + "defensor_player" + " ADD " + value + " AFTER uuid";
            statement.execute(query);
            connection.commit();

        } finally {
            connection.setAutoCommit(true);
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
        }
    }
}
