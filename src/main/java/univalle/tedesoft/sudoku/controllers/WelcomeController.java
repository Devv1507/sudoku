package univalle.tedesoft.sudoku.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import univalle.tedesoft.sudoku.views.GameView;

import java.io.IOException;

public class WelcomeController {

    @FXML
    public void onActionStartButton(ActionEvent actionEvent) throws IOException {
        GameView gameView = GameView.getInstance();
        gameView.show();
    }
    @FXML
    void onInstruction(ActionEvent event) {

    }

}
