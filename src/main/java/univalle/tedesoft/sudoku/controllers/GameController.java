package univalle.tedesoft.sudoku.controllers;

import javafx.scene.control.Label;
import univalle.tedesoft.sudoku.models.Board;
import univalle.tedesoft.sudoku.models.Cell;
import univalle.tedesoft.sudoku.models.GameState;
import univalle.tedesoft.sudoku.views.GameView;

import java.util.Optional;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

/**
 * Controlador para el juego Sudoku.
 * Conecta la Vista (GameView) con el Modelo (Board, GameState).
 * Maneja la lógica del juego y las acciones del usuario delegadas por la vista.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 */
public class GameController {

    private Board board;
    private GameState gameState;
    private GameView view;

    @FXML private Button cleanButton;
    @FXML private Button clueButton;
    @FXML private Button instructionsButton;
    @FXML private Button restartButton;
    @FXML private GridPane sudokuGridPane;
    @FXML private Label lackedThreeCounter;
    private int pistas = 0;
    private int threeCount = 0;

    /**
     * Inicializa el modelo y configura los manejadores de eventos de los botones.
     * La vista se inicializa por separado y se conecta mediante setView.
     */
    @FXML
    public void initialize() {
        this.board = new Board();
        this.gameState = new GameState(board);

        // Configurar acciones de los botones
        this.restartButton.setOnAction(event -> startNewGame());
        this.cleanButton.setOnAction(event -> clearUserEntries());
        this.clueButton.setOnAction(event -> showClue());
        this.instructionsButton.setOnAction(event -> showHelp());

        if (this.view != null) {
            initializeGameAndRender();
        }
    }

    /**
     * Establece la referencia a la vista asociada a este controlador.
     * @param newView La instancia de GameView
     */
    public void setView(GameView newView) {
        this.view = newView;
        if (this.board != null && this.gameState != null) {
            initializeGameAndRender();
        }
    }

    /**
     * Inicializa el tablero con un puzzle y le pide a la vista que lo renderice.
     */
    private void initializeGameAndRender() {
        this.board.initializeBoard(); // Prepara el modelo
        this.threeCount = this.board.threeCount;
        if (this.view != null) {
            this.view.renderBoard(this.board.getGridSnapshot()); // Pide renderizar
            this.view.highlightErrors(this.gameState.getInvalidCells()); // Pide validar visualmente
            this.view.setGridDisabled(false); // Asegura que la grilla esté activa
        } else {
            System.err.println("Error: Intento de renderizar sin vista establecida.");
        }
    }

    /**
     * Proporciona la referencia al GridPane a la Vista (llamado desde GameView).
     * @return El GridPane del tablero inyectado por FXML.
     */
    public GridPane getSudokuGridPane() {
        return this.sudokuGridPane;
    }

