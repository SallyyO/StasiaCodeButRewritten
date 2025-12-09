package jUnit;

import com.example.demo2.bll.PlaylistService;
import com.example.demo2.bll.SongService;
import com.example.demo2.entities.Playlist;
import com.example.demo2.entities.Song;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for PlaylistService
 * Make sure you have added the right dependencies in the pom file
 * If not, it ain't gon' work
 */
public class PlaylistServiceTest {
    private PlaylistService playlistService;
    private SongService songService;
    private File tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        playlistService = new PlaylistService();
        songService = new SongService();
        tempFile = File.createTempFile("test", ".mp3");
        tempFile.deleteOnExit();
    }
    @AfterEach
    public void tearDown() throws IOException {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }
    @Test
    public void testCreateNewPlaylist() throws Exception {
        Playlist playlist = new Playlist("Test playlist");

        Playlist created = playlistService.create(playlist);

        assertNotNull(created.getId());
        assertEquals("Test playlist", created.getName());
    }

    @Test //Testing if we (the user) can rename playlists
    public void testRename()  throws Exception {
        Playlist playlist = playlistService.create(new Playlist("OG name"));

        playlist.setName("Brand new name wow");
        playlistService.rename(playlist);

        List<Playlist> all = playlistService.getAll();
        Playlist renamed = all.stream()
                .filter(p -> p.getId().equals(playlist.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(renamed);
        assertEquals("Brand new name wow", renamed.getName());
    }

    @Test //der er sgu altid bøvl manner, hvorfor kan det Æ BARE VIRKE
    public void testAddSongToPlaylist()  throws Exception {
        Playlist playlist = playlistService.create(new Playlist("Test"));
        Song song = songService.create(new Song("Test song", "Artist", 123, tempFile.getAbsolutePath()));

        playlistService.addSongToEnd(playlist.getId(), song.getId());

        List<Song> songs = playlistService.getSongs(playlist.getId());
        assertEquals(1, songs.size());
        assertEquals(song.getId(), songs.get(0).getId());
    }

    @Test //i wonder if these assholes will work when i'm connected to the school's wifi
    public void testRemoveSongFromPlaylist()  throws Exception {
        Playlist playlist = playlistService.create(new Playlist("Test"));
        Song song = songService.create(new Song("Test song", "Artist", 123, tempFile.getAbsolutePath()));

        playlistService.addSongToEnd(playlist.getId(), song.getId());
        playlistService.removeAtPosition(playlist.getId(), 0);

        List<Song> songs = playlistService.getSongs(playlist.getId());
        assertEquals(0, songs.size());
    }

    
}


