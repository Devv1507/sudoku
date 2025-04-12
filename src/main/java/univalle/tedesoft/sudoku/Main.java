package univalle.tedesoft.sudoku;

import univalle.tedesoft.sudoku.views.GameView;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import univalle.tedesoft.sudoku.views.WelcomeView;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        WelcomeView welcomeView = new WelcomeView();
        welcomeView.show();
    }
    public static void main(String[] args) {
        launch();
    }
}