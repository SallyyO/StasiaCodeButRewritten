package com.example.demo2.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * DBManager for test-database :D
 * Definitely not because we learned the hard way why we should make sure to have this
 */
public final class TestDBManager {

    private static final String TEST_DB_URL = "jdbc:sqlite:my_tunes.db";
    private static volatile boolean initialized = false;

    private TestDBManager() {}

    static{
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {}
    }

    //Connect to the test-db
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(TEST_DB_URL);
        ensureInitialized(connection);
        return connection;
    }

    private static void ensureInitialized(Connection connection) throws SQLException {
        if(initialized) return;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("PRAGMA foreign_keys=ON;");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS songs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "artist TEXT NOT NULL, " +
                    "duration_seconds INTEGER NOT NULL DEFAULT 0, " +
                    "file_path TEXT NOT NULL)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS playlists (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS playlist_songs (" +
                    "playlist_id INTEGER NOT NULL, " +
                    "position INTEGER NOT NULL, " +
                    "song_id INTEGER NOT NULL, " +
                    "PRIMARY KEY (playlist_id, position), " +
                    "FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE)");
        }
        initialized = true;
    }

    public static void cleanDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM playlist_songs");
            stmt.executeUpdate("DELETE FROM playlists");
            stmt.executeUpdate("DELETE FROM songs");
        }
    }

    public static void resetInitialization() throws SQLException {
        initialized = false;
    }
}