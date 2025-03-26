package univalle.tedesoft.sudoku.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import univalle.tedesoft.sudoku.models.IRectangles;
import univalle.tedesoft.sudoku.models.Rectangles;

public class GameController {
    @FXML
    private Button clueButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button initButton;

    @FXML
    private Button restartButton;

    @FXML
    private GridPane sudokuGridPane;

    private  IRectangles rectangles;

    @FXML
    public void initialize() {
        rectangles = new Rectangles();
        rectangles.createTextFieldsAndLabelsInGrid(sudokuGridPane, 6);


    }
}
