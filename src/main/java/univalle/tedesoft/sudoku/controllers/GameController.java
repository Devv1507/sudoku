package univalle.tedesoft.sudoku.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import univalle.tedesoft.sudoku.models.Board;
import univalle.tedesoft.sudoku.models.GameState;

import java.util.Optional;

/**
 * Controlador para la vista principal del juego Sudoku.
 * Maneja la interacción del usuario con el tablero y los botones de control.
 * Implementa la creación dinámica de campos de texto y la validación del tablero.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 */
public class GameController {
    /**
     * Tamaño del tablero de Sudoku.
     * Este valor es constante y se utiliza para definir el tamaño de la cuadrícula.
     * En este caso, es 6 para un tablero 6x6.
     */
    private static final int GRID_SIZE = Board.GRID_SIZE;

    /**
     * Botón para pedir una pista al juego.
     * @see Button
     * TO-DO: Implementar la funcionalidad de pistas.
     */
    @FXML
    private Button clueButton;

    /**
     * Botón para mostrar ayuda.
     * @see Button
     * TO-DO: Implementar la funcionalidad de ayuda.
     */
    @FXML
    private Button helpButton;

    /**
     * Botón para iniciar un nuevo juego.
     * @see Button
     * Este botón reinicia el juego actual y genera un nuevo tablero.
     */
    @FXML
    private Button initButton;

    /**
     * Botón para reiniciar el juego actual.
     * @see Button
     * TO-DO: Implementar la funcionalidad de reinicio.
     */
    @FXML
    private Button restartButton;

    /**
     * El GridPane que contiene las celdas del Sudoku.
     * @see GridPane
     */
    @FXML
    private GridPane sudokuGridPane;
    /**
     * El modelo del tablero de Sudoku.
     * @see Board
     */
    private Board board;
    /**
     * El modelo para validar el estado del juego
     * @see GameState
     */
    private GameState gameState;
    /**
     * Campo de texto actualmente editado.
     * Este campo se utiliza para manejar la entrada del usuario en las celdas editables.
     * @see TextField
     */
    private TextField currentEditingTextField = null;

    /**
     * Inicialización llamado automáticamente después de cargar el FXML.
     * Configura el tablero inicial y los manejadores de eventos.
     */
    @FXML
    public void initialize() {
        this.board = new Board();
        this.gameState = new GameState(board);

        // TODO: definir como configurar manejador de clics para el GridPane

        // Configurar acciones de botones
        this.initButton.setOnAction(event -> startNewGame());
        // restartButton.setOnAction(event -> restartCurrentGame()); 
        // helpButton.setOnAction(event -> showHelp()); 
        // clueButton.setOnAction(event -> showClue()); 

        this.startNewGame();
    }

    /**
     * Inicia un nuevo juego de Sudoku.
     * Muestra una confirmación al usuario, genera un nuevo tablero,
     * y actualiza la interfaz gráfica.
     */
    private void startNewGame() {
        // Confirmación antes de iniciar nuevo juego si ya hay uno en curso
        // TODO: esto aún esta construyendose
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Iniciar un nuevo juego borrará el progreso actual. ¿Continuar?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Genera la lógica del tablero (números iniciales, celdas fijas)
            this.board.initializeBoard();
        }
    }
}
