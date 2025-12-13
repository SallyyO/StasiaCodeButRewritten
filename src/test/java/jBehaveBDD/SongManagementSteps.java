package jBehaveBDD;

import com.example.demo2.bll.SongService;
import com.example.demo2.dal.TestDBManager;
import com.example.demo2.entities.Song;
import org.jbehave.core.annotations.*;
import org.jbehave.core.model.ExamplesTable;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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
    private File tempTestFile;

    @BeforeScenario
    public void setUp() throws SQLException, IOException {
        songService = new SongService();
        currentSong = null;
        searchResults = null;
        lastException = null;
        TestDBManager.cleanDatabase();
        createTempTestFile();

    }

    @AfterScenario
    public void tearDown() {
        if (tempTestFile != null && tempTestFile.exists()) {
            tempTestFile.delete();
        }
    }

    private void createTempTestFile() throws IOException {
        tempTestFile =  File.createTempFile("test_audio", ".mp3");
        tempTestFile.deleteOnExit();
    }

    @Given("We have a valid audio file at \"$filePath\"")
    public void givenValidAudioFile (String filePath){
        //we use the tempfile for testing this
        assertNotNull(tempTestFile);
        assertTrue(tempTestFile.exists());
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
        currentSong = new Song(title, artist, 123, tempTestFile.getAbsolutePath());
        currentSong =  songService.create(currentSong);
    }

    @Given("I have a song with the title \"$title\" in my library")
    public void givenSongInLibrary(String title) throws Exception {
        currentSong = new Song(title, "Artist",  123, tempTestFile.getAbsolutePath());
        currentSong =  songService.create(currentSong);
    }

    @When("I create a song with the title\"$title\" and artist \"$artist\"")
    public void whenCreateSong(String title, String artist) throws Exception {
        try {
            currentSong = new Song(title, artist, 123, tempTestFile.getAbsolutePath());
            currentSong =  songService.create(currentSong);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I search for \"$query\"")
    public void whenSearchingFor(String query) throws Exception {
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
