package com.example.demo2.dal;

import com.example.demo2.entities.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for CRUD operations on songs table.
 */
public class SongDAO {

    // Gets all songs - here they're ordered by title
    public List<Song> findAll() throws SQLException {
       List<Song> songs = new ArrayList<>();

       //Try-with-resources used here
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
             "SELECT id, title, artist, duration_seconds, file_path FROM songs ORDER BY title COLLATE NOCASE"
             );
             ResultSet rs = ps.executeQuery()) {

            //List<Song> list = new ArrayList<>();
            while (rs.next()) {
                songs.add(mapToSong(rs));
            }
        }
        return songs;
    }

    public List<Song> search(String query) throws SQLException {
       if (query == null || query.isBlank()) {
           return findAll();
       }

       List<Song> songs = new ArrayList<>();
       String searchTerm = "%" + query.trim() + "%";

       try (Connection c = DBManager.getConnection();
            PreparedStatement ps = c.prepareStatement(
                    "SELECT id, title, artist, duration_seconds, file_path FROM songs" +
                            "WHERE title LIKE ? OR artist LIKE ? ORDER BY title COLLATE NOCASE")) {
           ps.setString(1, searchTerm);
           ps.setString(2, searchTerm);

           try(ResultSet rs = ps.executeQuery()) {
               while (rs.next()) {
                   songs.add(mapToSong(rs));
               }
           }
       }
       return songs;

        /* String like = "%" + query + "%";
        String sql = "SELECT id, title, artist, duration_seconds, file_path FROM songs " +
                "WHERE title LIKE ? OR artist LIKE ? ORDER BY title COLLATE NOCASE";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Song> list = new ArrayList<>();
                while (rs.next()) list.add(mapToSong(rs));
                return list;
            }
        }
         */
    }

    //Inserts a new song into the database
    public Song insert(Song s) throws SQLException {
       try (Connection c = DBManager.getConnection();
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO songs(title, artist, duration_seconds,file_path) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
           ps.setString(1, s.getTitle());
           ps.setString(2, s.getArtist());
           ps.setInt(3, s.getDurationSeconds());
           ps.setString(4, s.getFilePath());
           ps.executeUpdate();

           //Get the new Id
           try (ResultSet keys = ps.getGeneratedKeys()) {
               if (keys.next()) {
                   s.setId(keys.getInt(1));
               }
           }
       }
       return s;
        /* String sql = "INSERT INTO songs(title, artist, duration_seconds, file_path) VALUES(?,?,?,?)";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getTitle());
            ps.setString(2, s.getArtist());
            ps.setInt(3, s.getDurationSeconds());
            ps.setString(4, s.getFilePath());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getInt(1));
            }
            return s;
        }
         */
    }

    //Update a song
    public void update(Song s) throws SQLException {
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE songs SET title=?, artist=?, duration_seconds=?, file_path=? WHERE id=?")) {
            ps.setString(1, s.getTitle());
            ps.setString(2, s.getArtist());
            ps.setInt(3, s.getDurationSeconds());
            ps.setString(4, s.getFilePath());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        }
    }

    // Lets user delete a song by its ID
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM songs WHERE id=?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Used to map a ResultSet row to a Song
    private static Song mapToSong(ResultSet rs) throws SQLException {
        return new Song(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("artist"),
                rs.getInt("duration_seconds"),
                rs.getString("file_path")
        );
    }
}
