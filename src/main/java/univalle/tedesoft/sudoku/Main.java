package univalle.tedesoft.sudoku;

import univalle.tedesoft.sudoku.views.GameView;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        GameView gameView = GameView.getInstance();
        gameView.show();
    }
    public static void main(String[] args) {
        launch();
    }
}