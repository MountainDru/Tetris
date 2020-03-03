/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.bejinariu.controller;

import at.bejinariu.models.Highscore;
import at.bejinariu.models.Piece;
import at.bejinariu.tetris.MainApp;
import at.bejinariu.tetris.Utilities;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Dru
 */
public class EndgamePaneController implements Initializable {

    @FXML
    private Label lblPoints;
    @FXML
    private TextField lblPlayername;

    private static Image ICON = MainApp.APP_ICON;

    @FXML
    private AnchorPane root;
    private final BooleanProperty saveButtonDiasable = new SimpleBooleanProperty(false, null);
    @FXML
    private Button btnSave;
    private Stage highscoreScreen;
    private HighscorePaneController highscoreController;

    private int score;
    private String playerName;

    private boolean isSaveButtonDiasable() {
        return saveButtonDiasable.get();
    }

    private void setSaveButtonDiasable(boolean value) {
        saveButtonDiasable.set(value);
    }

    private BooleanProperty saveButtonDiasableProperty() {
        return saveButtonDiasable;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnSave.disableProperty().bind(saveButtonDiasable);
        if (root.getScene() != null) {
            try {
                root.getScene().getWindow().setOnCloseRequest(event -> saveButtonDiasable.set(false));
                FXMLLoader highscoreScreenLoader = new FXMLLoader();
                highscoreScreenLoader.setLocation(getClass().getResource("/fxml/HighscorePane.fxml"));
                highscoreScreen = new Stage();
                highscoreScreen.setScene(new Scene(highscoreScreenLoader.load()));
                highscoreScreen.getIcons().add(MainApp.APP_ICON);
                highscoreScreen.setResizable(false);
                highscoreController = highscoreScreenLoader.getController();
            } catch (Exception e) {
                System.out.println(e.getClass() + " " + e.getMessage());
            }
        }
    }

    @FXML
    private void onActionSaveScore(ActionEvent event) {
        if (lblPlayername.getText().isEmpty()) {
            Utilities.showNormalMessage("Information", "Field is empty", "In order to save your score, you must type in a valid name. ", Alert.AlertType.INFORMATION, ICON, false);
            return;
        }
        Highscore hs = new Highscore(score,
                lblPlayername.getText(),
                LocalDateTime.now());
        try {
            File scoresFile = new File(Piece.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scoresFile.getParentFile().getPath() + "/scores.csv", Boolean.TRUE))) {
                bw.newLine();
                bw.write(hs.toCSVLine());
                bw.flush();
            } catch (Exception io) {
                System.out.println(io.getClass() + " " + io.getMessage());
            }
        } catch (URISyntaxException io) {
            System.out.println(io.getClass() + " " + io.getMessage());
        }
        Utilities.showNormalMessage("Information", "Score stored successfully!", "", Alert.AlertType.INFORMATION, ICON, false);
        saveButtonDiasable.set(true);

    }

    @FXML
    private void onActionList(ActionEvent event) {
        highscoreController.readEntries();
        highscoreScreen.show();
    }

    @FXML
    private void onActionCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage thisStage = (Stage) this.root.getScene().getWindow();
        saveButtonDiasable.set(false);
        thisStage.close();
    }

    public void updatePoints() {
        lblPlayername.setText("");
        this.lblPoints.setText("Congratulations! You have achieved " + score + " points!");
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

}
