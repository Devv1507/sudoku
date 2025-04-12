package univalle.tedesoft.sudoku.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import univalle.tedesoft.sudoku.Main;

import java.io.IOException;

public class WelcomeView extends Stage {

    public WelcomeView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("welcome-view.fxml")
        );
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 608, 400);
        this.setTitle("Sudoku 6x6 - Welcome!");
        this.setScene(scene);
    }

    public static WelcomeView getInstance() throws IOException {
        if (WelcomeViewHolder.INSTANCE == null) {
            WelcomeViewHolder.INSTANCE = new WelcomeView();
            return WelcomeViewHolder.INSTANCE;
        } else {
            return WelcomeViewHolder.INSTANCE;
        }
    }

    private static class WelcomeViewHolder {
        private static WelcomeView INSTANCE;
    }

}
