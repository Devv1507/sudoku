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
 * Controlador (en patrón MVC) para el juego Sudoku.
 * Conecta la Vista (GameView) con el Modelo (Board, GameState).
 * Maneja la lógica del juego y las acciones del usuario delegadas por la vista.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @version 1.3 (Refactorizado MVC)
 */
public class GameController {

    private Board board;
    private GameState gameState;
    private GameView view;

    @FXML private Button clueButton;
    @FXML private Button helpButton;
    @FXML private Button restartButton;
    @FXML private Button cleanButton;
    @FXML private GridPane sudokuGridPane;
    private int pistas = 0;

    /**
     * Inicializa el modelo y configura los manejadores de eventos de los botones.
     * La vista se inicializa por separado y se conecta mediante setView.
     */
    @FXML
    public void initialize() {
        // Crear instancias del modelo
        this.board = new Board();
        this.gameState = new GameState(board);

        // Configurar acciones de los botones
        this.restartButton.setOnAction(event -> startNewGame());
        this.cleanButton.setOnAction(event -> clearUserEntries());
        this.helpButton.setOnAction(event -> showClue());
        this.clueButton.setOnAction(event -> showHelp());

        if (this.view != null) {
            initializeGameAndRender();
        }
    }

    /**
     * Establece la referencia a la vista asociada a este controlador.
     * @param view La instancia de GameView.
     */
    public void setView(GameView view) {
        this.view = view;
        if (this.board != null && this.gameState != null) {
            initializeGameAndRender();
        }
    }

    /**
     * Inicializa el tablero con un puzzle y le pide a la vista que lo renderice.
     */
    private void initializeGameAndRender() {
        this.board.initializeBoard(); // Prepara el modelo
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
        if (view == null) return; // No hacer nada si la vista no está lista

        Optional<ButtonType> result = view.showDialog(Alert.AlertType.CONFIRMATION,
                "Confirmar Reinicio", "Nuevo Juego",
                "Esto generará un tablero de Sudoku completamente nuevo, perdiendo el progreso actual. ¿Continuar?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("Nuevo juego iniciado.");
            initializeGameAndRender(); // Re-inicializa modelo y pide renderizado/validación a la vista
        }
    }

