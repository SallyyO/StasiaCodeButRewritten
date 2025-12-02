package com.example.demo2.dal;

import com.example.demo2.entities.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for CRUD operations on songs table.
 */
public class SongDAO {

    public List<Song> findAll() throws SQLException {
        String sql = "SELECT id, title, artist, duration_seconds, file_path FROM songs ORDER BY title COLLATE NOCASE";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Song> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    public List<Song> search(String query) throws SQLException {
        String like = "%" + query + "%";
        String sql = "SELECT id, title, artist, duration_seconds, file_path FROM songs " +
                "WHERE title LIKE ? OR artist LIKE ? ORDER BY title COLLATE NOCASE";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Song> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public Song insert(Song s) throws SQLException {
        String sql = "INSERT INTO songs(title, artist, duration_seconds, file_path) VALUES(?,?,?,?)";
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
    }

    public void update(Song s) throws SQLException {
        String sql = "UPDATE songs SET title=?, artist=?, duration_seconds=?, file_path=? WHERE id=?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getTitle());
            ps.setString(2, s.getArtist());
            ps.setInt(3, s.getDurationSeconds());
            ps.setString(4, s.getFilePath());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM songs WHERE id=?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Song map(ResultSet rs) throws SQLException {
        return new Song(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("artist"),
                rs.getInt("duration_seconds"),
                rs.getString("file_path")
        );
    }
}
