package com.floodeer.plugins.towerdefense.database;

import com.floodeer.plugins.towerdefense.Defensor;

import java.io.IOException;
import java.sql.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class SQLite implements Database {

    private AtomicReference<Connection> connection;
    private ExecutorService pool;

    public SQLite() throws ClassNotFoundException, SQLException {
        try {
            pool = Executors.newCachedThreadPool();
            connection = new AtomicReference<>();
            Class.forName("org.sqlite.JDBC");
            connect();
        } catch (SQLException ex) {
            close();
            ex.printStackTrace();
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
        if (connection.get() == null || connection.get().isClosed()) {
            connection.set(DriverManager.getConnection("jdbc:sqlite:" + Defensor.get().getDataFolder() + "/players.db"));
        }
    }

    @Override
    public void close() {
        try {
            if (connection.get() != null && !connection.get().isClosed()) {
                connection.get().close();
            }
        } catch (SQLException ignored) {}
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
    public void createTables() throws SQLException {
        try (Statement statement = getConnection().get().createStatement()) {

            String query = "CREATE TABLE IF NOT EXISTS `defensor_player` ( " +
                    "`player_id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "`uuid` VARCHAR(255) NOT NULL UNIQUE, " +
                    "`playername` VARCHAR(60) NOT NULL, " +
                    "`wins` INT NOT NULL DEFAULT 0, " +
                    "`losses` INT NOT NULL DEFAULT 0, " +
                    "`games_played` INT NOT NULL DEFAULT 0, " +
                    "`wave_record` INT NOT NULL DEFAULT 0, " +
                    "`kills` INT NOT NULL DEFAULT 0, " +
                    "`damage_caused` DOUBLE NOT NULL DEFAULT 0, " +
                    "`balance` INT NOT NULL DEFAULT 0, " +
                    "`exp` INT NOT NULL DEFAULT 0, " +
                    "`rank` VARCHAR(60) NOT NULL DEFAULT 'Level-1', " +
                    "`kit` VARCHAR(60) NOT NULL DEFAULT 'builder', " +
                    "`kits` VARCHAR(9999) NOT NULL DEFAULT 'builder');";

            statement.execute(query);

        } finally {
            connection.get().setAutoCommit(true);
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
    public void addColumn(String value) throws SQLException {
        Statement statement = null;
        Connection connection = this.getConnection().get();
        try {
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            String query = "ALTER TABLE " + "defensor_player" + " ADD COLUMN " + value;
            statement.execute(query);
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
            if (statement != null) {
                statement.close();
            }
        }
    }
}
