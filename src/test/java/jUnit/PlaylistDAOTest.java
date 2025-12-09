package jUnit;

import com.example.demo2.dal.PlaylistDAO;
import com.example.demo2.dal.SongDAO;
import com.example.demo2.entities.Playlist;
import org.junit.Test;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for PlaylistDAO
 * Make sure you have added the right dependencies in the pom file
 * If not, it ain't gon' work
 */
public class PlaylistDAOTest {
    private PlaylistDAO playlistDAO;
    private Playlist testPlaylist;
    private SongDAO songDAO;

    @BeforeEach
    public void setup() {
        playlistDAO = new PlaylistDAO();
        songDAO = new SongDAO();
        testPlaylist = new Playlist(1, "Test");
    }

    @Test
    public void testInsert() throws SQLException {
        Playlist inserted = playlistDAO.insert(testPlaylist);

        assertNotNull(inserted.getId());
        assertEquals("Test playlist", inserted.getName());
    }

    //Check if you can find your playlists
    @Test
    public void testFindAll() throws SQLException{
        playlistDAO.insert(testPlaylist);
        playlistDAO.insert(new Playlist("Playlist2"));

        List<Playlist> playlists = playlistDAO.findAll();

        assertTrue(playlists.size() >= 2, "We should find 2 playlists");
    }

}
