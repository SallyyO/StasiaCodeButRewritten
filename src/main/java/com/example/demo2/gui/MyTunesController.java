package com.example.demo2.gui;

import com.example.demo2.bll.PlaylistService;
import com.example.demo2.bll.SongService;
import com.example.demo2.entities.Playlist;
import com.example.demo2.entities.Song;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.logging.Level;

/**
 * Main controller for the MyTunes UI.
 * Handles songs and playlists management and playback.
 */
public class MyTunesController {
    // Top toolbar
    @FXML private Label statusLabel;

    // Left: playlists
    @FXML private ListView<Playlist> playlistList;

    // Center: songs in selected playlist
    @FXML private ListView<Song> playlistSongsList;

    // Right: all songs table and filter
    @FXML private TextField filterField;
    @FXML private Button filterButton;
    @FXML private TableView<Song> songTable;
    @FXML private TableColumn<Song, String> titleCol;
    @FXML private TableColumn<Song, String> artistCol;
    @FXML private TableColumn<Song, String> durationCol;

    private final SongService songService = new SongService();
    private final PlaylistService playlistService = new PlaylistService();

    private final ObservableList<Song> songs = FXCollections.observableArrayList();
    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private final ObservableList<Song> songsInSelectedPlaylist = FXCollections.observableArrayList();

    // Playback state
    private MediaPlayer mediaPlayer;
    private ObservableList<Song> currentQueue = FXCollections.observableArrayList();
    private int currentIndex = -1;