    /**
     * Limpia las entradas del usuario en el tablero actual.
     */
    private void clearUserEntries() {
        if (view == null) return;

        Optional<ButtonType> result = view.showDialog(Alert.AlertType.CONFIRMATION,
                "Confirmar Limpieza", "Limpiar Entradas",
                "¿Seguro que deseas borrar todos los números que has ingresado en este tablero?");

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
                this.view.highlightErrors(this.gameState.getInvalidCells());
            }
            this.view.setGridDisabled(false); // Asegurar que la grilla esté activa
            System.out.println("Entradas del usuario limpiadas.");
        }
    }

    /**
     * Muestra la ayuda del juego usando un diálogo en la vista.
     */
    private void showHelp() {
        if (view == null) return;
        System.out.println("Mostrando Ayuda...");
        view.showDialog(Alert.AlertType.INFORMATION, "Ayuda Sudoku 6x6", "Reglas del Juego",
                // Contenido del mensaje (sin cambios)
                """
               Completa la cuadrícula de 6x6 con números del 1 al 6.
               - Cada fila debe contener todos los números del 1 al 6 sin repetición.
               - Cada columna debe contener todos los números del 1 al 6 sin repetición.
               - Cada bloque de 2x3 debe contener todos los números del 1 al 6 sin repetición.
               Haz clic en una celda vacía para ingresar un número. Las celdas con números en negrita son fijas.
               Usa las teclas DELETE o BACKSPACE para borrar un número ingresado.

               Botones:
               - Reiniciar: Inicia un puzzle de Sudoku completamente nuevo.
               - Limpiar: Borra todos los números ingresados por el usuario en el puzzle actual.
               - Ayuda: Muestra una pista (si es posible).
               - ?: Muestra esta ventana.
               """
        );
    }

    /**
     * Busca y aplica una pista en el tablero.
     */
    private void showClue() {
        if (view == null) return;
        System.out.println("Pista - Buscando sugerencia...");
        boolean clueFound = false;

        int emptyCells = board.countEmptyEditableCells();
        //Si solo hay un espacio vacío deja de dr pistas.
        if (emptyCells <= 1) {
            view.showDialog(Alert.AlertType.INFORMATION, "Pista", null,
                    "No se puede dar una pista más sin completar el tablero.");
            return;
        }
        if (pistas == 10) {
            Label label = new Label("😔 Realmente quieres ganar así?");
            label.setStyle("-fx-font-size: 20px;");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pista");
            alert.setHeaderText(null);
            alert.getDialogPane().setContent(label);
            alert.showAndWait();
            return;
        }
        for (int r = 0; r < Board.GRID_SIZE; r++) {
            for (int c = 0; c < Board.GRID_SIZE; c++) {
                Cell cell = board.getCell(r, c);
                // Buscar primera celda editable vacía para la que haya sugerencia
                if (cell.getEditable() && cell.getValue() == 0) {
                    int suggestion = gameState.getSuggestion(r, c);
                    if (suggestion > 0) {
                        System.out.println("Pista: Poner " + suggestion + " en (" + r + "," + c + ")");
                        // Actualizar el Modelo
                        this.board.setCellValue(r, c, suggestion);
                        // Pedir a la Vista que renderice y valide
                        this.view.renderBoard(this.board.getGridSnapshot());
                        this.view.highlightErrors(this.gameState.getInvalidCells());
                        // TODO: Idealmente, pedir a la vista que enfoque la celda: view.focusCell(r, c);
                        clueFound = true;
                        pistas++;
                        break; // Salir del bucle interno
                    }
                }
            }
            if (clueFound) break; // Salir del bucle externo
        }

        if (!clueFound) {
            view.showDialog(Alert.AlertType.INFORMATION, "Pista", null,
                    "No hay pistas obvias disponibles o el tablero está lleno/inválido.");
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
        if (view == null) return; // Seguridad

        try {
            int value = newValue.isEmpty() ? 0 : Integer.parseInt(newValue);
            Cell cell = board.getCell(row, col); // Obtener celda del modelo

            // Solo procesar si es editable y el valor realmente cambió
            if (cell.getEditable() && cell.getValue() != value) {
                // 1. Actualizar el Modelo
                boolean updated = board.setCellValue(row, col, value);

                if (updated) {
                    // 2. Validar estado y pedir a la Vista que actualice resaltados
                    validateAndHighlightBoard();
                    // 3. Comprobar si se ha ganado el juego
                    checkWinCondition();
                }
            }
        } catch (NumberFormatException e) {
            // Esto no debería ocurrir debido al TextFormatter de la vista, pero por robustez:
            System.err.println("Error de formato numérico inesperado: " + newValue);
            // Podríamos limpiar la celda en el modelo si previamente tenía un valor válido
            Cell cell = board.getCell(row, col);
            if(cell.getEditable() && cell.getValue() != 0) {
                board.setCellValue(row, col, 0);
                validateAndHighlightBoard(); // Revalidar
            }
        } catch (IllegalArgumentException e) {
            // Captura valores fuera de rango (0-6) si setCellValue los lanza
            System.err.println("Intento de valor ilegal en ("+row+","+col+"): " + newValue + " - " + e.getMessage());
            // Podríamos forzar a la vista a refrescarse para revertir el cambio visual si es necesario
            // view.renderBoard(board.getGridSnapshot());
        }
    }

    // --- Lógica Interna del Controlador ---

    /**
     * Consulta el estado de validez del tablero y le pide a la vista que actualice
     * el resaltado de las celdas erróneas.
     */
    private void validateAndHighlightBoard() {
        if (view == null) return;
        Set<Pair<Integer, Integer>> invalidCells = gameState.getInvalidCells();
        view.highlightErrors(invalidCells); // Ordena a la vista resaltar
    }

    /**
     * Comprueba si el juego ha sido ganado y, si es así, notifica al usuario
     * a través de la vista y deshabilita la interacción con el tablero.
     */
    private void checkWinCondition() {
        if (view == null) return;
        if (gameState.isGameWon()) {
            System.out.println("¡Juego ganado!");
            view.showDialog(Alert.AlertType.INFORMATION, "¡Felicidades!", "¡Sudoku Resuelto!",
                    "¡Has completado el Sudoku exitosamente!");
            view.setGridDisabled(true); // Ordena a la vista deshabilitar la grilla
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
}