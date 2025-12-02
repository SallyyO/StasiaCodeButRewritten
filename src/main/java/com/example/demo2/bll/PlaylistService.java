package com.example.demo2.bll;

import com.example.demo2.dal.PlaylistDAO;
import com.example.demo2.entities.Playlist;
import com.example.demo2.entities.Song;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for playlists and their songs ordering.
 */
public class PlaylistService {
    private PlaylistDAO playlistDAO = new PlaylistDAO(); //WAS FINAL BEFORE, CHANGE BACK IF NEEDED

    //Added constructors to make testing easier/possible without having to touch the DB
    // for prod
    public PlaylistService() {

        this.playlistDAO = new PlaylistDAO();
    }

    //One for testing if needed (dependency injection)
    public PlaylistService (PlaylistDAO mockDAO){
        this.playlistDAO = mockDAO;
    }

    //Gets all playlists
    public List<Playlist> getAll() throws SQLException {
        return playlistDAO.findAll();
    }

    //Creates a new playlist
    public Playlist create(Playlist p) throws SQLException {
        return playlistDAO.insert(p);
    }

    //Lets the user rename a playlist
    public void rename(Playlist p) throws SQLException {
        playlistDAO.update(p);
    }

    //Deletes a playlist
    public boolean delete(int playlistId) throws SQLException {
        return playlistDAO.delete(playlistId);
    }

    //Gets the songs from the playlist
    public List<Song> getSongs(int playlistId) throws SQLException {
        return playlistDAO.getSongs(playlistId);
    }

    //Adds a song to the playlist
    public void addSongToEnd(int playlistId, int songId) throws SQLException {
        playlistDAO.addSongToEnd(playlistId, songId);
    }

    //Removes a song from the playlist
    public void removeAtPosition(int playlistId, int position) throws SQLException {
        playlistDAO.removeAtPosition(playlistId, position);
    }

    //Moves a song from one position to another in the playlist
    public void move(int playlistId, int fromPos, int toPos) throws SQLException {
        playlistDAO.move(playlistId, fromPos, toPos);
    }
}
