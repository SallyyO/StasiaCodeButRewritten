package com.example.demo2.dal;

import java.sql.*;

/**
 * Provides JDBC connection to the embedded SQLite database and initializes schema on first use.
 */
public final class DBManager {
    private static final String DB_URL = "jdbc:sqlite:mytunes.db";
    private static volatile boolean initialized = false;

    private DBManager() {}

    // Ensure the SQLite JDBC driver is loaded in all environments (modular/classpath)
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
            // Modern JDBC auto-loading should still work via ServiceLoader,
            // but explicit loading avoids "No suitable driver" issues on some setups.
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        ensureInitialized(conn);
        return conn;
    }

    private static synchronized void ensureInitialized(Connection conn) throws SQLException {
        if (initialized) return;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("PRAGMA foreign_keys=ON");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS songs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "artist TEXT NOT NULL, " +
                    "duration_seconds INTEGER NOT NULL DEFAULT 0, " +
                    "file_path TEXT NOT NULL)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS playlists (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS playlist_songs (" +
                    "playlist_id INTEGER NOT NULL, " +
                    "position INTEGER NOT NULL, " +
                    "song_id INTEGER NOT NULL, " +
                    "PRIMARY KEY (playlist_id, position), " +
                    "FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE)");
        }
        initialized = true;
    }
}
