/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.bejinariu.controller;

import at.bejinariu.models.Highscore;
import at.bejinariu.models.Piece;
import java.net.URL;
import java.io.*;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author Dru
 */
public class HighscorePaneController implements Initializable {

    @FXML
    private TableView<Highscore> tblTableList;
    @FXML
    private TableColumn<Highscore, String> colPlayer;
    @FXML
    private TableColumn<Highscore, String> colScore;
    @FXML
    private TableColumn<Highscore, String> colDate;
    private final ObservableList<Highscore> listScores = FXCollections.observableArrayList();
    private static final String pointsExtension = " Points";

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colDate.setCellValueFactory(
                cell -> new SimpleStringProperty(((LocalDateTime) cell.getValue().dateProperty().get()).format(Highscore.dateFormat)));
        colScore.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().playerScoreProperty().get() + pointsExtension));
        colPlayer.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().playerNameProperty().get()));

        tblTableList.setEditable(false);

        colScore.setComparator((p1, p2) -> -Integer.compare(Integer.parseInt(p1.replace(pointsExtension, "")),
                Integer.parseInt(p2.replace(pointsExtension, ""))));
        tblTableList.setItems(listScores);
        tblTableList.getSortOrder().clear();
        tblTableList.getSortOrder().add(colScore);
    }

    public void readEntries() {
        listScores.clear();
        try {
            File scoresFile = new File(Piece.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(scoresFile.getParentFile().getPath() + "/scores.csv")))) {
                String line = null;
                while ((line = buffer.readLine()) != null && !line.isEmpty()) {
                    listScores.add(Highscore.fromCSVLine(line));
                }
            } catch (Exception e) {
                System.out.println(e.getClass() + " " + e.getMessage());
            }
            tblTableList.sort();
        } catch (URISyntaxException e) {
            System.out.println(e.getClass() + " " + e.getMessage());
        }

    }
}
