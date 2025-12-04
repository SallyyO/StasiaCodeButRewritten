package com.example.demo2.dal;

import com.example.demo2.entities.Playlist;
import com.example.demo2.entities.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for playlists and their song ordering.
 */
public class PlaylistDAO {

    //gets all playlists
    public List<Playlist> findAll() throws SQLException {
       //Think this might look a bit more organized?
        List<Playlist> playlists = new ArrayList<>();

       Connection connection = DBManager.getConnection();
       Statement statement = connection.createStatement();
       ResultSet resultSet = statement.executeQuery("SELECT id, name FROM playlists ORDER BY name");

       while (resultSet.next()) {
           Playlist playlist = new Playlist(resultSet.getInt("id"), resultSet.getString("name"));
           playlists.add(playlist);
       }

       resultSet.close();
       statement.close();
       connection.close();
       return playlists;
        /* String sql = "SELECT id, name FROM playlists ORDER BY name COLLATE NOCASE";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Playlist> list = new ArrayList<>();
            while (rs.next()) list.add(new Playlist(rs.getInt("id"), rs.getString("name")));
            return list;
        } */
    }

    //Add a new playlist
    public Playlist insert(Playlist p) throws SQLException {
       // Maybe looks better?
        Connection connection = DBManager.getConnection();
       PreparedStatement preparedStatement = connection.prepareStatement(
               "INSERT INTO playlists (name) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS
       );

       preparedStatement.setString(1, p.getName());
       preparedStatement.executeUpdate();

       //Get the new ID that was just created
        ResultSet keys = preparedStatement.getGeneratedKeys();
        if (keys.next()) {
            p.setId(keys.getInt(1));
        }

        keys.close();
        preparedStatement.close();
        connection.close();
        return p;

        /* String sql = "INSERT INTO playlists(name) VALUES(?)";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
            return p;
        }
         */
    }

    //Update a playlist's name
    public void update(Playlist p) throws SQLException {
       //Same thing again
        Connection connection = DBManager.getConnection();
        PreparedStatement ps =  connection.prepareStatement("UPDATE playlists SET name = ? WHERE id = ?");

        ps.setString(1, p.getName());
        ps.setInt(2, p.getId());
        ps.executeUpdate();

        ps.close();
        connection.close();
        /* String sql = "UPDATE playlists SET name=? WHERE id=?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getId());
            ps.executeUpdate();
        }
         */
    }

    //Delete a playlist
    public boolean delete(int playlistId) throws SQLException {
      Connection connection = DBManager.getConnection();
      PreparedStatement ps =  connection.prepareStatement("DELETE FROM playlists WHERE id = ?");

      ps.setInt(1, playlistId);
      int deleted = ps.executeUpdate();

      ps.close();
      connection.close();
      return deleted > 0;
       /* String sql = "DELETE FROM playlists WHERE id=?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            return ps.executeUpdate() > 0;
        }
        */
    }

    // Get all songs from a playlist
    public List<Song> getSongs(int playlistId) throws SQLException {
       List<Song> songs = new ArrayList<>();

       Connection connection = DBManager.getConnection();
       PreparedStatement ps =  connection.prepareStatement(
               "SELECT s.id, s.title, s.artist, s.duration_seconds, s.file_path " +
                    "FROM playlist_songs ps JOIN songs s ON ps.song_id = s.id " +
                    "WHERE ps.playlist_id = ? ORDER BY ps.position"
       );

       ps.setInt(1, playlistId);
       ResultSet resultSet = ps.executeQuery();

       while (resultSet.next()) {
           Song song = new Song(
                   resultSet.getInt("id"),
                   resultSet.getString("title"),
                   resultSet.getString("artist"),
                   resultSet.getInt("duration_seconds"),
                   resultSet.getString("file_path")
           );
           songs.add(song);
       }

       resultSet.close();
       ps.close();
       connection.close();
       return songs;


        /* String sql = "SELECT s.id, s.title, s.artist, s.duration_seconds, s.file_path " +
                "FROM playlist_songs ps JOIN songs s ON ps.song_id = s.id " +
                "WHERE ps.playlist_id=? ORDER BY ps.position";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Song> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Song(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("artist"),
                            rs.getInt("duration_seconds"),
                            rs.getString("file_path")));
                }
                return list;
            }
        }
        */
    }

