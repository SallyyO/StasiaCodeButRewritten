package jUnit;

import com.example.demo2.dal.DBManager;
import com.example.demo2.dal.SongDAO;
import com.example.demo2.dal.TestDBManager;
import com.example.demo2.entities.Song;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for SongDAO
 * Make sure you have added the right dependencies in the pom file
 * If not, it ain't gon' work
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SongDAOTest {
    //IT ACTUALLY WORKS YAYYYYY: update, nvm
    private static SongDAO songDAO;

    @BeforeAll
    public static void setUpClass() throws SQLException {
    songDAO = new SongDAO();
        TestDBManager.cleanDatabase();
    }
    @BeforeEach
    public void setUp() throws SQLException {
        TestDBManager.cleanDatabase();
    }

    private static void cleanDatabase() throws SQLException {
        try (Connection connection = DBManager.getConnection();
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM playlist_songs");
            statement.executeUpdate("DELETE FROM playlists");
            statement.executeUpdate("DELETE FROM songs");
        }
    }

    @Test
    @Order(1)
    public void testNewSong() throws SQLException {
        Song song = new Song("Test song", "Test artist", 123, "/path/test.mp3");

        Song result = songDAO.insert(song);

        assertNotNull(result.getId());
        assertEquals("Test song", result.getTitle());
        assertEquals("Test artist", result.getArtist());
        assertEquals(123, result.getDurationSeconds());
    }

    @Test
    @Order(2)
    public void testFindAllSongs() throws SQLException {
        songDAO.insert(new Song("Song A", "Artist A", 123, "/path/a.mp3"));
        songDAO.insert(new Song("Song B", "Artist B", 234, "/path/b.mp3"));
        songDAO.insert(new Song("Song C", "Artist C", 345, "/path/c.mp3"));

        List<Song> songs = songDAO.findAll();

        assertEquals(3, songs.size());
        //Bc we wanted the songs to be ordered by their title
        assertEquals("Song A", songs.get(0).getTitle());
    }

    @Test
    @Order(3)
    public void testSearchTitleFindMatch() throws SQLException {
        songDAO.insert(new Song("Rock song", "Rock Artist", 333, "/path/rockypath.mp3"));
        songDAO.insert(new Song("Jazz Song", "That Bee Guy", 234, "/path/jazzbee.mp3"));
        songDAO.insert(new Song("Rocking around some tree", "Known Artist", 384, "/path/stillrocky.mp3"));

        List<Song> results = songDAO.search("Rock");
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(song -> song.getTitle().contains("Rock")));
    }

    @Test
    @Order(4)
    public void testSearchArtistFindMatch() throws SQLException {
        songDAO.insert(new Song("Hee hee", "MJ", 186, "/path/heehee.mp3"));
        songDAO.insert(new Song("Definitely no hee", "Not Michael J at all", 333, "/path/nope.mp3"));
        songDAO.insert( new Song("HeeHee Hee", "MJ", 157, "/path/heeonceagain.mp3"));

        List<Song> results = songDAO.search("MJ");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(song -> song.getArtist().equals("MJ")));

    }
}