    @FXML
    private void initialize() {
        setupListViews(); //New method1
        setupTableColumns(); //New method2
        setupEventHandlers(); //New method3

        // Selection changes
        playlistList.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            try {
                refreshPlaylistSongs();
            } catch (Exception e) {
                showError("Failed to load playlist songs", e);
            }
        });

        // Initial load
        reloadAll();
    }

    //New method for setting up the table columns
    private void setupTableColumns() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("Title"));
        artistCol.setCellValueFactory(new PropertyValueFactory<>("Artist"));
        durationCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDurationDisplay()));
        songTable.setItems(songs);
    }

    //The other new method
    private void setupListViews() {
        playlistList.setItems(playlists);
        playlistSongsList.setItems(songsInSelectedPlaylist);

        playlistList.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            refreshPlaylistSongs();
        });
    }

    private void refreshPlaylistSongs() {
        try {
            Playlist selected = playlistList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                songsInSelectedPlaylist.clear();
            } else {
                songsInSelectedPlaylist.setAll(playlistService.getSongs(selected.getId()));
            }
        } catch (Exception e) { showError("Failed to refresh", e);
        }
    }

    private void reloadAll() {
        try {
            songs.setAll(songService.getAll());
            playlists.setAll(playlistService.getAll());
            refreshPlaylistSongs();
            // Try to auto-fill durations for any songs that have unknown duration (0)
            autofillUnknownDurations();
        } catch (Exception e) {
            showError("Failed to load data", e);
        }
    }

    //Third new method, here for figuring out what song to play
    private void setupEventHandlers() {
        // Double-click song in table to play
        songTable.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    playFromTable(row.getItem());
                }
            });
            return row;
        });

        // Double-click song in playlist to play
        playlistSongsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song s = playlistSongsList.getSelectionModel().getSelectedItem();
                if (s != null) playFromPlaylist(s);
            }
        });
    }

    // Toolbar: playback
    @FXML private void onPlay() {
        // Determine current context: playlist if selected and has songs, else song table
        Playlist selPl = playlistList.getSelectionModel().getSelectedItem();
        if (selPl != null && !songsInSelectedPlaylist.isEmpty()) {
            Song song = playlistSongsList.getSelectionModel().getSelectedItem();
            if (song == null) {
                playlistSongsList.getSelectionModel().select(0);
                song = playlistSongsList.getSelectionModel().getSelectedItem();
            }
            if (song != null) playQueue(songsInSelectedPlaylist, playlistSongsList.getSelectionModel().getSelectedIndex());
            return;
        }

        Song song = songTable.getSelectionModel().getSelectedItem();
        if (song == null && !songs.isEmpty()) {
            songTable.getSelectionModel().select(0);
            song = songTable.getSelectionModel().getSelectedItem();
        }
        if (song != null) playQueue(songs, songTable.getSelectionModel().getSelectedIndex());
        else statusLabel.setText("No song to play");
    }

    @FXML private void onPause() {
        if (mediaPlayer == null) return;
        switch (mediaPlayer.getStatus()) {
            case PLAYING -> { mediaPlayer.pause(); statusLabel.setText("Paused"); }
            case PAUSED -> { mediaPlayer.play(); statusLabel.setText("Playing"); }
            default -> {}
        }
    }

    @FXML private void onStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            statusLabel.setText("Stopped");
        }
    }

    private void playFromTable(Song s) { playQueue(songs, songs.indexOf(s)); }
    private void playFromPlaylist(Song s) { playQueue(songsInSelectedPlaylist, songsInSelectedPlaylist.indexOf(s)); }

    private void playQueue(ObservableList<Song> queue, int startIndex) {
        if (startIndex < 0 || startIndex >= queue.size()) return;
        currentQueue = FXCollections.observableArrayList(queue);
        currentIndex = startIndex;
        playCurrent();
    }

    private void playCurrent() {
        if (currentIndex < 0 || currentIndex >= currentQueue.size()) {
            statusLabel.setText("Finished queue");
            return;
        }
        Song s = currentQueue.get(currentIndex);
        try {
            if (mediaPlayer != null) mediaPlayer.stop();
            Media media = new Media(new File(s.getFilePath()).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            statusLabel.setText("Loading: " + s.getTitle());
            mediaPlayer.setOnReady(() -> {
                statusLabel.setText("Playing: " + s.getTitle());
                mediaPlayer.play();
            });
            mediaPlayer.setOnEndOfMedia(() -> {
                currentIndex++;
                playCurrent();
            });
            mediaPlayer.setOnError(() -> statusLabel.setText("Error: " + mediaPlayer.getError()));
        } catch (Exception ex) {
            statusLabel.setText("Could not play: " + s.getTitle());
        }
    }

    // Playlists
    @FXML private void onNewPlaylist() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("New Playlist");
        dlg.setHeaderText("Create new playlist");
        dlg.setContentText("Name:");
        Optional<String> res = dlg.showAndWait();
        res.map(String::trim).filter(n -> !n.isBlank()).ifPresent(name -> {
            try {
                playlistService.create(new Playlist(name));
                playlists.setAll(playlistService.getAll());
            } catch (Exception e) { showError("Failed to create playlist", e); }
        });
    }

    @FXML private void onEditPlaylist() {
        Playlist sel = playlistList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        TextInputDialog dlg = new TextInputDialog(sel.getName());
        dlg.setTitle("Edit Playlist");
        dlg.setHeaderText("Rename playlist");
        dlg.setContentText("Name:");
        dlg.showAndWait().map(String::trim).filter(n -> !n.isBlank()).ifPresent(name -> {
            try {
                sel.setName(name);
                playlistService.rename(sel);
                playlists.setAll(playlistService.getAll());
                playlistList.getSelectionModel().select(sel);
            } catch (Exception e) { showError("Failed to rename playlist", e); }
        });
    }

    @FXML private void onDeletePlaylist() {
        Playlist sel = playlistList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Playlist");
        confirm.setHeaderText("Delete playlist '" + sel.getName() + "'?");
        confirm.setContentText("This will not delete the songs.");
        if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
            try {
                playlistService.delete(sel.getId());
                playlists.setAll(playlistService.getAll());
                songsInSelectedPlaylist.clear();
            } catch (Exception e) { showError("Failed to delete playlist", e); }
        }
    }

    @FXML private void onMoveUp() {
        Playlist sel = playlistList.getSelectionModel().getSelectedItem();
        int idx = playlistSongsList.getSelectionModel().getSelectedIndex();
        if (sel == null || idx <= 0) return;
        try {
            playlistService.move(sel.getId(), idx, idx - 1);
            refreshPlaylistSongs();
            playlistSongsList.getSelectionModel().select(idx - 1);
        } catch (Exception e) { showError("Failed to move", e); }
    }

    @FXML private void onMoveDown() {
        Playlist sel = playlistList.getSelectionModel().getSelectedItem();
        int idx = playlistSongsList.getSelectionModel().getSelectedIndex();
        if (sel == null || idx < 0 || idx >= songsInSelectedPlaylist.size() - 1) return;
        try {
            playlistService.move(sel.getId(), idx, idx + 1);
            refreshPlaylistSongs();
            playlistSongsList.getSelectionModel().select(idx + 1);
        } catch (Exception e) { showError("Failed to move", e); }
    }

    @FXML private void onRemoveFromPlaylist() {
        Playlist sel = playlistList.getSelectionModel().getSelectedItem();
        int idx = playlistSongsList.getSelectionModel().getSelectedIndex();
        if (sel == null || idx < 0) return;
        try {
            playlistService.removeAtPosition(sel.getId(), idx);
            refreshPlaylistSongs();
        } catch (Exception e) { showError("Failed to remove from playlist", e); }
    }

    // Songs
    @FXML private void onNewSong() { showSongDialog(null); }
    @FXML private void onEditSong() { showSongDialog(songTable.getSelectionModel().getSelectedItem()); }

    @FXML private void onDeleteSong() {
        Song sel = songTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Song");
        confirm.setHeaderText("Delete song '" + sel.getTitle() + "'?");
        CheckBox deleteFile = new CheckBox("Also delete the file from disk");
        confirm.getDialogPane().setContent(deleteFile);
        if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
            try {
                songService.delete(sel.getId());
                if (deleteFile.isSelected()) {
                    try { new File(sel.getFilePath()).delete(); } catch (Exception ignored) {}
                }
                songs.setAll(songService.getAll());
                refreshPlaylistSongs();
            } catch (Exception e) { showError("Failed to delete song", e); }
        }
    }

    @FXML private void onAddToPlaylist() {
        Playlist pl = playlistList.getSelectionModel().getSelectedItem();
        Song song = songTable.getSelectionModel().getSelectedItem();
        if (pl == null || song == null) return;
        try {
            playlistService.addSongToEnd(pl.getId(), song.getId());
            refreshPlaylistSongs();
        } catch (Exception e) { showError("Failed to add to playlist", e); }
    }

    @FXML private void onFilterToggle() {
        try {
            if ("Filter".equals(filterButton.getText())) {
                List<Song> filtered = songService.search(filterField.getText().trim());
                songs.setAll(filtered);
                filterButton.setText("Clear");
            } else {
                songs.setAll(songService.getAll());
                filterField.clear();
                filterButton.setText("Filter");
            }
        } catch (Exception e) { showError("Filter failed", e); }
    }

    private void showSongDialog(Song editing) {
        Dialog<Song> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "New Song" : "Edit Song");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField title = new TextField(editing != null ? editing.getTitle() : "");
        title.setPromptText("Title");
        TextField artist = new TextField(editing != null ? editing.getArtist() : "");
        artist.setPromptText("Artist");
        TextField path = new TextField(editing != null ? editing.getFilePath() : "");
        path.setPromptText("File path (.mp3/.wav)");
        Button choose = new Button("Choose...");
        choose.setOnAction(e -> {
            FileChooser ch = new FileChooser();
            ch.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav", "*.m4a"));
            File f = ch.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (f != null) path.setText(f.getAbsolutePath());
        });

        // Duration UI and state
        Label durationLbl = new Label("Unknown");
        final int[] computedDuration = { editing != null ? editing.getDurationSeconds() : 0 };
        if (computedDuration[0] > 0) durationLbl.setText(formatDuration(computedDuration[0]));

        // Helper to (re)probe duration when file path changes
        final MediaPlayer[] probeRef = { null };
        Runnable doProbe = () -> {
            String p = path.getText().trim();
            if (p.isEmpty()) {
                durationLbl.setText("No file");
                computedDuration[0] = 0;
                return;
            }
            File f = new File(p);
            if (!f.exists()) {
                durationLbl.setText("File not found");
                computedDuration[0] = 0;
                return;
            }
            // Cancel previous probe
            if (probeRef[0] != null) {
                try { probeRef[0].dispose(); } catch (Exception ignored) {}
                probeRef[0] = null;
            }
            durationLbl.setText("Reading…");
            try {
                Media media = new Media(f.toURI().toString());
                MediaPlayer mp = new MediaPlayer(media);
                probeRef[0] = mp;
                mp.setOnReady(() -> {
                    int secs = (int) Math.round(media.getDuration().toSeconds());
                    computedDuration[0] = Math.max(0, secs);
                    durationLbl.setText(computedDuration[0] > 0 ? formatDuration(computedDuration[0]) : "Unknown");
                    try { mp.dispose(); } catch (Exception ignored) {}
                    probeRef[0] = null;
                });
                mp.setOnError(() -> {
                    computedDuration[0] = 0;
                    durationLbl.setText("Unknown");
                    try { mp.dispose(); } catch (Exception ignored) {}
                    probeRef[0] = null;
                });
            } catch (Exception ex) {
                computedDuration[0] = 0;
                durationLbl.setText("Unknown");
            }
        };
        // Trigger probe when the path changes (either when typing or choosing)
        path.textProperty().addListener((obs, ov, nv) -> doProbe.run());
        // If editing and path already present, try to probe at dialog open (in case duration was 0)
        if (editing != null && path.getText() != null && !path.getText().isBlank() && editing.getDurationSeconds() == 0) {
            Platform.runLater(doProbe);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Title"), title);
        grid.addRow(1, new Label("Artist"), artist);
        grid.add(new Label("File"), 0, 2);
        grid.add(path, 1, 2);
        grid.add(choose, 2, 2);
        grid.addRow(3, new Label("Duration"), durationLbl);
        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (title.getText().isBlank() || artist.getText().isBlank() || path.getText().isBlank()) {
                showWarning("Please fill title, artist and choose a file");
                evt.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (editing == null) {
                    // Use computed duration if available
                    return new Song(title.getText().trim(), artist.getText().trim(), computedDuration[0], path.getText().trim());
                } else {
                    editing.setTitle(title.getText().trim());
                    editing.setArtist(artist.getText().trim());
                    editing.setFilePath(path.getText().trim());
                    // If a new duration was successfully computed (and > 0), update it
                    if (computedDuration[0] > 0) {
                        editing.setDurationSeconds(computedDuration[0]);
                    }
                    return editing;
                }
            }
            return null;
        });

        Optional<Song> res = dialog.showAndWait();
        if (res.isPresent()) {
            try {
                Song s = res.get();
                if (s.getId() == null) songService.create(s); else songService.update(s);
                songs.setAll(songService.getAll());
                songTable.refresh();
                refreshPlaylistSongs();
            } catch (Exception e) { showError("Failed to save song", e); }
        }
    }

    private void showError(String msg, Throwable e) {
        statusLabel.setText(msg);
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(msg);
        a.setContentText(e.getMessage());
        a.showAndWait();
    }

    private void showWarning(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // Helpers for duration probing and backfilling
    private static String formatDuration(int durationSeconds) {
        int s = durationSeconds % 60;
        int m = (durationSeconds / 60) % 60;
        int h = durationSeconds / 3600;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%d:%02d", m, s);
    }

    private void probeDuration(String filePath, IntConsumer onDone) {
        File f = new File(filePath);
        if (!f.exists()) { onDone.accept(0); return; }
        try {
            Media media = new Media(f.toURI().toString());
            MediaPlayer mp = new MediaPlayer(media);
            mp.setOnReady(() -> {
                int secs = (int) Math.round(media.getDuration().toSeconds());
                try { mp.dispose(); } catch (Exception ignored) {}
                onDone.accept(Math.max(0, secs));
            });
            mp.setOnError(() -> {
                try { mp.dispose(); } catch (Exception ignored) {}
                onDone.accept(0);
            });
        } catch (Exception ex) {
            onDone.accept(0);
        }
    }

    private void autofillUnknownDurations() {
        // Iterate over a copy to avoid mods happening concurrently
        for (Song s : List.copyOf(songs)) {
            if (s.getDurationSeconds() == 0) {
                File f = new File(s.getFilePath());
                if (f.exists()) {
                    probeDuration(s.getFilePath(), secs -> {
                        if (secs > 0) {
                            // Persist and update UI
                            Platform.runLater(() -> {
                                try {
                                    s.setDurationSeconds(secs);
                                    songService.update(s);
                                    songTable.refresh();
                                } catch (Exception ignored) { }
                            });
                        }
                    });
                }
            }
        }
    }
}
