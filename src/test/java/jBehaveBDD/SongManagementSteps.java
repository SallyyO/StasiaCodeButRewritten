package jBehaveBDD;

import com.example.demo2.bll.SongService;
import com.example.demo2.entities.Song;
import org.jbehave.core.annotations.*;
import org.jbehave.core.model.ExamplesTable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for JBehave testing
 * Here for the Song management scenarios:)
 */
public class SongManagementSteps {
    private SongService songService;
    private Song currentSong;
    private List<Song> searchResults;
    private Exception lastException;
    private File tempFile;

    @BeforeScenario
    public void setUp() throws IOException {
        songService = new SongService();
        currentSong = null;
        searchResults = null;
        lastException = null;
        tempFile = File.createTempFile("test", ".mp3");
        tempFile.deleteOnExit();

    }

    @AfterScenario
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }
    @Given("We have a valid audio file at \"$filePath\"")
    public void givenValidAudioFile (String filePath){
        //we use the tempfile for testing this
        assertNotNull(tempFile);
        assertTrue(tempFile.exists());
    }

    @Given("There are songs in the library: $songsTable")
    public void givenSongsInLibrary(ExamplesTable songsTable) throws Exception {
        for (int i = 0; i < songsTable.getRowCount(); i++) {
            String title = songsTable.getRow(i).get("title");
            String artist = songsTable.getRow(i).get("artist");

            File file = File.createTempFile("testSong" + i, ".mp3");
            file.deleteOnExit();

            Song song = new Song(title, artist, 123, file.getAbsolutePath());
            songService.create(song);
        }
    }

    @Given("I have a song called \"$title\" by \"$artist\"")
    public void givenSongByArtist(String title, String artist) throws Exception {
        currentSong = new Song(title, artist, 123, tempFile.getAbsolutePath());
        currentSong =  songService.create(currentSong);
    }

    @Given("I have a song with the title \"$title\" in my library")
    public void givenSongInLibrary(String title) throws Exception {
        currentSong = new Song(title, "Artist",  123, tempFile.getAbsolutePath());
        currentSong =  songService.create(currentSong);
    }

    @When("I create a song with the title\"$title\" and artist \"$artist\"")
    public void whenCreateSong(String title, String artist) throws Exception {
        try {
            currentSong = new Song(title, artist, 123, tempFile.getAbsolutePath());
            currentSong =  songService.create(currentSong);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I search for \"$query\"")
    public void whenSearchingFor(String query) {
        searchResults = songService.search(query);
    }

    @When("I update a song title to \"$newTitle\"")
    public void whenUpdateSongTitle(String newTitle) throws Exception {
        currentSong.setTitle(newTitle);
        songService.update(currentSong);
    }

    @When("I delete the song")
    public void whenDeleteSong() throws Exception {
        songService.delete(currentSong.getId());
    }

    @Then("the song should appear in my library")
    public void thenSongInLibrary() throws Exception {
        List<Song> songs = songService.getAll();
        assertTrue(songs.stream().anyMatch(song -> song.getId().equals(currentSong.getId())));
    }

    @Then("the song should have the title \"$title\"")
    public void thenSongShouldBeTitled(String title) throws Exception {
        List<Song> songs = songService.getAll();
        Song updated = songs.stream()
                .filter(song -> song.getId().equals(currentSong.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(updated);
        assertEquals(title, updated.getTitle());
    }

    @Then("the song should no longer exist in my library")
    public void thenSongGone() throws Exception {
        List<Song> songs = songService.getAll();
        assertFalse(songs.stream().anyMatch(song -> song.getId().equals(currentSong.getId())));
    }
}
