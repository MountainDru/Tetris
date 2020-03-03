/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.bejinariu.controller;

//java -jar Own_Games/Tetris/target/Tetris-1.0-SNAPSHOT.jar


import at.bejinariu.models.Piece;
import at.bejinariu.tetris.MainApp;
import at.bejinariu.models.Timer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.util.Pair;
import java.util.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author Dru
 */
public class SceneController implements Initializable {

    @FXML
    private BorderPane fxmlRoot;
    @FXML
    private AnchorPane fxmlAnPaneRight;
    @FXML
    private Label fxmlScore;
    @FXML
    private Button fxmlBtnStart;
    @FXML
    private Button fxmlButtonMusic;
    @FXML
    private Label fxmlLevel;
    
   
    
    //Representation constants
    private static final int ACTUAL_PIECE = -2;
    private static final int BORDER = -1;
    private static final int EMPTY = 0;
    //**************************************************************************
    
    private static final int MAX_PIECE_LENGTH = 4; 
    private static final int BLOCKSIZE = 30;
    
    private final GridPane rightGrid = new GridPane();
    private final GridPane centerGrid = new GridPane();
    private final int[][] backgroundArray = new int[27 + MAX_PIECE_LENGTH][15]; // (27 + 4) * 15
    
    private static int defaultTimerPeriod = 500;
    private final Timer renderer = new Timer(defaultTimerPeriod);
    
    private Pair<Integer, Integer> piecePosition;
    private Piece actualPiece;
    private int actualCombination = 0;
    private final Map<Integer, Color> colorMap = new HashMap<>();
    private final Map<Integer, Double> pointsMap = new HashMap<>();
    private final List<Piece> pieces = new ArrayList<>();
    private final Random rd = new Random();
    private boolean musicOn = true;
    private int nextPieceId;
    private MediaPlayer mediaPlayer;
    private List<Piece> extraPieces = new ArrayList();  
    @FXML
    private CheckBox fxmlExtraPieces;
    private Stage endScreen; 
    private EndgamePaneController endGameController; 

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Load the End-Game Screen and initialize it 
        FXMLLoader endScreenLoader = new FXMLLoader();
        endScreenLoader.setLocation(getClass().getResource("/fxml/EndgamePane.fxml"));
        try {
            endScreen = new Stage();
            endScreen.setScene(new Scene((AnchorPane) endScreenLoader.load()));
            endGameController = endScreenLoader.getController();
            endGameController.initialize(null, null);
           
        } catch (IOException io) {
            System.out.println(io.getClass() + " " + io.getMessage());
        }
        endScreen.getIcons().add(MainApp.APP_ICON);
        
        
        //Create extra-game pieces
        createExtraPieces();
        
        //Initialize the game-gridpane 
        centerGrid.setPrefHeight((backgroundArray.length - MAX_PIECE_LENGTH) * BLOCKSIZE);
        centerGrid.setPrefWidth(backgroundArray[0].length * BLOCKSIZE);
        centerGrid.setHgap(0);
        centerGrid.setVgap(0);
        centerGrid.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        //Create the refresh-Daemon
        setUpBackgroundThread(); 

        //Set up the music played in the background
        setUpMedia(); 
      
        //Resets the background-array
        refreshBackground();
        
        //Draws the board (empty- and border-blocks )
        drawGameBoard();
        
        //Defines the function, that makes the game work
        setUpTimer(); 
        