    /**
     * Inicia un puzzle completamente nuevo.
     */
    private void startNewGame() {
        if (this.view == null) return; // No hacer nada si la vista no está lista

        // Solicitar confirmación a la vista
        Optional<ButtonType> result = this.view.showRestartConfirmationDialog();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            this.initializeGameAndRender();
        }
    }

    /**
     * Limpia las entradas del usuario en el tablero actual.
     */
    private void clearUserEntries() {
        if (this.view == null) return;

        // Solicitar confirmación a la vista
        Optional<ButtonType> result = this.view.showClearConfirmationDialog();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("Limpiando entradas del usuario...");
            boolean changed = false;
            for (int row = 0; row < Board.GRID_SIZE; row++) {
                for (int col = 0; col < Board.GRID_SIZE; col++) {
                    // Actualizar el Modelo directamente
                    Cell cell = this.board.getCell(row, col);
                    if (cell.getEditable() && cell.getValue() != 0) {
                        this.board.setCellValue(row, col, 0); // Actualiza el modelo
                        changed = true;
                    }
                }
            }

            if (changed) {
                // Pedir a la Vista que refleje los cambios del modelo y revalide
                this.view.renderBoard(this.board.getGridSnapshot());
                validateAndHighlightBoard();
            }
            this.view.setGridDisabled(false); // Asegurar que la grilla esté activa
            System.out.println("Entradas del usuario limpiadas.");
        }
        this.showThreeLack();
    }

    /**
     * Muestra la ayuda del juego usando un diálogo en la vista.
     */
    private void showHelp() {
        if (this.view == null) {
            System.err.println("Error: Intento de mostrar ayuda sin vista establecida.");
            return;
        }
        this.view.showHelpDialog();
    }

    /**
     * Busca y aplica una pista en el tablero.
     */
    private void showClue() {
        if (this.view == null) return;
        System.out.println("Pista - Buscando sugerencia...");
        boolean clueFound = false;

        int emptyCells = board.countEmptyEditableCells();
        //Si solo hay un espacio vacío deja de dr pistas.
        if (emptyCells <= 1) {
            this.view.showNoMoreCluesDialog();
            return;
        }
        if (this.pistas >= 10) {
            this.view.showMaxCluesReachedDialog();
            return;
        }
        for (int row = 0; row < Board.GRID_SIZE; row++) {
            for (int col = 0; col < Board.GRID_SIZE; col++) {
                Cell cell = board.getCell(row, col);
                // Buscar primera celda editable vacía para la que haya sugerencia

                if (cell.getEditable() && cell.getValue() == 0) {
                    int suggestion = gameState.getClue(row, col);
                    if (suggestion > 0) {
                        System.out.println("Pista: Poner " + suggestion + " en (" + row + "," + col + ")");
                        // Actualizar el Modelo
                        this.board.setCellValue(row, col, suggestion);
                        // Pedir a la Vista que renderice y valide
                        this.view.renderBoard(this.board.getGridSnapshot());
                        this.validateAndHighlightBoard();
                        clueFound = true;
                        pistas++;
                        break; // Salir del bucle interno
                    }
                }
                if (cell.getValue() == 3) {
                    this.threeCount++;
                }
            }
            if (clueFound) break;
        }

        if (!clueFound) {
            this.view.showNoObviousCluesDialog();
        }
    }

    /**
     * Llamado por GameView cuando el texto de un TextField cambia.
     * Actualiza el modelo y desencadena la validación y posible condición de victoria.
     * @param row Fila del cambio.
     * @param col Columna del cambio.
     * @param newValue El nuevo valor como String (puede ser vacío).
     */
    public void cellValueChanged(int row, int col, String newValue) {
        if (this.view == null) return; // Seguridad

        try {
            int value = newValue.isEmpty() ? 0 : Integer.parseInt(newValue);
            Cell cell = board.getCell(row, col); // Obtener celda del modelo

            // Solo procesar si es editable y el valor realmente cambió
            if (cell.getEditable() && cell.getValue() != value) {
                boolean updated = board.setCellValue(row, col, value);

                if (updated) {
                    this.validateAndHighlightBoard();
                    // comprobar si se ha ganado el juego
                    this.checkWinCondition();
                }
            }
        } catch (NumberFormatException e) {
            // TODO:  no debería ocurrir debido al TextFormatter de la vista, pero por robustez:
            System.err.println("Error de formato numérico inesperado: " + newValue);
            Cell cell = board.getCell(row, col);
            if(cell.getEditable() && cell.getValue() != 0) {
                board.setCellValue(row, col, 0);
                validateAndHighlightBoard(); // Revalidar
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Intento de valor ilegal en ("+row+","+col+"): " + newValue + " - " + e.getMessage());
        }
    }

    // --- Lógica Interna del Controlador ---

    /**
     * Consulta el estado de validez del tablero y le pide a la vista que actualice
     * el resaltado de las celdas erróneas.
     */
    private void validateAndHighlightBoard() {
        if (this.view == null) return;
        Set<Pair<Integer, Integer>> invalidCells = gameState.getInvalidCells();
        this.view.highlightErrors(invalidCells); // Ordena a la vista resaltar
    }

    /**
     * Comprueba si el juego ha sido ganado y, si es así, notifica al usuario
     * a través de la vista y deshabilita la interacción con el tablero.
     */
    private void checkWinCondition() {
        if (this.view == null) return;
        if (gameState.isGameWon()) {
            System.out.println("¡Juego ganado!");
            this.view.showWinDialog();
            this.view.setGridDisabled(true); // Ordena a la vista deshabilitar la grilla
        }
    }

    /**
     * Permite a la vista consultar si una celda es editable (necesario para restoreBaseStyle).
     * @param row Fila.
     * @param col Columna.
     * @return true si la celda en el modelo es editable, false en caso contrario.
     */
    public boolean isCellEditable(int row, int col) {
        if (board != null && row >= 0 && row < Board.GRID_SIZE && col >= 0 && col < Board.GRID_SIZE) {
            return board.getCell(row, col).getEditable();
        }
        return false; // Valor por defecto o lanzar excepción si se prefiere
    }

    /**
     * Metodo para contar cuantos 3 faltan
     */
    public void showThreeLack() {
//        int lackedThrees = 6 - this.threeCount;
        if (this.view != null) return;

        int lackedThrees = this.view.getThreeCountLacked();

        this.lackedThreeCounter.setText(String.valueOf(lackedThrees));
//        return lackedThrees;
    }
}