    // Adds a song to the END of a playlist
    public void addSongToEnd(int playlistId, int songId) throws SQLException {
      /* Connection connection = DBManager.getConnection();

      //Helps finding what position to add the new song at
        PreparedStatement maxPS = connection.prepareStatement(
                "SELECT COALESCE(MAX(position), -1) as max_pos FROM playlist_songs WHERE playlist_id"
        );
        maxPS.setInt( 1, playlistId); // !! Figure out what's wrong with this one !!
        ResultSet resultSet = maxPS.executeQuery();

        int nextPosition = 0;
        if (resultSet.next()) {
            nextPosition = resultSet.getInt("max_pos") + 1;
        }
        resultSet.close();
        maxPS.close();

        //Now add the song
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO playlist_songs (playlist_id, position, song_id) VALUES (?, ?, ?)"
        );

        ps.setInt(1, playlistId);
        ps.setInt(2, nextPosition);
        ps.setInt(3, songId);
        ps.executeUpdate();
        ps.close();

        connection.close();

       */
        String maxSql = "SELECT COALESCE(MAX(position), -1) FROM playlist_songs WHERE playlist_id=?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement maxPs = c.prepareStatement(maxSql)) {
            c.setAutoCommit(false);
            int nextPos = 0;
            try {
                maxPs.setInt(1, playlistId);
                try (ResultSet rs = maxPs.executeQuery()) {
                    if (rs.next()) nextPos = rs.getInt(1) + 1;
                }
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO playlist_songs(playlist_id, position, song_id) VALUES(?,?,?)")) {
                    ins.setInt(1, playlistId);
                    ins.setInt(2, nextPos);
                    ins.setInt(3, songId);
                    ins.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    //Removes a song at a given position
    public void removeAtPosition(int playlistId, int position) throws SQLException {
       Connection connection = DBManager.getConnection();

       //Delete the song
        PreparedStatement ps1 = connection.prepareStatement(
                "DELETE FROM playlist_songs WHERE playlist_id=? AND position=?"
        );
        ps1.setInt(1, playlistId);
        ps1.setInt(2, position);
        ps1.executeUpdate();
        ps1.close();

        //Moves the other songs up, so we don't get a weird hole somewhere in the playlist
        PreparedStatement ps2 = connection.prepareStatement(
                "UPDATE playlist_songs SET position = position -1 WHERE playlist_id=? AND position > ?"
        );
        ps2.setInt(1, playlistId);
        ps2.setInt(2, position);
        ps2.executeUpdate();
        ps2.close();

        connection.close();
        /* try (Connection c = DBManager.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement del = c.prepareStatement(
                    "DELETE FROM playlist_songs WHERE playlist_id=? AND position=?");
                 PreparedStatement shift = c.prepareStatement(
                         "UPDATE playlist_songs SET position = position - 1 WHERE playlist_id=? AND position > ?")) {
                del.setInt(1, playlistId);
                del.setInt(2, position);
                del.executeUpdate();

                shift.setInt(1, playlistId);
                shift.setInt(2, position);
                shift.executeUpdate();
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } */
    }

    // Lets the user move songs
    public void move(int playlistId, int fromPos, int toPos) throws SQLException {
        if (fromPos == toPos) return;
        try (Connection c = DBManager.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Temporarily set fromPos to -1 to free the slot
                try (PreparedStatement temp = c.prepareStatement(
                        "UPDATE playlist_songs SET position=-1 WHERE playlist_id=? AND position=?")) {
                    temp.setInt(1, playlistId);
                    temp.setInt(2, fromPos);
                    temp.executeUpdate();
                }
                // We shift the other songs
                if (fromPos < toPos) {
                    // User wants to move song down, so we shift the other songs up
                    try (PreparedStatement shiftDown = c.prepareStatement(
                            "UPDATE playlist_songs SET position=position-1 WHERE playlist_id=? AND position>? AND position<=?")) {
                        shiftDown.setInt(1, playlistId);
                        shiftDown.setInt(2, fromPos);
                        shiftDown.setInt(3, toPos);
                        shiftDown.executeUpdate();
                    }
                } else {
                    // User wants to move song up, so we shift the other songs down
                    try (PreparedStatement shiftUp = c.prepareStatement(
                            "UPDATE playlist_songs SET position=position+1 WHERE playlist_id=? AND position>=? AND position<?")) {
                        shiftUp.setInt(1, playlistId);
                        shiftUp.setInt(2, toPos);
                        shiftUp.setInt(3, fromPos);
                        shiftUp.executeUpdate();
                    }
                }
                // Puts the song in the new position
                try (PreparedStatement place = c.prepareStatement(
                        "UPDATE playlist_songs SET position=? WHERE playlist_id=? AND position=-1")) {
                    place.setInt(1, toPos);
                    place.setInt(2, playlistId);
                    place.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
