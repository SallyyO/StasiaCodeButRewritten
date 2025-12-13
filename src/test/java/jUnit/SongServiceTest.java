package jUnit;

import com.example.demo2.bll.SongService;
import com.example.demo2.dal.SongDAO;
import com.example.demo2.entities.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for SongService
 * Make sure you have added the right dependencies in the pom file
 * If not, it ain't gon' work
 */
public class SongServiceTest {

    @Mock
    private SongDAO mockSongDAO;

    private SongService songService;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        songService = new SongService(mockSongDAO);
    }

    @Test
    public void testGetAllSongs() throws SQLException {
        List<Song> expectedSongs = Arrays.asList(
                new Song ("Song A", "Artist A", 123, "/path/a.mp3"),
                new Song("Song B", "Artist B", 234, "/path/b.mp3")
        );
        when(mockSongDAO.findAll()).thenReturn(expectedSongs);

        List<Song> actualSongs = songService.getAll();

        assertEquals(2, actualSongs.size());
        assertEquals("Song A", actualSongs.get(0).getTitle());
        verify(mockSongDAO, times(1)).findAll();
    }

    @Test
    public void testValidSearchGetsMatch() throws SQLException {
        String query = "Hee";
        List<Song> expectedSongs = Arrays.asList(
                new Song(1, "Hee Hee", "M J", 123, "/path/heehee.mp3")
        );
        when(mockSongDAO.search(query)).thenReturn(expectedSongs);

        List<Song> actualSongs = songService.search(query);

        assertEquals(1, actualSongs.size());
        assertEquals("Hee Hee", actualSongs.get(0).getTitle());
        verify(mockSongDAO, times(1)).search(query);
    }

    @Test
    public void testBlankSearchReturnsAllSongs() throws SQLException {
        List<Song> allSongs = Arrays.asList(
                new Song ("Song A", "Artist A", 123, "/path/a.mp3"),
                new Song("Song B", "Artist B", 234, "/path/b.mp3")
        );
        when(mockSongDAO.findAll()).thenReturn(allSongs);

        List<Song> actualSongs = songService.search(" ");

        assertEquals(2, actualSongs.size());
        verify(mockSongDAO, times(1)).findAll();
        verify(mockSongDAO, never()).search(anyString());
    }

}
