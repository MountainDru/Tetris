/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.bejinariu.controller;

import at.bejinariu.models.Highscore;
import java.net.URL;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
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
        listScores.addAll(
                    Files.lines(new File("src/main/resources/files/scores.csv").toPath())
                            .filter(line -> line != null && !line.isEmpty())
                            .map(line -> Highscore.fromCSVLine(line))
                            .collect(Collectors.toList()));
            
        } catch (IOException e) {
            System.out.println(e.getClass() + " " + e.getMessage());
        }
        tblTableList.sort();
    }
}
