package com.example.demo2.bll;

import com.example.demo2.dal.SongDAO;
import com.example.demo2.entities.Song;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for songs.
 */
public class SongService {
    private final SongDAO songDAO = new SongDAO();

    public List<Song> getAll() throws SQLException {
        return songDAO.findAll();
    }

    public List<Song> search(String query) throws SQLException {
        if (query == null || query.isBlank()) return getAll();
        return songDAO.search(query);
    }

    public Song create(Song s) throws SQLException {
        return songDAO.insert(s);
    }

    public void update(Song s) throws SQLException {
        songDAO.update(s);
    }

    public boolean delete(int songId) throws SQLException {
        return songDAO.delete(songId);
    }
}
