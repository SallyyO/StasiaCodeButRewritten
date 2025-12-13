package jUnit;

import com.example.demo2.dal.DBManager;
import com.example.demo2.dal.PlaylistDAO;
import com.example.demo2.dal.SongDAO;
import com.example.demo2.dal.TestDBManager;
import com.example.demo2.entities.Playlist;
import org.junit.Test;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for PlaylistDAO
 * Make sure you have added the right dependencies in the pom file
 * If not, it ain't gon' work
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaylistDAOTest {
    private static PlaylistDAO playlistDAO;
    private static SongDAO songDAO;

    @BeforeEach
    public void setupClass() {
        playlistDAO = new PlaylistDAO();
        songDAO = new SongDAO();
    }

    @BeforeEach
    public void setupTest()  throws SQLException {
        TestDBManager.cleanDatabase();
    }

    private void cleanDatabase() throws SQLException {
        try (Connection conn = DBManager.getConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM playlist_songs");
            statement.executeUpdate("DELETE FROM playlists");
            statement.executeUpdate("DELETE FROM songs");
        }
    }

    @Test
    @Order(1)
    public void testInsert() throws SQLException {
        Playlist playlist = new Playlist("Test playlist");

        Playlist result = playlistDAO.insert(playlist);

        assertNotNull(playlist.getId());
        assertEquals("Test playlist", playlist.getName());
    }

    //Check if you can find multiple playlists
    @Test
    @Order(2)
    public void testFindAll() throws SQLException{
        playlistDAO.insert(new Playlist("Test rock"));
        playlistDAO.insert(new Playlist("Test Jazz"));
        playlistDAO.insert(new Playlist("Test OPERAAAA"));


        List<Playlist> playlists = playlistDAO.findAll();

        assertEquals(3, playlists.size());
    }

    @Test
    @Order(3)
    public void testRenamePlaylist() throws SQLException {
        Playlist playlist = playlistDAO.insert(new Playlist("Test OldName"));
        playlist.setName("Test NewName");

        playlistDAO.update(playlist);
        List<Playlist> playlists = playlistDAO.findAll();

        assertEquals(1, playlists.size());
        assertEquals("Test NewName", playlists.get(0).getName());
    }

    @Test
    @Order(4)
    public void testDeletePlaylist() throws SQLException {

    }
}
