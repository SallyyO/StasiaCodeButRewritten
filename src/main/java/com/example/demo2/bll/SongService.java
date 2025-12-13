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
    private SongDAO songDAO = new SongDAO(); //Was also final before, could probably be changed back if needed

    public SongService() {
        this.songDAO = new SongDAO();
    }

    public SongService(SongDAO sMockDAO){
        this.songDAO = sMockDAO;
    }

    //Gets all songs
    public List<Song> getAll() throws SQLException {
        return songDAO.findAll();
    }

    //Allows the user to search for songs
    public List<Song> search(String query) throws SQLException {
        if (query == null || query.isBlank())
            return getAll();
        return songDAO.search(query);
    }

    //Add a new song
    public Song create(Song song) throws SQLException {

        //We would like to make sure that we get all info
        if (song.getTitle() == null || song.getTitle().trim().isEmpty()){
            throw new SQLException("Please enter a title");
        }
        if (song.getArtist() == null || song.getArtist().trim().isEmpty()){
            throw new SQLException("Please enter the song's artist");
        }
        if(song.getFilePath() == null || song.getFilePath().trim().isEmpty()){
            throw new SQLException("Please make sure the file path is correct");
        }

        //Then make sure the actual file exists
        File file = new File(song.getFilePath());
        if (!file.exists()){
            throw new SQLException("File does not exist" + song.getFilePath());
        }

        return songDAO.insert(song);
    }

    //Update the song
    public void update(Song song) throws SQLException {
        songDAO.update(song);
    }

    //Delete the song
    public boolean delete(int songId) throws SQLException {
        return songDAO.delete(songId);
    }

}
