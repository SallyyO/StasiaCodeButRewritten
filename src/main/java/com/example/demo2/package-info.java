/**
 * Package-level documentation for the demo2 application.
 *
 * This project is a JavaFX starter that can be extended into a simple media
 * player (like a lightweight Spotify). For now, this file only provides
 * documentation and must not contain any executable code or free-form text
 * outside of a proper Javadoc comment, otherwise it will break compilation.
 */
package com.example.demo2;
/*
    private String path;
    private String duration;

    public Song(int id, String title, String artist, String path, String duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
    }

    public Song(String title, String artist, String path, String duration) {
        this(-1, title, artist, path, duration);
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getPath() { return path; }
    public String getDuration() { return duration; }

    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setPath(String path) { this.path = path; }
    public void setDuration(String duration) { this.duration = duration; }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}
Playlist.java
        java
package dk.mytunes.be;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private int id;
    private String name;
    private List<Song> songs;

    public Playlist(int id, String name) {
        this.id = id;
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public Playlist(String name) {
        this(-1, name);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<Song> getSongs() { return songs; }

    public void addSong(Song song) { songs.add(song); }
    public void removeSong(Song song) { songs.remove(song); }

    @Override
    public String toString() { return name; }
}
🧠 2. Business Logic Layer
MyTunesManager.java
        java
package dk.mytunes.bll;

import dk.mytunes.be.Playlist;
import dk.mytunes.be.Song;
import dk.mytunes.dal.SongDAO;
import dk.mytunes.dal.PlaylistDAO;

import java.sql.SQLException;
import java.util.List;

public class MyTunesManager {
    private final SongDAO songDAO;
    private final PlaylistDAO playlistDAO;

    public MyTunesManager() throws SQLException {
        songDAO = new SongDAO();
        playlistDAO = new PlaylistDAO();
    }

    // SONGS
    public List<Song> getAllSongs() throws SQLException {
        return songDAO.getAllSongs();
    }

    public Song createSong(Song song) throws SQLException {
        return songDAO.createSong(song);
    }

    public void deleteSong(Song song) throws SQLException {
        songDAO.deleteSong(song);
    }

    // PLAYLISTS
    public List<Playlist> getAllPlaylists() throws SQLException {
        return playlistDAO.getAllPlaylists();
    }

    public Playlist createPlaylist(Playlist playlist) throws SQLException {
        return playlistDAO.createPlaylist(playlist);
    }

    public void deletePlaylist(Playlist playlist) throws SQLException {
        playlistDAO.deletePlaylist(playlist);
    }
}
💾 3. Data Access Layer (DAL)
DBConnector.java
        java
package dk.mytunes.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private static final String URL = "jdbc:sqlite:myTunes.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }
}
SongDAO.java
        java
package dk.mytunes.dal;

import dk.mytunes.be.Song;
import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class SongDAO {

    public List<Song> getAllSongs() throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM Song;";
        try (Connection con = DBConnector.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                songs.add(new Song(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("path"),
                        rs.getString("duration")
                ));
            }
        }
        return songs;
    }

    public Song createSong(Song song) throws SQLException {
        String sql = "INSERT INTO Song(title, artist, path, duration) VALUES (?, ?, ?, ?);";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, song.getTitle());
            ps.setString(2, song.getArtist());
            ps.setString(3, song.getPath());
            ps.setString(4, song.getDuration());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                song = new Song(rs.getInt(1), song.getTitle(), song.getArtist(), song.getPath(), song.getDuration());
            }
        }
        return song;
    }

    public void deleteSong(Song song) throws SQLException {
        String sql = "DELETE FROM Song WHERE id = ?;";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, song.getId());
            ps.executeUpdate();
        }
    }
}
(PlaylistDAO would be similar — handle CRUD for playlists and maybe a join table for playlist-to-song relationships.)

        🎨 4. GUI Layer (JavaFX)
MyTunesApp.java – entry point
java
package dk.mytunes.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MyTunesApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/mytunes/gui/view/MainView.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("myTunes - Music Player");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
MainView.fxml
A basic layout (save this under resources/dk/mytunes/gui/view/MainView.fxml):

xml
<?xml version="1.0" encoding="UTF-8"?>

<BorderPane xmlns:fx="http://javafx.com/fxml" fx:controller="dk.mytunes.gui.controller.MainController">
    <left>
        <VBox spacing="8" padding="10">
            <Label text="Playlists"/>
            <ListView fx:id="playlistList" prefHeight="200"/>
            <HBox spacing="5">
                <Button text="New" fx:id="btnNewPlaylist"/>
                <Button text="Edit" fx:id="btnEditPlaylist"/>
                <Button text="Delete" fx:id="btnDeletePlaylist"/>
            </HBox>
        </VBox>
    </left>

    <center>
        <VBox spacing="8" padding="10">
            <Label text="Songs in Playlist"/>
            <ListView fx:id="playlistSongsList" prefHeight="200"/>
        </VBox>
    </center>

    <right>
        <VBox spacing="8" padding="10">
            <Label text="All Songs"/>
            <TableView fx:id="songTable" prefHeight="400">
                <columns>
                    <TableColumn fx:id="colTitle" text="Title" />
                    <TableColumn fx:id="colArtist" text="Artist" />
                </columns>
            </TableView>
            <HBox spacing="5">
                <Button text="New" fx:id="btnNewSong"/>
                <Button text="Edit" fx:id="btnEditSong"/>
                <Button text="Delete" fx:id="btnDeleteSong"/>
            </HBox>
        </VBox>
    </right>

    <bottom>
        <HBox spacing="10" alignment="CENTER" padding="10">
            <Button text="Play" fx:id="btnPlay"/>
            <Button text="Pause" fx:id="btnPause"/>
            <Button text="Stop" fx:id="btnStop"/>
        </HBox>
    </bottom>
</BorderPane>
MainController.java
        java
package dk.mytunes.gui.controller;

import dk.mytunes.be.Song;
import dk.mytunes.bll.MyTunesManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

        import java.sql.SQLException;

public class MainController {
    @FXML private TableView<Song> songTable;
    @FXML private TableColumn<Song, String> colTitle;
    @FXML private TableColumn<Song, String> colArtist;

    private MyTunesManager myTunesManager;

    public void initialize() {
        try {
            myTunesManager = new MyTunesManager();
            colTitle.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
            colArtist.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getArtist()));
            loadSongs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSongs() throws SQLException {
        songTable.getItems().setAll(myTunesManager.getAllSongs());
    }
}
🧱 Database Setup
Create myTunes.db with tables:

sql
CREATE TABLE Song (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        artist TEXT NOT NULL,
        duration TEXT,
        path TEXT NOT NULL
);

CREATE TABLE Playlist (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL
);
(Add PlaylistSong later if you want to store song order per playlist.)

        ▶️ Running the Project
Use Java 17+
Add JavaFX SDK to your build path or use Maven/Gradle
Run MyTunesApp
The GUI will load, showing placeholders for playlists and songs
🧰 Next Steps
After this skeleton:

Add dialogs for new/edit song & playlist
Implement delete and filter logic
Add audio playback using:
java
Media media = new Media(new File(song.getPath()).toURI().toString());
MediaPlayer player = new MediaPlayer(media);
player.play();
Connect playlists to songs via a join table
Would you like me to generate the SongDialog.fxml + controller (for adding/editing songs) next? It’s the next step after setting up this base.




        Yes please

Excellent 🎶 — let’s continue by adding the New/Edit Song dialog — one of the key pieces of your myTunes JavaFX app.

This dialog will let users create or edit songs, including title, artist, duration, and file path.

        🧩 Files You’ll Add
src/dk/mytunes/gui/view/SongDialog.fxml src/dk/mytunes/gui/controller/SongDialogController.java
🎨 SongDialog.fxml
A simple form with “OK” and “Cancel” buttons.

        xml
<?xml version="1.0" encoding="UTF-8"?>

<DialogPane xmlns:fx="http://javafx.com/fxml"
fx:controller="dk.mytunes.gui.controller.SongDialogController"
prefWidth="400">

    <content>
        <GridPane hgap="10" vgap="10" padding="20">
            <Label text="Title:" GridPane.rowIndex="0" GridPane.columnIndex="0"/>
            <TextField fx:id="txtTitle" GridPane.rowIndex="0" GridPane.columnIndex="1"/>

            <Label text="Artist:" GridPane.rowIndex="1" GridPane.columnIndex="0"/>
            <TextField fx:id="txtArtist" GridPane.rowIndex="1" GridPane.columnIndex="1"/>

            <Label text="Duration:" GridPane.rowIndex="2" GridPane.columnIndex="0"/>
            <TextField fx:id="txtDuration" GridPane.rowIndex="2" GridPane.columnIndex="1"/>

            <Label text="File Path:" GridPane.rowIndex="3" GridPane.columnIndex="0"/>
            <HBox spacing="5" GridPane.rowIndex="3" GridPane.columnIndex="1">
                <TextField fx:id="txtPath" HBox.hgrow="ALWAYS"/>
                <Button text="Browse..." fx:id="btnBrowse"/>
            </HBox>
        </GridPane>
    </content>

    <buttonTypes>
        <ButtonType text="OK" buttonData="OK_DONE"/>
        <ButtonType text="Cancel" buttonData="CANCEL_CLOSE"/>
    </buttonTypes>
</DialogPane>
        🎛️ SongDialogController.java
        java
package dk.mytunes.gui.controller;

import dk.mytunes.be.Song;
import javafx.fxml.FXML;
import javafx.scene.control.*;
        import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class SongDialogController {

    @FXML private TextField txtTitle;
    @FXML private TextField txtArtist;
    @FXML private TextField txtDuration;
    @FXML private TextField txtPath;
    @FXML private Button btnBrowse;

    private Song song;

    @FXML
    private void initialize() {
        btnBrowse.setOnAction(e -> selectFile());
    }

    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Music File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Music Files", "*.mp3", "*.wav")
        );
        Window window = btnBrowse.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            txtPath.setText(file.getAbsolutePath());
        }
    }

    // Called when editing an existing song
    public void setSong(Song song) {
        this.song = song;
        if (song != null) {
            txtTitle.setText(song.getTitle());
            txtArtist.setText(song.getArtist());
            txtDuration.setText(song.getDuration());
            txtPath.setText(song.getPath());
        }
    }

    // Called when clicking OK in the dialog
    public Song getResult() {
        // Handle both new & edited songs
        if (song == null) {
            song = new Song(
                    txtTitle.getText().trim(),
                    txtArtist.getText().trim(),
                    txtPath.getText().trim(),
                    txtDuration.getText().trim()
            );
        } else {
            song.setTitle(txtTitle.getText().trim());
            song.setArtist(txtArtist.getText().trim());
            song.setDuration(txtDuration.getText().trim());
            song.setPath(txtPath.getText().trim());
        }
        return song;
    }
}
🧠 Using the Dialog from MainController
When the user clicks “New” or “Edit”, you can show the dialog like this:

java
@FXML
private void handleNewSong() {
    Song newSong = openSongDialog(null);
    if (newSong != null) {
        try {
            myTunesManager.createSong(newSong);
            loadSongs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

@FXML
private void handleEditSong() {
    Song selected = songTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
        Song edited = openSongDialog(selected);
        if (edited != null) {
            try {
                // Here you can update in DB using a SongDAO.updateSong()
                loadSongs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

// Helper for opening dialogs
private Song openSongDialog(Song song) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/mytunes/gui/view/SongDialog.fxml"));
        DialogPane dialogPane = loader.load();

        SongDialogController controller = loader.getController();
        controller.setSong(song);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.setTitle(song == null ? "New Song" : "Edit Song");

        dialog.showAndWait();

        if (dialog.getResult() == ButtonType.OK) {
            return controller.getResult();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
🎧 What Happens Here
When the user clicks “New Song” or “Edit Song”:
A SongDialog appears.
        If editing, fields are pre-filled.
When the user clicks “OK”, form data creates or updates a Song object.
The MainController then calls the business layer (via MyTunesManager.createSong()) to persist it.
✅ Next Steps
Once this dialog works well, you can:

Add validation (e.g., ensure title/artist/path are not empty)
Implement the updateSong() method in SongDAO
Add error messages for invalid input
Later, reuse the same approach for the Playlist dialog
Would you like me to give you the PlaylistDialog.fxml + controller next (so you can wire up playlist creation/editing too)?




Yes please

Awesome 🎵 — you’re building this like a pro! Let’s now add the New/Edit Playlist dialog, which works just like the Song Dialog but is simpler (it only manages the playlist name).

        🧩 Files You’ll Add
src/dk/mytunes/gui/view/PlaylistDialog.fxml src/dk/mytunes/gui/controller/PlaylistDialogController.java
🎨 PlaylistDialog.fxml
This FXML defines a small dialog pane with a single text field for the playlist name.

        xml
<?xml version="1.0" encoding="UTF-8"?>

<DialogPane xmlns:fx="http://javafx.com/fxml"
fx:controller="dk.mytunes.gui.controller.PlaylistDialogController"
prefWidth="350">

    <content>
        <GridPane hgap="10" vgap="10" padding="20">
            <Label text="Playlist Name:" GridPane.rowIndex="0" GridPane.columnIndex="0"/>
            <TextField fx:id="txtName" GridPane.rowIndex="0" GridPane.columnIndex="1"
promptText="Enter playlist name"/>
        </GridPane>
    </content>

    <buttonTypes>
        <ButtonType text="OK" buttonData="OK_DONE"/>
        <ButtonType text="Cancel" buttonData="CANCEL_CLOSE"/>
    </buttonTypes>
</DialogPane>
        🧠 PlaylistDialogController.java
This class handles the dialog’s data and interaction.

java
package dk.mytunes.gui.controller;

import dk.mytunes.be.Playlist;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class PlaylistDialogController {

    @FXML private TextField txtName;

    private Playlist playlist;

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        if (playlist != null) {
            txtName.setText(playlist.getName());
        }
    }

    public Playlist getResult() {
        String name = txtName.getText().trim();

        if (playlist == null) {
            playlist = new Playlist(name);
        } else {
            playlist = new Playlist(playlist.getId(), name);
        }

        return playlist;
    }
}
🪄 Using It in the MainController
You’ll show this dialog when the user presses New… or Edit… in the playlist section.

Add these methods to your MainController.java:

java
@FXML
private void handleNewPlaylist() {
    Playlist newPlaylist = openPlaylistDialog(null);
    if (newPlaylist != null) {
        try {
            myTunesManager.createPlaylist(newPlaylist);
            playlistList.getItems().setAll(myTunesManager.getAllPlaylists());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

@FXML
private void handleEditPlaylist() {
    Playlist selected = playlistList.getSelectionModel().getSelectedItem();
    if (selected != null) {
        Playlist edited = openPlaylistDialog(selected);
        if (edited != null) {
            try {
                // TODO: implement PlaylistDAO.updatePlaylist(edited)
                playlistList.getItems().setAll(myTunesManager.getAllPlaylists());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

private Playlist openPlaylistDialog(Playlist playlist) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/mytunes/gui/view/PlaylistDialog.fxml"));
        DialogPane dialogPane = loader.load();

        PlaylistDialogController controller = loader.getController();
        controller.setPlaylist(playlist);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.setTitle(playlist == null ? "New Playlist" : "Edit Playlist");

        dialog.showAndWait();

        if (dialog.getResult() == ButtonType.OK) {
            return controller.getResult();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
💾 DAO Extension Example (Later)
When you’re ready to persist changes to the database, you can extend your existing PlaylistDAO with an update method:

java
public void updatePlaylist(Playlist playlist) throws SQLException {
    String sql = "UPDATE Playlist SET name = ? WHERE id = ?;";
    try (Connection con = DBConnector.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, playlist.getName());
        ps.setInt(2, playlist.getId());
        ps.executeUpdate();
    }
}
⚡ Summary
✅ What you have now:

Song Dialog → create / edit songs
Playlist Dialog → create / edit playlists
Both return the updated entities to the main controller
The UI integrates with the BLL / DAO layers for persistence
Would you like me to help you next with the playback functionality (so songs can actually play using MediaPlayer, including next/previous queue logic)?




Yes

Perfect 🎧 — this is the exciting part!
Let’s add song playback functionality to your myTunes JavaFX app using the built-in Media and MediaPlayer classes.

🧠 Goal
Enable your app to:

Play, pause, stop a selected song
Automatically proceed to the next song (in All Songs list or selected playlist)
Provide buttons wired to player logic (Play / Pause / Stop / Next / Previous)
Handle playback state cleanly
🧩 File: PlayerManager.java (New Class in bll)
We'll create this lightweight player manager to encapsulate all playback logic.

Location:
src/dk/mytunes/bll/PlayerManager.java

        java
package dk.mytunes.bll;

import dk.mytunes.be.Song;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class PlayerManager {

    private MediaPlayer mediaPlayer;
    private List<Song> currentQueue;
    private int currentIndex = -1;

    public void playSongs(List<Song> songs, Song startSong) {
        this.currentQueue = songs;
        this.currentIndex = songs.indexOf(startSong);
        playCurrent();
    }

    public void playSong(Song song) {
        this.currentQueue = List.of(song);
        this.currentIndex = 0;
        playCurrent();
    }

    private void playCurrent() {
        if (currentQueue == null || currentQueue.isEmpty() || currentIndex < 0) return;
        Song song = currentQueue.get(currentIndex);

        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            Media media = new Media(new File(song.getPath()).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();

            mediaPlayer.setOnEndOfMedia(this::playNext); // Auto-play next song

        } catch (Exception e) {
            System.err.println("Failed to play song: " + e.getMessage());
        }
    }

    public void playNext() {
        if (currentQueue == null || currentQueue.isEmpty()) return;
        currentIndex = (currentIndex + 1) % currentQueue.size();
        playCurrent();
    }

    public void playPrevious() {
        if (currentQueue == null || currentQueue.isEmpty()) return;
        currentIndex = (currentIndex - 1 + currentQueue.size()) % currentQueue.size();
        playCurrent();
    }

    public void pause() {
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    public void resume() {
        if (mediaPlayer != null) mediaPlayer.play();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }

    public Duration getCurrentTime() {
        return (mediaPlayer != null) ? mediaPlayer.getCurrentTime() : Duration.ZERO;
    }

    public Duration getTotalDuration() {
        return (mediaPlayer != null) ? mediaPlayer.getTotalDuration() : Duration.UNKNOWN;
    }
}
🎛️ Update MainController.java — Hook Player Buttons
In your controller, add:

java
import dk.mytunes.be.Playlist;
import javafx.collections.ObservableList;

@FXML private Button btnPlay;
@FXML private Button btnPause;
@FXML private Button btnStop;
@FXML private Button btnNext;
@FXML private Button btnPrev;

private PlayerManager playerManager = new PlayerManager();
🔹 Add Event Handlers
        java
@FXML
private void initialize() {
    try {
        myTunesManager = new MyTunesManager();
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colArtist.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArtist()));
        loadSongs();
        loadPlaylists();
    } catch (SQLException e) {
        e.printStackTrace();
    }

    // Player button actions:
    btnPlay.setOnAction(e -> handlePlay());
    btnPause.setOnAction(e -> playerManager.pause());
    btnStop.setOnAction(e -> playerManager.stop());
    btnNext.setOnAction(e -> playerManager.playNext());
    btnPrev.setOnAction(e -> playerManager.playPrevious());
}
🔹 Implement handlePlay()
java
private void handlePlay() {
    Song selectedSong = songTable.getSelectionModel().getSelectedItem();

    if (selectedSong == null) {
        // If none selected in All Songs, try current playlist
        selectedSong = playlistSongsList.getSelectionModel().getSelectedItem();
    }

    if (selectedSong != null) {
        // Decide what the active list is (playlist or all songs)
        ObservableList<Song> activeList;
        if (playlistList.getSelectionModel().getSelectedItem() != null) {
            Playlist selectedPlaylist = playlistList.getSelectionModel().getSelectedItem();
            activeList = javafx.collections.FXCollections.observableArrayList(selectedPlaylist.getSongs());
        } else {
            activeList = songTable.getItems();
        }

        playerManager.playSongs(activeList, selectedSong);
    }
}
🖼️ Update MainView.fxml
Add the missing buttons (if not already there):

xml
        <HBox spacing="10" alignment="CENTER" padding="10">
    <Button text="⏮" fx:id="btnPrev" />
    <Button text="▶️ Play" fx:id="btnPlay" />
    <Button text="⏸ Pause" fx:id="btnPause" />
    <Button text="⏹ Stop" fx:id="btnStop" />
    <Button text="⏭" fx:id="btnNext" />
</HBox>
        ✅ How It Works
The Play button checks which song is selected.
If a playlist is selected → the app plays all songs in that order.
If “All Songs” view is selected → it plays the selected song and continues down the table.
The playback automatically advances when the current track ends.
Buttons control the same MediaPlayer instance in PlayerManager.
        ⚙️ Optional Enhancements
Once this works, you can expand with:

Volume control slider:
java
mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
Progress bar linked to mediaPlayer.currentTimeProperty()
Shuffle / repeat modes
Displaying currently playing song label at the bottom
🎯 Next Step Suggestion
Want me to show you how to add a progress slider + currently playing label
        (e.g., “Now Playing: [Song Title] — [Artist]” + a live progress bar)?
That’s the next natural polish for the player UI.




        Yes please

Excellent — you’re building a real music player now! 🎶
Let’s add a “Now Playing” label and a Progress Slider that updates while a song is playing and lets you seek within the track.

🧭 Overview
We’ll update:

MainView.fxml → add the label and slider.
        PlayerManager.java → expose playback time updates and seeking.
MainController.java → link the slider and label to the player in real time.
🎨 Step 1: Update MainView.fxml
Locate the <bottom> section of your layout and replace it with this enhanced version:

xml
        <bottom>
    <VBox spacing="8" alignment="CENTER" padding="10">
        <Label fx:id="lblNowPlaying" text="Now Playing: [none]" />

        <HBox spacing="10" alignment="CENTER">
            <Button text="⏮" fx:id="btnPrev" />
            <Button text="▶️ Play" fx:id="btnPlay" />
            <Button text="⏸ Pause" fx:id="btnPause" />
            <Button text="⏹ Stop" fx:id="btnStop" />
            <Button text="⏭" fx:id="btnNext" />
        </HBox>

        <HBox spacing="10" alignment="CENTER">
            <Label fx:id="lblCurrentTime" text="00:00" />
            <Slider fx:id="sliderProgress" prefWidth="300" />
            <Label fx:id="lblTotalTime" text="--:--" />
        </HBox>
    </VBox>
</bottom>
        🧩 Step 2: Update PlayerManager.java
Add time tracking and seeking support.
Edit your existing PlayerManager to include listeners for real‑time updates and an optional callback for the controller.

java
package dk.mytunes.bll;

import dk.mytunes.be.Song;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class PlayerManager {

    private MediaPlayer mediaPlayer;
    private List<Song> currentQueue;
    private int currentIndex = -1;

    // For progress tracking
    private final DoubleProperty progressPercent = new SimpleDoubleProperty(0);
    private Consumer<Song> onSongChange;

    public void setOnSongChange(Consumer<Song> callback) {
        this.onSongChange = callback;
    }

    public DoubleProperty progressProperty() {
        return progressPercent;
    }

    public void playSongs(List<Song> songs, Song startSong) {
        this.currentQueue = songs;
        this.currentIndex = songs.indexOf(startSong);
        playCurrent();
    }

    private void playCurrent() {
        if (currentQueue == null || currentQueue.isEmpty() || currentIndex < 0) return;

        Song song = currentQueue.get(currentIndex);

        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            Media media = new Media(new File(song.getPath()).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            if (onSongChange != null) onSongChange.accept(song);

            mediaPlayer.currentTimeProperty().addListener((obs, oldT, newT) -> {
                if (mediaPlayer.getTotalDuration().toSeconds() > 0) {
                    double progress = newT.toSeconds() / mediaPlayer.getTotalDuration().toSeconds();
                    progressPercent.set(progress);
                }
            });

            mediaPlayer.setOnEndOfMedia(this::playNext);
            mediaPlayer.play();

        } catch (Exception e) {
            System.err.println("Failed to play song: " + e.getMessage());
        }
    }

    public void seek(double percent) {
        if (mediaPlayer != null && mediaPlayer.getTotalDuration().greaterThan(Duration.ZERO)) {
            double total = mediaPlayer.getTotalDuration().toMillis();
            mediaPlayer.seek(Duration.millis(percent * total));
        }
    }

    public void playNext() {
        if (currentQueue == null || currentQueue.isEmpty()) return;
        currentIndex = (currentIndex + 1) % currentQueue.size();
        playCurrent();
    }

    public void playPrevious() {
        if (currentQueue == null || currentQueue.isEmpty()) return;
        currentIndex = (currentIndex - 1 + currentQueue.size()) % currentQueue.size();
        playCurrent();
    }

    public void pause() {
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        progressPercent.set(0);
    }

    public Duration getCurrentTime() {
        return (mediaPlayer != null) ? mediaPlayer.getCurrentTime() : Duration.ZERO;
    }

    public Duration getTotalDuration() {
        return (mediaPlayer != null) ? mediaPlayer.getTotalDuration() : Duration.UNKNOWN;
    }
}
🎛️ Step 3: Update MainController.java
Add new FXML elements
At the top of your controller:

java
@FXML private Label lblNowPlaying;
@FXML private Label lblCurrentTime;
@FXML private Label lblTotalTime;
@FXML private Slider sliderProgress;
Initialize Bindings
In your initialize() method (after setting up buttons):

java
playerManager.setOnSongChange(song -> lblNowPlaying.setText("Now Playing: " + song.getTitle() + " – " + song.getArtist()));

        playerManager.progressProperty().addListener((obs, oldV, newV) -> {
        sliderProgress.setValue(newV.doubleValue() * 100);
updateTimeLabels();
});

        sliderProgress.setOnMouseReleased(e ->
        playerManager.seek(sliderProgress.getValue() / 100)
        );
Add Time Label Updates
Define this helper in the controller:

java
private void updateTimeLabels() {
    double current = playerManager.getCurrentTime().toSeconds();
    double total = playerManager.getTotalDuration().toSeconds();

    lblCurrentTime.setText(formatTime(current));
    lblTotalTime.setText(total > 0 ? formatTime(total) : "--:--");
}

private String formatTime(double seconds) {
    int mins = (int) seconds / 60;
    int secs = (int) seconds % 60;
    return String.format("%02d:%02d", mins, secs);
}
🎯 How It Works
✅ When a song starts:

PlayerManager publishes updates through progressProperty
The slider moves smoothly along with playback
The “Now Playing” label updates to the current song’s title/artist
You can drag the slider to seek within the song
🧰 Optional Enhancements
Add a volume slider:
xml
<Slider fx:id="sliderVolume" min="0" max="100" value="80" prefWidth="100" />
Then in initialize():

java
sliderVolume.valueProperty().addListener((obs, oldV, newV) -> {
        if (playerManager != null) {
// Only adjust if a song is playing
double vol = newV.doubleValue() / 100.0;
        playerManager.setVolume(vol);
    }
            });
and in PlayerManager simply add:

java
public void setVolume(double volume) {
    if (mediaPlayer != null) mediaPlayer.setVolume(volume);
}
💡 Result
You now have a fully interactive media control panel:
        ✅ Shows current playing song
✅ Live progress bar
✅ Seek control
✅ Time display
✅ Optional volume slider

Would you like me to show you next how to add saving and restoring the last played song / playlist (so playback resumes from where the user left off)?




Yes please

Perfect 😎 — that’s a great next step. Let’s make your myTunes app remember what was playing last, so when the user reopens it, playback resumes from the last played song (and optionally from within the same playlist).

We’ll do this in a way that’s simple, persistent, and database‑friendly.

🎯 Goal
When the user closes and relaunches the app:

The last played song and (if relevant) playlist are reloaded.
The table and lists highlight that song/playlist.
Optionally auto‑resume playback if desired.
🧠 Step 1: Decide How to Store This Info
You have two good options:

In the database (table Settings or new columns in Playlist)
In a local config file (e.g., lastSession.properties in your project folder)
Let’s implement the simple and flexible file‑based version, since it requires no schema changes.

🗃️ Step 2: Create SettingsManager.java
Path: src/dk/mytunes/bll/SettingsManager.java

        java
package dk.mytunes.bll;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsManager {

    private static final String FILE_NAME = "lastSession.properties";

    public void saveLastPlayed(int songId, Integer playlistId) {
        Properties props = new Properties();
        props.setProperty("songId", String.valueOf(songId));
        props.setProperty("playlistId", playlistId == null ? "" : String.valueOf(playlistId));
        try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
            props.store(out, "myTunes last session");
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    public int getLastSongId() {
        Properties props = loadProps();
        return Integer.parseInt(props.getProperty("songId", "-1"));
    }

    public Integer getLastPlaylistId() {
        Properties props = loadProps();
        String val = props.getProperty("playlistId", "");
        return val.isEmpty() ? null : Integer.parseInt(val);
    }

    private Properties loadProps() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(FILE_NAME)) {
            props.load(in);
        } catch (IOException ignored) {}
        return props;
    }
}
This will create a small text file like:

        #myTunes last session songId=12 playlistId=3
        🎛️ Step 3: Integrate With PlayerManager and Controller
You’ll save “last song” whenever a new song starts playing, and load it when the app starts.

Modify MainController.java
Add:

java
private SettingsManager settingsManager = new SettingsManager();
3a. Save When Song Changes
Inside your existing initialize() method, you linked onSongChange like this:

java
playerManager.setOnSongChange(song -> lblNowPlaying.setText("Now Playing: " + song.getTitle() + " – " + song.getArtist()));
Update it to also call the settings manager:

java
playerManager.setOnSongChange(song -> {
        lblNowPlaying.setText("Now Playing: " + song.getTitle() + " – " + song.getArtist());

Playlist currentPlaylist = playlistList.getSelectionModel().getSelectedItem();
Integer playlistId = currentPlaylist != null ? currentPlaylist.getId() : null;
    settingsManager.saveLastPlayed(song.getId(), playlistId);
        });
        3b. Restore at Startup
After you’ve loaded songs and playlists in initialize():

java
@FXML
private void initialize() {
    try {
        myTunesManager = new MyTunesManager();
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colArtist.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArtist()));
        loadSongs();
        loadPlaylists();

        restoreLastPlayed();  // <--- add this line here
    } catch (SQLException e) {
        e.printStackTrace();
    }
    ...
}
Then implement this helper method:

java
private void restoreLastPlayed() {
    try {
        int lastSongId = settingsManager.getLastSongId();
        Integer lastPlaylistId = settingsManager.getLastPlaylistId();
        if (lastSongId < 0) return;

        Song lastSong = myTunesManager.getAllSongs()
                .stream()
                .filter(s -> s.getId() == lastSongId)
                .findFirst()
                .orElse(null);

        if (lastSong != null) {
            // Highlight it in the UI
            songTable.getSelectionModel().select(lastSong);
            songTable.scrollTo(lastSong);

            if (lastPlaylistId != null) {
                Playlist playlist = myTunesManager.getAllPlaylists().stream()
                        .filter(p -> p.getId() == lastPlaylistId)
                        .findFirst()
                        .orElse(null);
                if (playlist != null) {
                    playlistList.getSelectionModel().select(playlist);
                }
            }

            // Optionally resume playback automatically
            // Uncomment if you want auto-start:
            // playerManager.playSong(lastSong);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
✅ What You Get
When you close the app after playing a song:

A small lastSession.properties file is saved with the song and playlist ID.
When you restart the app:
The song and playlist are selected, and with one click (or auto‑play) the music resumes exactly where you left off.
        🧰 Optional Enhancements
You can expand the SettingsManager to store other user preferences:

Volume level
Shuffle / repeat state
Window size and position
App theme (dark/light)
Example:

java
props.setProperty("volume", String.valueOf(currentVolume));
Now your app feels like a real media player — memorizing what you played and picking up right where you left off. 🪩

Would you like me to show you next how to implement a “filter/search bar” above the songs table (so users can quickly find songs by title or artist)?




Yes please

Fantastic 🎶 — adding a filter/search bar will make your myTunes app much more user‑friendly.
        We’ll implement it so users can type part of a title or artist, and the song table instantly updates to show only matching results.

🧭 Overview
We’ll add:

        🔍 A TextField and “Filter / Clear” Button to the UI (top area)
🧠 Logic in MainController to filter songs dynamically
⚙️ Integration with your existing MyTunesManager (and optionally DAO filtering later)
🎨 Step 1: Update MainView.fxml
Locate the <top> region in your main layout (or add one if you don’t have it yet).

xml
        <top>
    <HBox spacing="10" alignment="CENTER_LEFT" padding="10">
        <Label text="Filter:"/>
        <TextField fx:id="txtFilter" promptText="Type song title or artist..." prefWidth="200"/>
        <Button text="Filter" fx:id="btnFilter"/>
    </HBox>
</top>
That gives you a simple filter bar above your tables.

        🧠 Step 2: Update MainController.java
Add the two new UI fields at the top:

java
@FXML private TextField txtFilter;
@FXML private Button btnFilter;

private boolean isFiltered = false;
private List<Song> allSongs = new ArrayList<>();
Initialize Filter Logic
Update your initialize() method after loading songs:

java
@FXML
private void initialize() {
    try {
        myTunesManager = new MyTunesManager();

        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colArtist.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArtist()));

        loadSongs();
        loadPlaylists();
        restoreLastPlayed();
    } catch (SQLException e) {
        e.printStackTrace();
    }

    // Filter button logic
    btnFilter.setOnAction(e -> {
        if (!isFiltered) {
            handleFilter();
        } else {
            clearFilter();
        }
    });
}
Filtering Methods
Add these below your other helper methods:

java
private void loadSongs() throws SQLException {
    allSongs = myTunesManager.getAllSongs();
    songTable.getItems().setAll(allSongs);
}

private void handleFilter() {
    String query = txtFilter.getText().trim().toLowerCase();
    if (query.isEmpty()) {
        return;
    }

    List<Song> filtered = allSongs.stream()
            .filter(s -> s.getTitle().toLowerCase().contains(query) || s.getArtist().toLowerCase().contains(query))
            .toList();

    songTable.getItems().setAll(filtered);
    isFiltered = true;
    btnFilter.setText("Clear");
}

private void clearFilter() {
    songTable.getItems().setAll(allSongs);
    txtFilter.clear();
    isFiltered = false;
    btnFilter.setText("Filter");
}
🧬 Step 3: Optional DAO‑Based Filtering
If you’d rather filter directly from the database instead of loading all songs into memory (for large collections), you can add this method to your SongDAO:

java
public List<Song> getFilteredSongs(String query) throws SQLException {
    List<Song> songs = new ArrayList<>();
    String sql = "SELECT * FROM Song WHERE LOWER(title) LIKE ? OR LOWER(artist) LIKE ?;";
    try (Connection con = DBConnector.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        String q = "%" + query.toLowerCase() + "%";
        ps.setString(1, q);
        ps.setString(2, q);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            songs.add(new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("artist"),
                    rs.getString("path"),
                    rs.getString("duration")
            ));
        }
    }
    return songs;
}
Then just replace your handleFilter() logic with:

java
private void handleFilter() {
    String query = txtFilter.getText().trim();
    if (query.isEmpty()) return;

    try {
        List<Song> filtered = myTunesManager.getFilteredSongs(query);
        songTable.getItems().setAll(filtered);
        isFiltered = true;
        btnFilter.setText("Clear");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
…and add this to your BLL:

java
public List<Song> getFilteredSongs(String query) throws SQLException {
    return songDAO.getFilteredSongs(query);
}
✅ How It Works
User types in the filter box → clicks “Filter”.
The app compares lowercase title or artist to the search string.
Matching rows are shown; “Filter” becomes “Clear”.
Clicking “Clear” restores the full song list.
💡 Optional Enhancements
You can make the filter even smoother:

Enable live filtering on text input (no need to press “Filter”):
java
txtFilter.textProperty().addListener((obs, oldV, newV) -> handleLiveFilter(newV));
Add total count label (e.g., "Showing 12 of 150 songs")
Make the filter also search playlists or album names
💾 Example in Action
🔥 Before:

All Songs: [ 200 total ]
User types “Eminem” → presses Filter
👉 Table updates to only show Eminem's songs
Button turns to Clear

Summary
✅ Added TextField + Button in FXML
✅ Implemented search filter in controller
✅ Optional database query for efficiency
✅ “Filter” toggles to “Clear” seamlessly

Would you like me to show you how to implement the “Add Song to Playlist” / “Move Up / Down” / “Remove from Playlist” functionality next — so playlists actually manage their songs dynamically?




Yes please

Perfect — this next feature is the heart of the playlist system 💿
We’ll make playlists actually store songs, and allow the user to:

        ➕ Add a song to a playlist
🔼 Move songs up/down inside the playlist
❌ Remove a song from a playlist
        When finished, your myTunes app will behave like a real music manager — not just a song browser.

🧭 Step 1: Update Your GUI (MainView.fxml)
We’ll expand the middle section that shows songs inside the selected playlist.

Replace your existing <center> block with this enhanced version:

xml
        <center>
    <VBox spacing="8" padding="10">
        <Label text="Songs in Playlist"/>
        <ListView fx:id="playlistSongsList" prefHeight="250"/>

        <HBox spacing="6" alignment="CENTER">
            <Button text="⬆ Move Up" fx:id="btnMoveUp"/>
            <Button text="⬇ Move Down" fx:id="btnMoveDown"/>
            <Button text="➖ Remove" fx:id="btnRemoveSongFromPlaylist"/>
        </HBox>

        <HBox spacing="6" alignment="CENTER">
            <Button text="⬅ Add to Playlist" fx:id="btnAddToPlaylist"/>
        </HBox>
    </VBox>
</center>
        💡 The idea is:

You select a playlist on the left.
Select a song on the right (in the Songs table).
Then press ⬅ Add to Playlist — the song appears in the list in the middle.
Use the Move Up/Down/Delete buttons to reorder/remove songs.
        🧩 Step 2: Update Playlist Entity (Playlist.java)
We’ll keep the order of songs as a List<Song>.

Make sure your class looks something like this:

java
package dk.mytunes.be;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Playlist {
    private int id;
    private String name;
    private List<Song> songs;

    public Playlist(int id, String name) {
        this.id = id;
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public Playlist(String name) {
        this(-1, name);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<Song> getSongs() { return songs; }

    public void addSong(Song song) { songs.add(song); }

    public void removeSong(Song song) { songs.remove(song); }

    public void moveSongUp(Song song) {
        int index = songs.indexOf(song);
        if (index > 0) {
            Collections.swap(songs, index, index - 1);
        }
    }

    public void moveSongDown(Song song) {
        int index = songs.indexOf(song);
        if (index >= 0 && index < songs.size() - 1) {
            Collections.swap(songs, index, index + 1);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
🧠 Step 3: Extend Your Database Table
You’ll need a join table that maps playlist-song relationships including order.

SQL Example:

sql
CREATE TABLE PlaylistSong (
        playlist_id INTEGER,
        song_id INTEGER,
        position INTEGER,
        FOREIGN KEY (playlist_id) REFERENCES Playlist(id),
FOREIGN KEY (song_id) REFERENCES Song(id)
        );
        ⚙️ Step 4: Add PlaylistDAO Logic
Inside PlaylistDAO.java, handle CRUD for playlist–song relations.

java
package dk.mytunes.dal;

import dk.mytunes.be.Playlist;
import dk.mytunes.be.Song;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {

    public List<Playlist> getAllPlaylists() throws SQLException {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT * FROM Playlist;";

        try (Connection con = DBConnector.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Playlist playlist = new Playlist(rs.getInt("id"), rs.getString("name"));
                loadSongsForPlaylist(playlist);
                playlists.add(playlist);
            }
        }
        return playlists;
    }

    public void loadSongsForPlaylist(Playlist playlist) throws SQLException {
        String sql = """
            SELECT s.id, s.title, s.artist, s.path, s.duration, ps.position
            FROM PlaylistSong ps
            JOIN Song s ON s.id = ps.song_id
            WHERE ps.playlist_id = ?
            ORDER BY ps.position;
            """;

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playlist.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Song song = new Song(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("path"),
                        rs.getString("duration")
                );
                playlist.addSong(song);
            }
        }
    }

    public void addSongToPlaylist(Playlist playlist, Song song) throws SQLException {
        String sql = "INSERT INTO PlaylistSong (playlist_id, song_id, position) VALUES (?, ?, ?)";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playlist.getId());
            ps.setInt(2, song.getId());
            ps.setInt(3, playlist.getSongs().size() + 1);
            ps.executeUpdate();
        }
    }

    public void removeSongFromPlaylist(Playlist playlist, Song song) throws SQLException {
        String sql = "DELETE FROM PlaylistSong WHERE playlist_id = ? AND song_id = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playlist.getId());
            ps.setInt(2, song.getId());
            ps.executeUpdate();
        }
    }

    public void updateSongOrder(Playlist playlist) throws SQLException {
        String sql = "UPDATE PlaylistSong SET position = ? WHERE playlist_id = ? AND song_id = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < playlist.getSongs().size(); i++) {
                Song s = playlist.getSongs().get(i);
                ps.setInt(1, i + 1);
                ps.setInt(2, playlist.getId());
                ps.setInt(3, s.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
🧠 Step 5: Add Business Logic in MyTunesManager
        java
public void addSongToPlaylist(Playlist playlist, Song song) throws SQLException {
    playlist.addSong(song);
    playlistDAO.addSongToPlaylist(playlist, song);
}

public void removeSongFromPlaylist(Playlist playlist, Song song) throws SQLException {
    playlist.removeSong(song);
    playlistDAO.removeSongFromPlaylist(playlist, song);
}

public void moveSongUpInPlaylist(Playlist playlist, Song song) throws SQLException {
    playlist.moveSongUp(song);
    playlistDAO.updateSongOrder(playlist);
}

public void moveSongDownInPlaylist(Playlist playlist, Song song) throws SQLException {
    playlist.moveSongDown(song);
    playlistDAO.updateSongOrder(playlist);
}
🎮 Step 6: Wire the Buttons in MainController
At the top:

java
@FXML private Button btnAddToPlaylist;
@FXML private Button btnMoveUp;
@FXML private Button btnMoveDown;
@FXML private Button btnRemoveSongFromPlaylist;
Then in initialize():

java
btnAddToPlaylist.setOnAction(e -> handleAddToPlaylist());
        btnMoveUp.setOnAction(e -> handleMoveSongUp());
        btnMoveDown.setOnAction(e -> handleMoveSongDown());
        btnRemoveSongFromPlaylist.setOnAction(e -> handleRemoveFromPlaylist());
Now implement the logic:

java
private void handleAddToPlaylist() {
    Playlist selectedPlaylist = playlistList.getSelectionModel().getSelectedItem();
    Song selectedSong = songTable.getSelectionModel().getSelectedItem();

    if (selectedPlaylist != null && selectedSong != null) {
        try {
            myTunesManager.addSongToPlaylist(selectedPlaylist, selectedSong);
            refreshPlaylistSongs(selectedPlaylist);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

private void handleMoveSongUp() {
    Playlist playlist = playlistList.getSelectionModel().getSelectedItem();
    Song selected = playlistSongsList.getSelectionModel().getSelectedItem();
    if (playlist != null && selected != null) {
        try {
            myTunesManager.moveSongUpInPlaylist(playlist, selected);
            refreshPlaylistSongs(playlist);
            playlistSongsList.getSelectionModel().select(selected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

private void handleMoveSongDown() {
    Playlist playlist = playlistList.getSelectionModel().getSelectedItem();
    Song selected = playlistSongsList.getSelectionModel().getSelectedItem();
    if (playlist != null && selected != null) {
        try {
            myTunesManager.moveSongDownInPlaylist(playlist, selected);
            refreshPlaylistSongs(playlist);
            playlistSongsList.getSelectionModel().select(selected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

private void handleRemoveFromPlaylist() {
    Playlist playlist = playlistList.getSelectionModel().getSelectedItem();
    Song selected = playlistSongsList.getSelectionModel().getSelectedItem();
    if (playlist != null && selected != null) {
        try {
            myTunesManager.removeSongFromPlaylist(playlist, selected);
            refreshPlaylistSongs(playlist);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

private void refreshPlaylistSongs(Playlist playlist) {
    playlistSongsList.getItems().setAll(playlist.getSongs());
}
🧩 Step 7: Update Selection Logic
When a playlist is selected (in your existing playlistList view), load its songs:

java
playlistList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
        if (newV != null) {
        playlistSongsList.getItems().setAll(newV.getSongs());
        }
        });
        ✅ Result
🎧 You now have a fully interactive playlist system:

Select a playlist → see its songs.
Select a song → click “⬅ Add to Playlist”.
Reorder with “⬆ / ⬇”.
Remove songs safely.
Everything saved in the database with correct order.
        💡 Optional Improvements
Auto‑refresh the playlist view after edits
Display total playlist duration
Enable keyboard shortcuts (e.g. ↑, ↓, Delete) for manipulation
Confirm before removing a song from a playlist
Would you like me to show you next how to display total playlist duration and update it automatically when songs are added/removed/moved? (It’s a nice finishing touch to the playlist system.)
*/