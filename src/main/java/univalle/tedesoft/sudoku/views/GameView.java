package univalle.tedesoft.sudoku.views;

import univalle.tedesoft.sudoku.Main;
import univalle.tedesoft.sudoku.controllers.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Representa la ventana principal del juego Sudoku.
 * Carga la interfaz de usuario desde un archivo FXML y la muestra.
 * Implementa el patrón Singleton para asegurar una única instancia de la ventana.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 */
public class GameView extends Stage {
    private final GameController controller;

    /**
     * Constructor privado para implementar el patrón Singleton.
     * Carga el archivo FXML, obtiene el controlador y configura la escena y el título.
     * @throws IOException Si no se puede cargar el archivo FXML.
     */
    private GameView() throws IOException {
        URL fxmlUrl = Main.class.getResource("sudoku-view.fxml");
        if (fxmlUrl == null) {
            throw new IOException("No se pudo encontrar el archivo FXML: sudoku-view.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(fxmlLoader.load());
        this.controller = fxmlLoader.getController();
        this.setTitle("6x6 Sudoku Game");
        this.setScene(scene);
        this.setResizable(false);
    }

    /**
     * Obtiene el controlador asociado a esta vista.
     * @return La instancia de GameController.
     */
    public GameController getController() {
        return this.controller;
    }

    /**
     * Obtiene la instancia única de GameView, siguendo un patrón Singleton.
     * Si no existe, la crea. Esto asegura que solo haya una ventana de juego abierta a la vez.
     * @return La instancia única de GameView.
     * @throws IOException Si ocurre un error durante la creación de la primera instancia.
     */
    public static GameView getInstance() throws IOException {
        return this.GameViewHolder.INSTANCE;
    }

    /**
     * Clase interna estática para contener la instancia Singleton (patrón Bill Pugh).
     * La instancia se crea solo cuando se accede a GameViewHolder.INSTANCE por primera vez.
     */
    private static class GameViewHolder {
        private static final GameView INSTANCE;

        static {
            try {
                INSTANCE = new GameView();
            } catch (IOException error) {
                 // Manejar el error crítico de no poder cargar la UI
                 System.err.println("Error crítico al inicializar GameView: " + error.getMessage());
                 error.printStackTrace();
                 // Podríamos lanzar una RuntimeException para detener la aplicación
                 throw new RuntimeException("No se pudo inicializar la vista del juego.", error);
            }
        }
    }
}