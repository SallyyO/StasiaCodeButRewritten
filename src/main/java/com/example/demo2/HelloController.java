package com.example.demo2;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        // Navigate to the Player screen
        try {
            // Load the working player view instead of the unfinished mytunes view
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("player-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeText.getScene().getWindow();
            stage.setTitle("MyTunes");
            stage.setScene(new Scene(root, 1100, 650));
        } catch (IOException e) {
            welcomeText.setText("Failed to open player: " + e.getMessage());
        }
    }
}
