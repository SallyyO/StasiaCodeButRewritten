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

       Connection connection = DBManager.getConnection();
       Statement statement = connection.createStatement();
       ResultSet resultSet = statement.executeQuery(
               "SELECT id, title, artist, duration_seconds, file_path FROM songs ORDER BY title"
       );

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
       statement.close();
       connection.close();
       return songs;
      /* //Try-with-resources used here
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
        return songs; */
    }
    // Lets the user search for songs either by title or artist
    public List<Song> search(String query) throws SQLException {
      List<Song> songs = new ArrayList<>();

      Connection connection = DBManager.getConnection();
      PreparedStatement ps = connection.prepareStatement(
              "SELECT id, title, artist, duration_seconds, file_path FROM songs" +
                      "WHERE title LIKE ? OR artist LIKE ? ORDER BY title COLLATE NOCASE"
      );

      String searchInput = "%" + query + "%";
      ps.setString(1, searchInput);
      ps.setString(2, searchInput);

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

        /*if (query == null || query.isBlank()) {
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
       return songs; */

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

    //Add a song (and put it into the database)
    public Song insert(Song song) throws SQLException {
       Connection connection = DBManager.getConnection();
       PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO songs(title, artist, duration_seconds,file_path) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
       );

        ps.setString(1, song.getTitle());
        ps.setString(2, song.getArtist());
        ps.setInt(3, song.getDurationSeconds());
        ps.setString(4, song.getFilePath());
        ps.executeUpdate();

        //Get the new Id
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            song.setId(keys.getInt(1));
        }

        keys.close();
        ps.close();
        connection.close();
        return song;
    }

    //Update a song
    public void update(Song song) throws SQLException {
        Connection connection = DBManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "UPDATE songs SET title=?, artist=?, duration_seconds=?, file_path=? WHERE id=?"
        );
        ps.setString(1, song.getTitle());
        ps.setString(2, song.getArtist());
        ps.setInt(3, song.getDurationSeconds());
        ps.setString(4, song.getFilePath());
        ps.setInt(5, song.getId());
        ps.executeUpdate();

        ps.close();
        connection.close();
    }

    // Lets user delete a song by its ID
    public boolean delete(int id) throws SQLException {
        Connection connection = DBManager.getConnection();
        PreparedStatement ps = connection.prepareStatement("DELETE FROM songs WHERE id=?");

        ps.setInt(1, id);
        int deleted = ps.executeUpdate();

        ps.close();
        connection.close();
        return deleted > 0;
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
