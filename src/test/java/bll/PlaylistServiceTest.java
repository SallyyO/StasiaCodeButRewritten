package bll;

import com.example.demo2.bll.PlaylistService;
import com.example.demo2.dal.PlaylistDAO;
import com.example.demo2.entities.Playlist;
import com.example.demo2.entities.Song;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaylistServiceTest {

    private PlaylistDAO mockDAO;
    private PlaylistService playlistService;

    @BeforeEach
    public void setup() {
        mockDAO = Mockito.mock(PlaylistDAO.class);
        playlistService = new PlaylistService(mockDAO);
    }

    @Test
    public void testGetAll() throws SQLException {
        List <Playlist> mockList = List.of(new Playlist(1, "SingSong"));
        when (mockDAO.findAll()).thenReturn(mockList);

        List<Playlist> result = playlistService.getAll();

        assertEquals(1, result.size());
        assertEquals("SingSong", result.get(0).getName());
        verify(mockDAO).findAll();
    }

    @Test
    void testCreate() throws SQLException {
        Playlist p = new Playlist(0, "New");
        Playlist saved = new Playlist(5, "New");

        when(mockDAO.insert(p)).thenReturn(saved);

        Playlist result = playlistService.create(p);

        assertEquals(5, result.getId());
        verify(mockDAO).insert(p);
    }

    @Test
    void testRename() throws SQLException {
        Playlist p = new Playlist(3, "OldName");

        playlistService.rename(p);

        verify(mockDAO).update(p);
    }

    @Test
    void testDelete() throws SQLException {
        when(mockDAO.delete(9)).thenReturn(true);

        boolean result = playlistService.delete(9);

        assertTrue(result);
        verify(mockDAO).delete(9);
    }

    @Test
    void testGetSongs() throws SQLException {
        List<Song> mockSongs = List.of(new Song(1, "Thriller", "Michael Jackson", 3087, "user.music.michaeljackson"));

        when(mockDAO.getSongs(7)).thenReturn(mockSongs);

        List<Song> result = playlistService.getSongs(7);

        assertEquals(1, result.size());
        verify(mockDAO).getSongs(7);
    }

    @Test
    void testAddSongToEnd() throws SQLException {
        playlistService.addSongToEnd(1, 2);
        verify(mockDAO).addSongToEnd(1, 2);
    }

    @Test
    void testRemoveAtPosition() throws SQLException {
        playlistService.removeAtPosition(3, 1);
        verify(mockDAO).removeAtPosition(3, 1);
    }

    @Test
    void testMove() throws SQLException {
        playlistService.move(4, 1, 3);
        verify(mockDAO).move(4, 1, 3);
    }
}