        //Initialize other elements
        fxmlAnPaneRight.getChildren().add(rightGrid);
        AnchorPane.setLeftAnchor(rightGrid, 50.0);
        AnchorPane.setTopAnchor(rightGrid, 100.0);
        fxmlButtonMusic.setText("Music " + "✓");
        fxmlLevel.setText("1"); 
    }

    private void undrawPiece(int row, int col) {
        for (int i = row - 1; i < row + 5; i++) {
            for (int j = col - 1; j < col + 5; j++) {
                //Check Indizes, so no NullPointerException occurs
                if (checkIndizes(i, j) && backgroundArray[i][j] == -2) {
                    backgroundArray[i][j] = EMPTY;
                }
            }
        }
    }

    private void createNewPiece() {
        actualCombination = 0;
        actualPiece = pieces.get(nextPieceId);
        int nextId;
        //Choose a next Id, but it should be != the actual Id
        while ((nextId = pieces.indexOf(pieces.get(rd.nextInt(pieces.size())))) == nextPieceId);
        nextPieceId = nextId;
        //draw the next Piece in the right corner of the application 
        drawNextPiece();
        //Set the position of the new piece 
        piecePosition = new Pair<>(0, backgroundArray[0].length / 2);

    }

    private void drawNextPiece() {
        //Draws the next piece in the right corner of the applcation in a gridPane
        rightGrid.getChildren().clear();
        Integer[] combination = pieces.get(nextPieceId).getCombinations().get(0);
        int rowIndex = 0, colIndex = 0;
        Rectangle part;
        for (int i = 0; i < combination.length; i++) {
            if (i % MAX_PIECE_LENGTH == 0 && i != 0) {
                rowIndex++;
                colIndex = 0;
            }
            colIndex = (i % MAX_PIECE_LENGTH);
            if (combination[i] == 1) {
                part = new Rectangle(120 / MAX_PIECE_LENGTH, 120 / MAX_PIECE_LENGTH);
                part.setFill(colorMap.get(pieces.get(nextPieceId).getId()));
                part.setStrokeType(StrokeType.INSIDE);
                part.setStroke(Color.LIGHTGRAY);
                rightGrid.add(part, colIndex, rowIndex);
            }
        }

    }

    private boolean checkIndizes(int row, int col) {
        //Checks if the indizes are valid (! col is allowed to be 0 as the position of the actual piece 
        //(piecePosition) can be "in the wall" while the piece is actually in a valid spot)
        return !(row < 1 || row >= backgroundArray.length - 1
                || col < 0 || col >= backgroundArray[0].length - 1);
    }

    private void drawPiece(int row, int col) {
        //Check if drwing is possible 
        if (!checkIndizes(row, col)) {
            return;
        }

        Integer[] combination = actualPiece.getCombinations().get(actualCombination);
        int rowIndex = row, colIndex = col;
        Rectangle part;
        for (int i = 0; i < combination.length; i++) {
            if (i % MAX_PIECE_LENGTH == 0 && i != 0) {
                rowIndex++;
                colIndex = col;
            }
            colIndex = col + (i % MAX_PIECE_LENGTH);
            if (combination[i] == 1) {
                //Set the background-Array at the specified position to ACTUAL_PIECE  
                backgroundArray[rowIndex][colIndex] = ACTUAL_PIECE;
            }
        }

    }

    private void drawGameBoard() {
        //Draw the gameboard, paint the rectangles at any position according to the 
        //correspounding value
        centerGrid.getChildren().clear();
        for (int i = MAX_PIECE_LENGTH; i < backgroundArray.length; i++) {
            for (int j = 0; j < backgroundArray[i].length; j++) {
                Rectangle part = new Rectangle(BLOCKSIZE, BLOCKSIZE);
                switch (backgroundArray[i][j]) {
                    case EMPTY:
                        part.setFill(Color.rgb(0, 0, 0));
                        break;
                    case BORDER:
                        part.setFill(Color.DARKGRAY);
                        part.setStrokeType(StrokeType.INSIDE);
                        part.setStroke(Color.GRAY);
                        break;
                    case ACTUAL_PIECE:
                        part.setFill(actualPiece.getBlockColor());
                        part.setStrokeType(StrokeType.INSIDE);
                        part.setStroke(Color.LIGHTGRAY);
                        break;
                    default:
                        //The already fixed blocks (pieces that are already fixed)
                        part.setFill(colorMap.get(backgroundArray[i][j]));
                        part.setStrokeType(StrokeType.INSIDE);
                        part.setStroke(Color.LIGHTGRAY);
                }
                centerGrid.add(part, j, i);
            }
            fxmlRoot.setCenter(centerGrid);
        }
    }

    private void setUpKeyListeners() {
        fxmlRoot.getScene().setOnKeyPressed((KeyEvent keyEvent) -> {
            KeyCode keyCode = keyEvent.getCode();
            if (keyCode != KeyCode.P && !renderer.isNotification()) {
                return;
            }
            switch (keyCode) {
                case UP:
                    int last = actualCombination;
                    actualCombination = (actualCombination + 1) % actualPiece.getCombinations().size();
                    if (checkTranformation(piecePosition.getKey(), piecePosition.getValue())) {
                        undrawPiece(piecePosition.getKey(), piecePosition.getValue());
                        drawPiece(piecePosition.getKey(), piecePosition.getValue());
                        drawGameBoard();
                    } else {
                        actualCombination = last;
                    }
                    fxmlBtnStart.requestFocus();
                    break;
                case DOWN:
                    int millies = renderer.getTimeToSleep();
                    renderer.setTimeToSleep(
                            millies / 2 >= (defaultTimerPeriod / 10) ? millies / 2 : defaultTimerPeriod / 10);
                    break;
                case RIGHT:
                    if (checkNextStep(piecePosition.getKey(), piecePosition.getValue(), 1, 0, 0)) {
                        undrawPiece(piecePosition.getKey(), piecePosition.getValue());
                        piecePosition = new Pair<>(piecePosition.getKey(), piecePosition.getValue() + 1);
                        drawPiece(piecePosition.getKey(), piecePosition.getValue());
                        drawGameBoard();
                    }
                    break;
                case LEFT:
                    if (checkNextStep(piecePosition.getKey(), piecePosition.getValue(), 0, -1, 0)) {
                        undrawPiece(piecePosition.getKey(), piecePosition.getValue());
                        piecePosition = new Pair<>(piecePosition.getKey(), piecePosition.getValue() - 1);
                        drawPiece(piecePosition.getKey(), piecePosition.getValue());
                        drawGameBoard();
                    }
                    break;
                case P:
                    renderer.setNotification(!renderer.isNotification());
                    break;
            }
        });
        fxmlRoot.getScene().setOnKeyReleased((KeyEvent keyEvent) -> {
            KeyCode keyCode = keyEvent.getCode();
            switch (keyCode) {
                case DOWN:
                    renderer.setTimeToSleep(defaultTimerPeriod);
                    break;

            }
        });

    }

    @FXML
    private void onActionStartGame(ActionEvent event) {
        createPieces();
        if(fxmlExtraPieces.isSelected()){
            pieces.addAll(extraPieces);
        } 
 
        for (int i = 0; i < pieces.size(); i++) {
            colorMap.put(pieces.get(i).getId(), pieces.get(i).getBlockColor());
            pointsMap.put(pieces.get(i).getId(), pieces.get(i).getCost());
        }
        setUpKeyListeners();
        refreshBackground();
        nextPieceId = rd.nextInt(pieces.size());
        createNewPiece();
        fxmlScore.setText("0");
        defaultTimerPeriod = 500;
        renderer.setTimeToSleep(defaultTimerPeriod);
        renderer.setNotification(true);
    }

    private void createPieces() {
        pieces.clear();
        int id = EMPTY + 1;
        Piece newPiece;
        newPiece = new Piece(id++, Color.RED, 7.00);
        newPiece.addCombination(new Integer[]{
            1, 1, 0, 0,
            1, 1, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        });
        pieces.add(newPiece);
        //**********************************************************************
        newPiece = new Piece(id++, Color.rgb(255, 128, 0), 10.00);
        newPiece.addCombination(new Integer[]{
            1, 1, 0, 0,
            0, 1, 0, 0,
            0, 1, 0, 0,
            0, 0, 0, 0
        });
        newPiece.addCombination(new Integer[]{
            0, 0, 0, 0,
            1, 1, 1, 0,
            1, 0, 0, 0,
            0, 0, 0, 0
        });
        newPiece.addCombination(new Integer[]{
            0, 1, 0, 0,
            0, 1, 0, 0,
            0, 1, 1, 0,
            0, 0, 0, 0
        });
        newPiece.addCombination(new Integer[]{
            0, 0, 1, 0,
            1, 1, 1, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        });
        pieces.add(newPiece);
        //**********************************************************************
        newPiece = new Piece(id++, Color.rgb(255, 255, 0), 10.00);
        newPiece.addCombination(new Integer[]{
            0, 1, 1, 0,
            0, 1, 0, 0,
            0, 1, 0, 0,
            0, 0, 0, 0
        });
        newPiece.addCombination(new Integer[]{
            1, 0, 0, 0,
            1, 1, 1, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,});
        newPiece.addCombination(new Integer[]{
            0, 1, 0, 0,
            0, 1, 0, 0,
            1, 1, 0, 0,
            0, 0, 0, 0,});
        newPiece.addCombination(new Integer[]{
            0, 0, 0, 0,
            1, 1, 1, 0,
            0, 0, 1, 0,
            0, 0, 0, 0,});
        pieces.add(newPiece);
        //**********************************************************************
        newPiece = new Piece(id++, Color.rgb(128, 255, 0), 12.50);
        newPiece.addCombination(new Integer[]{
            0, 1, 1, 0,
            1, 1, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        });
        newPiece.addCombination(new Integer[]{
            0, 1, 0, 0,
            0, 1, 1, 0,
            0, 0, 1, 0,
            0, 0, 0, 0
        });
        pieces.add(newPiece);
        //**********************************************************************
        newPiece = new Piece(id++, Color.rgb(0, 128, 255), 12.50);
        newPiece.addCombination(new Integer[]{
            1, 1, 0, 0,
            0, 1, 1, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        });
        newPiece.addCombination(new Integer[]{
            0, 1, 0, 0,
            1, 1, 0, 0,
            1, 0, 0, 0,
            0, 0, 0, 0
        });
        pieces.add(newPiece);
        //**********************************************************************
        newPiece = new Piece(id++, Color.rgb(0, 0, 204), 5.00);
        newPiece.addCombination(new Integer[]{
            1, 0, 0, 0,
            1, 0, 0, 0,
            1, 0, 0, 0,
            1, 0, 0, 0
        });
        newPiece.addCombination(new Integer[]{
            1, 1, 1, 1,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        });
        pieces.add(newPiece);
        //**********************************************************************
        newPiece = new Piece(id++, Color.rgb(127, 0, 255), 9.00);
        newPiece.addCombination(new Integer[]{
            0, 1, 0, 0,
            1, 1, 1, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        });
        newPiece.addCombination(new Integer[]{
            0, 1, 0, 0,
            1, 1, 0, 0,
            0, 1, 0, 0,
            0, 0, 0, 0,});
        newPiece.addCombination(new Integer[]{
            0, 0, 0, 0,
            1, 1, 1, 0,
            0, 1, 0, 0,
            0, 0, 0, 0
        });
        newPiece.addCombination(new Integer[]{
            0, 1, 0, 0,
            0, 1, 1, 0,
            0, 1, 0, 0,
            0, 0, 0, 0
        });
        pieces.add(newPiece);
        //**********************************************************************
//        Piece newPiece = new Piece(id++, Color.rgb(127, 0, 255), 15.00);
//        newPiece.addCombination(new Integer[]{
//           1, 0, 1, 1, 1,
//           1, 0, 1, 0, 0,
//           1, 1, 1, 1, 1, 
//           0, 0, 1, 0, 1,
//           1, 1, 1, 0, 1,
//        });
//        pieces.add(newPiece); 
//        newPiece = new Piece(id++, Color.rgb(127, 0, 128), 10.00);
//        newPiece.addCombination(new Integer[]{
//           1, 1, 1, 0, 1,
//           0, 0, 1, 0, 1,
//           1, 1, 1, 1, 1, 
//           1, 0, 1, 0, 0,
//           1, 0, 1, 1, 1,
//        });
//        pieces.add(newPiece); 


    }

    private boolean checkNextStep(int row, int col, int right, int left, int down) {
        int rowIndex = row;
        int colIndex = col;
        Integer[] combinations = actualPiece.getCombinations().get(actualCombination);
        for (int i = 0; i < combinations.length; i++) {
            if (i % MAX_PIECE_LENGTH == 0 && i != 0) {
                rowIndex++;
                colIndex = col;
            }
            colIndex = col + (i % MAX_PIECE_LENGTH);
            if (combinations[i] == 1) {
                if (backgroundArray[rowIndex + down][colIndex + right + left] != EMPTY && backgroundArray[rowIndex + down][colIndex + right + left] != ACTUAL_PIECE) {
                    return false;
                }
            }
        }
        return true;
    }

    private void createExtraPieces(){
        //ID always larger than the number of normal pieces
        int id = 10;  
        Piece newPiece;
        newPiece = new Piece(id++, Color.WHITE, 15.00);
        newPiece.addCombination(new Integer[]{
            0, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        });
        extraPieces.add(newPiece); 
        newPiece = new Piece(id++, Color.AQUA, 6.00);
        newPiece.addCombination(new Integer[]{
            0, 0, 0, 0,
            0, 1, 0, 0,
            0, 1, 0, 0,
            0, 0, 0, 0
        });
        newPiece.addCombination(new Integer[]{
            0, 0, 0, 0,
            0, 1, 1, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        });
        extraPieces.add(newPiece);
    }
    
    private boolean checkRow(int[] row) {
        for (int i = 0; i < row.length; i++) {
            if (row[i] == EMPTY) {
                return false;
            }
        }
        return true;
    }

    private void transformPiece(int row, int col) {
        int rowIndex = row;
        int colIndex = col;
        Integer[] combinations = actualPiece.getCombinations().get(actualCombination);
        for (int i = 0; i < combinations.length; i++) {
            if (i % MAX_PIECE_LENGTH == 0 && i != 0) {
                rowIndex++;
                colIndex = col;
            }
            colIndex = col + (i % MAX_PIECE_LENGTH);
            if (combinations[i] == 1) {
                backgroundArray[rowIndex][colIndex] = actualPiece.getId();
            }
        }
    }

    private Pair<Integer, Double> checkForDeletedRows(Integer row) {
        int rowIndex;
        Pair<Integer, Double> result = new Pair<>(0, 0.0); 
        int multiplicator = 0; 
        double points; 
        //Check a total of MAX_PIECE_LENGTH rows, as this is the maximum amount of rows that can be delted in one turn  
        for (int i = 0; i < MAX_PIECE_LENGTH; i++) {
            rowIndex = row + i;
            //check if (viable) rows can be deleted 
            if (rowIndex <= backgroundArray.length - 2 && checkRow(backgroundArray[rowIndex])) {
                points = result.getValue() + getPoints(rowIndex);   //sum up the points gathered  
                //foreach deleted row, copy the ones above (all until those become invisible)
                for (int j = rowIndex; j >= MAX_PIECE_LENGTH; j--) {
                    backgroundArray[j] = backgroundArray[j - 1];
                }
                //Create a new int-array for the (invisible) row with the index MAX_PIECE_LENGTH - 1 (<= the last one that cannot be seen)
                backgroundArray[MAX_PIECE_LENGTH - 1] = new int[15];
                //Add borders, so the row can be copied, if a row below is deleted 
                backgroundArray[MAX_PIECE_LENGTH - 1][0] = backgroundArray[MAX_PIECE_LENGTH - 1][14] = BORDER;
                //Update the multiplicator
                result = new Pair<>(result.getKey() + 1, points); 
            }
        }
        return result;
    }

    private boolean checkTranformation(int row, int col) {
        int rowIndex = row;
        int colIndex = col;
        Integer[] combinations = actualPiece.getCombinations().get(actualCombination);
        for (int i = 0; i < combinations.length; i++) {
            if (i % MAX_PIECE_LENGTH == 0 && i != 0) {
                rowIndex++;
                colIndex = col;
            }
            colIndex = col + (i % MAX_PIECE_LENGTH);
            if (combinations[i] == 1) {
                if (backgroundArray[rowIndex][colIndex] != EMPTY && backgroundArray[rowIndex][colIndex] != ACTUAL_PIECE) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getTimerRefreshDuration(int score) {
        int time = 0, result = 0;
        int level = score / 500;
        fxmlLevel.setText(level + 1 + ""); 
        switch (level) {
            case 6:
                result += 25;
            case 5:
                result += 25;
            case 4:
                result += 50;
            case 3:
                result += 50;
            case 2:
                result += 100;
            case 1:
                result += 100;
            case 0:
                break;
            default:
                result = 375;
        }
        return 500 - result;
    }

    private void refreshBackground() {
        for (int i = 0; i < backgroundArray.length; i++) {
            Arrays.fill(backgroundArray[i], 0);
        }

        for (int i = 0; i < backgroundArray[0].length; i++) {
            backgroundArray[backgroundArray.length - 1][i] = BORDER;
        }
        for (int i = MAX_PIECE_LENGTH - 1; i < backgroundArray.length; i++) {
            backgroundArray[i][0] = BORDER;
            backgroundArray[i][backgroundArray[i].length - 1] = BORDER;
        }
    }

    @FXML
    private void handleMusic(ActionEvent event) {
        //flip the actual music configuration and edit the text of the button as needed
        musicOn = !musicOn;
        if (musicOn) {
            mediaPlayer.play();
            fxmlButtonMusic.setText("Music " + "✓");
        } else {
            mediaPlayer.pause();
            fxmlButtonMusic.setText("Music " + "✗");
        }
    }

    private void setUpBackgroundThread() {
        Thread t = new Thread(renderer);
        t.setDaemon(true);
        t.start();
        renderer.setNotification(false);
    }

    private void setUpMedia() {
          try {
            Media media = new Media(new MainApp().getClass().getResource("/music/Tetris.mp3").toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            });
            mediaPlayer.play();
        } catch (URISyntaxException uri) {
            //Music cannot be played
            //This problem should never occour, if the jar-file is built properly
        }

    }

    private void setUpTimer() {
        renderer.valueProperty().addListener((l, oldV, newV) -> {
            if (!renderer.isNotification()) {
                //if the nortifications are set to false, the game should not be changed (for example when the pause-button is clicked)
                return;
            }
            //Check is the actual piece can move down 
            if (checkNextStep(piecePosition.getKey(), piecePosition.getValue(), 0, 0, 1)) {
                //Remove all the elements in the background-Array with the value ACTUAL_PIECE
                undrawPiece(piecePosition.getKey(), piecePosition.getValue());
                //Next position
                piecePosition = new Pair<>(piecePosition.getKey() + 1, piecePosition.getValue());
                //Draw next piece, set the representing blocks to ACTUAL_PIECE 
                drawPiece(piecePosition.getKey(), piecePosition.getValue());
            } else {
                //Piece cannot move down, so it is fixed now 
                Pair<Integer, Double> points; 
                //Check if the game is over 
                if (renderer.isNotification() && isGameOver()){
                    //Undo the listeners and show the user a game-over screen with hos points
                    renderer.setNotification(false);
                    fxmlRoot.getScene().setOnKeyPressed(null);
                    fxmlRoot.getScene().setOnKeyReleased(null);
                    showEndGameScreen();
                    return;
                }
                //Game is not over, the piece is now fixed somewhere and does not result in a lost game 
                //Transform from ACTUAL_PIECE to Id (of the piece)
                transformPiece(piecePosition.getKey(), piecePosition.getValue());
                //Check if some rows can be deleted (max MAX_PIECE_LENGTH)
                points = checkForDeletedRows(piecePosition.getKey());
                int multiplicator = points.getKey(); 
                if (multiplicator != 0) {
                    int score = (int) (Integer.parseInt(fxmlScore.getText()) + 
                                      (points.getValue() * (1 + (1.0/3.0) * (multiplicator - 1) ) ));
                    fxmlScore.setText(score + "");
                    //Set the defaultTimerPeriod, might be decreased by the number of achieved points so far
                    defaultTimerPeriod = getTimerRefreshDuration(score);
                    renderer.setTimeToSleep(defaultTimerPeriod);
                }
                //Create a new piece and draw it 
                createNewPiece();
                drawPiece(piecePosition.getKey(), piecePosition.getValue());
            }
            
            drawGameBoard();
        });
    }

    private boolean isGameOver() {
        boolean result = true; 
        //Check the first (visible) row, if all possible blocks are EMPTY
        for (int i = 1; i < backgroundArray[MAX_PIECE_LENGTH].length - 1; i++) {
            result = result && (backgroundArray[MAX_PIECE_LENGTH][i] == EMPTY); 
        }
        return !result; 
    }

    private double getPoints(int rowIndex) {
       double points = 0; 
        for (int i = 1; i < backgroundArray[rowIndex].length - 1; i++) {
            points += pointsMap.get(backgroundArray[rowIndex][i]); 
        }
        return points; 
    }
    
    private void showEndGameScreen(){
        endGameController.setScore(Integer.parseInt(fxmlScore.getText()));
        endGameController.updatePoints();
        endScreen.show();
    }
}