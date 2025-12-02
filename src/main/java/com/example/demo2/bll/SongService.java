package com.example.demo2.bll;

import com.example.demo2.dal.SongDAO;
import com.example.demo2.entities.Song;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for songs.
 */
public class SongService {
    private final SongDAO songDAO = new SongDAO();

    //Gets all songs
    public List<Song> getAll() throws SQLException {
        return songDAO.findAll();
    }

    //Allows the user to search for songs
    public List<Song> search(String query) throws SQLException {
        if (query == null || query.isBlank()) return getAll();
        return songDAO.search(query);
    }

    //Add a new song
    public Song create(Song s) throws SQLException {
        //We would like to make sure that we get all info
        if (s.getTitle() == null || s.getTitle().trim().isEmpty()){
            throw new SQLException("Please enter a title");
        }
        if (s.getArtist() == null || s.getArtist().trim().isEmpty()){
            throw new SQLException("Please enter the song's artist");
        }
        if(s.getFilePath() == null || s.getFilePath().trim().isEmpty()){
            throw new SQLException("Please make sure the file path is correct");
        }

        //Then make sure the actual file exists (mby? idk if we need this)
        File file = new File(s.getFilePath());
        if (!file.exists()){
            throw new SQLException("File does not exist" + s.getFilePath());
        }

        return songDAO.insert(s);
    }

    //Update the song
    public void update(Song s) throws SQLException {
        songDAO.update(s);
        //Maybe put in some validation here like the one above for creating the song
    }

    //Delete the song
    public boolean delete(int songId) throws SQLException {
        return songDAO.delete(songId);
    }
}
