/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.bejinariu.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Dru
 */
public class Highscore {

    public static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEEE 'the' dd. 'of' MMMM yyyy 'at' HH:mm", Locale.UK);
    private final IntegerProperty playerScore = new SimpleIntegerProperty(0, null);
    private final StringProperty playerName = new SimpleStringProperty(null, null);
    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>(null, null);

  
    public IntegerProperty playerScoreProperty(){
        return playerScore;
    }
    
   
    public int getPlayerScore() {
        return playerScore.get();
    }

    public void setPlayerScore(int value) {
        playerScore.set(value);
    }

    public String getPlayerName() {
        return playerName.get();
    }

    public void setPlayerName(String value) {
        playerName.set(value);
    }

    public StringProperty playerNameProperty() {
        return playerName;
    }

    public LocalDateTime getDate() {
        return date.get();
    }

    public void setDate(LocalDateTime value) {
        date.set(value);
    }

    public ObjectProperty dateProperty() {
        return date;
    }

 
    public Highscore(int playerScore, String playerName, LocalDateTime date) {
        this.date.set(date);
        this.playerName.set(playerName);
        this.playerScore.set(playerScore);
    }

    public String toCSVLine() {
        StringBuilder line = new StringBuilder();
        line.append(playerName.get());
        line.append(";");
        line.append(playerScore.get());
        line.append(";");
        line.append(date.get().format(dateFormat));
        return line.toString(); 
    }

    public static Highscore fromCSVLine(String line) {
        String parts[] = line.split(";");
        return new Highscore(Integer.parseInt(parts[1]),
                parts[0],
                LocalDateTime.parse(parts[2], dateFormat));
    }

}
