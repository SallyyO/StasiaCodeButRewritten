package com.example.demo2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.List;

public class PlayerController {

    @FXML
    private ListView<File> songsList;

    @FXML
    private Label statusLabel;

    private final ObservableList<File> selectedSongs = FXCollections.observableArrayList();
    private MediaPlayer mediaPlayer;

    @FXML
    private void initialize() {
        songsList.setItems(selectedSongs);
        songsList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    @FXML
    private void onChooseSongs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose audio files");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a"));
        Window owner = statusLabel.getScene() != null ? statusLabel.getScene().getWindow() : null;
        List<File> files = chooser.showOpenMultipleDialog(owner);
        if (files != null && !files.isEmpty()) {
            selectedSongs.setAll(files);
            statusLabel.setText("Loaded " + files.size() + " song(s)");
        }
    }

    @FXML
    private void onPlay() {
        File file = songsList.getSelectionModel().getSelectedItem();
        if (file == null && !selectedSongs.isEmpty()) {
            file = selectedSongs.get(0);
            songsList.getSelectionModel().select(0);
        }
        if (file == null) {
            statusLabel.setText("No song selected");
            return;
        }
        playFile(file);
    }

    @FXML
    private void onPause() {
        if (mediaPlayer != null) {
            switch (mediaPlayer.getStatus()) {
                case PLAYING -> {
                    mediaPlayer.pause();
                    statusLabel.setText("Paused");
                }
                case PAUSED -> {
                    mediaPlayer.play();
                    statusLabel.setText("Playing");
                }
            }
        }
    }

    @FXML
    private void onStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            statusLabel.setText("Stopped");
        }
    }

    private void playFile(File file) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            statusLabel.setText("Loading: " + file.getName());
            File finalFile = file;
            mediaPlayer.setOnReady(() -> {
                statusLabel.setText("Playing: " + finalFile.getName());
                mediaPlayer.play();
            });
            mediaPlayer.setOnEndOfMedia(() -> statusLabel.setText("Finished: " + finalFile.getName()));
            mediaPlayer.setOnError(() -> statusLabel.setText("Error: " + mediaPlayer.getError()));
        } catch (Exception ex) {
            statusLabel.setText("Could not play file: " + ex.getMessage());
        }
    }
}
