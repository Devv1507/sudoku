package univalle.tedesoft.sudoku.views;

import univalle.tedesoft.sudoku.Main;
import univalle.tedesoft.sudoku.controllers.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GameView extends Stage {
    private final GameController controller;

    public GameView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("sudoku-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());
        this.controller = fxmlLoader.getController();
        this.setTitle("6x6 Sudoku Game");
        this.setScene(scene);
    }

    public GameController getController() {
        return controller;
    }

    public static GameView getInstance() throws IOException {
        if (GameViewHolder.INSTANCE == null) {
            GameViewHolder.INSTANCE = new GameView();
            return GameViewHolder.INSTANCE;
        } else {
            return GameViewHolder.INSTANCE;
        }
    }

    private static class GameViewHolder {
        private static GameView INSTANCE;
    }
}