package univalle.tedesoft.sudoku.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import univalle.tedesoft.sudoku.models.Board;
import univalle.tedesoft.sudoku.models.Cell;
import univalle.tedesoft.sudoku.models.GameState;

/**
 * Controlador para la vista principal del juego Sudoku (Refactorizado sin CSS externo).
 * Maneja la interacción del usuario con el tablero y los botones de control.
 * Implementa la creación dinámica de campos de texto y la validación del tablero.
 * Incluye mejoras: caché de nodos, gestión inteligente de errores, comprobación de victoria.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @version 1.1 (Refactorizado)
 */
public class GameController {

    private static final int GRID_SIZE = Board.GRID_SIZE;
    private static final int BLOCK_ROWS = Board.BLOCK_ROWS; // 2
    private static final int BLOCK_COLS = Board.BLOCK_COLS; // 3

    // Constantes para estilos (para facilitar mantenimiento)
    private static final String STYLE_FONT_SIZE = "-fx-font-size: 16px;";
    private static final String STYLE_FONT_BOLD = "-fx-font-weight: bold;";
    private static final String STYLE_ALIGNMENT_CENTER = "-fx-alignment: center;";
    private static final String BORDER_COLOR_NORMAL = "lightgray";
    private static final String BORDER_COLOR_BLOCK = "black";
    private static final String BORDER_COLOR_ERROR = "red";
    private static final String BORDER_WIDTH_NORMAL = "0.5px";
    private static final String BORDER_WIDTH_BLOCK = "2px"; // Borde grueso para bloques Y errores
    private static final String BORDER_STYLE_SOLID = "-fx-border-style: solid;"; // Añadido para asegurar visibilidad

    @FXML private Button clueButton;
    @FXML private Button helpButton;
    @FXML private Button initButton;
    @FXML private Button restartButton;
    @FXML private GridPane sudokuGridPane;

    private Board board;
    private GameState gameState;
    private TextField currentEditingTextField = null;

    /**
     * Arreglo de nodos para cache.
     */
    private Node[][] nodeGrid = new Node[GRID_SIZE][GRID_SIZE];
    /**
     * Permite resaltado de estilos inteligente
     * TODO: mover toda esta lógica de estilos a la capa view
     */
    private Set<Pair<Integer, Integer>> currentErrors = new HashSet<>();

    @FXML
    public void initialize() {
        this.board = new Board();
        this.gameState = new GameState(board);

        // manejar el evento de clickear sobre la grilla
        this.sudokuGridPane.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleGridClick);

        // Configurar acciones de botones (activando más botones)
        this.initButton.setOnAction(event -> startNewGame());
        this.restartButton.setOnAction(event -> restartCurrentGame());
        this.helpButton.setOnAction(event -> showHelp());
        // this.clueButton.setOnAction(event -> showClue()); // TODO: Descomentar cuando se implemente

        // Iniciar el primer juego
        this.board.initializeBoard();
        this.updateGridUI();
    }

    private void startNewGame() {
        // Confirmación antes de iniciar nuevo juego
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Iniciar un nuevo juego borrará el progreso actual. ¿Continuar?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            this.board.initializeBoard();
            this.updateGridUI();
            this.sudokuGridPane.setDisable(false);
        }
    }

    /**
     * Actualiza la interfaz gráfica (GridPane) basándose en el estado actual del Board.
     * Limpia la cuadrícula y la rellena con Labels (fijos) o Panes/TextFields (editables).
     * Aplica estilos de borde inline para simular la cuadrícula.
     */
    private void updateGridUI() {
        // Limpiar celdas anteriores
        this.sudokuGridPane.getChildren().clear();
        // Resetear referencias y estado de errores
        this.currentEditingTextField = null;
        this.nodeGrid = new Node[GRID_SIZE][GRID_SIZE];
        this.currentErrors.clear();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell cell = this.board.getCell(row, col);
                Node cellNode;

                if (!cell.getEditable()) {
                    // --- Celda Fija (Label) ---
                    Label label = new Label(String.valueOf(cell.getValue()));
                    label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Para que ocupe la celda
                    // Aplicar estilo base (fuente) y bordes
                    applyBaseCellStyle(label, row, col);
                    label.setStyle(label.getStyle() + STYLE_FONT_BOLD); // Añadir negrita
                    cellNode = label;
                } else {
                    // --- Celda Editable ---
                    if (cell.getValue() != 0) {
                        // Celda editable con valor preexistente (TextField)
                        TextField existingTf = createTextField(cell, row, col);
                        existingTf.setText(String.valueOf(cell.getValue()));
                        cellNode = existingTf; // Estilos aplicados en createTextField
                    } else {
                        // Celda editable vacía (Pane placeholder)
                        Pane placeholder = new Pane();
                        placeholder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        // Aplicar solo estilo de borde (sin fuente)
                        applyBaseCellStyle(placeholder, row, col);
                        placeholder.setUserData(new int[]{row, col}); // Guardar coordenadas
                        cellNode = placeholder;
                    }
                }

                // Añadir el nodo (Label, TextField o Pane) al GridPane
                GridPane.setRowIndex(cellNode, row);
                GridPane.setColumnIndex(cellNode, col);

                // El alineamiento se maneja en la creación del nodo
                this.sudokuGridPane.getChildren().add(cellNode);

                // --- Guardar en cache de nodos ---
                this.nodeGrid[row][col] = cellNode;
            }
        }
        // Aplicar validación inicial (resaltado de errores si aplica)
        this.validateAndHighlightBoard();
    }

    /**
     * Aplica los estilos base (fuente, alineación, borde) a un nodo de celda.
     * Construye el string de estilo inline para los bordes (normales y de bloque).
     * @param node El nodo (Label, TextField, Pane) al que aplicar el estilo.
     * @param row La fila de la celda.
     * @param col La columna de la celda.
     */
    private void applyBaseCellStyle(Node node, int row, int col) {
        StringBuilder style = new StringBuilder();

        // --- Estilos Comunes ---
        if (node instanceof Labeled) { // Para Label y TextField (que hereda de Labeled)
            ((Labeled) node).setAlignment(Pos.CENTER);
            style.append(STYLE_FONT_SIZE);
        } else if (node instanceof Pane) {
            // Los Panes no tienen texto, pero sí necesitan fondo transparente y tamaño preferido
            // para que el borde sea visible si la celda está vacía.
            node.setStyle("-fx-background-color: transparent;"); // Asegurar transparencia
            // Podríamos darles un tamaño mínimo/preferido para que el borde se vea
            ((Pane) node).setPrefSize(40, 40); // Ajustar si es necesario
        }

        // --- Lógica de Bordes ---
        String topBorder = BORDER_WIDTH_NORMAL;
        String rightBorder = (col + 1) % BLOCK_COLS == 0 ? BORDER_WIDTH_BLOCK : BORDER_WIDTH_NORMAL;
        String bottomBorder = (row + 1) % BLOCK_ROWS == 0 ? BORDER_WIDTH_BLOCK : BORDER_WIDTH_NORMAL;
        String leftBorder = BORDER_WIDTH_NORMAL;

        String topColor = BORDER_COLOR_NORMAL;
        String rightColor = (col + 1) % BLOCK_COLS == 0 ? BORDER_COLOR_BLOCK : BORDER_COLOR_NORMAL;
        String bottomColor = (row + 1) % BLOCK_ROWS == 0 ? BORDER_COLOR_BLOCK : BORDER_COLOR_NORMAL;
        String leftColor = BORDER_COLOR_NORMAL;

        // Ancho de bordes (arriba, derecha, abajo, izquierda)
        style.append("-fx-border-width: ")
                .append(topBorder).append(" ")
                .append(rightBorder).append(" ")
                .append(bottomBorder).append(" ")
                .append(leftBorder).append("; ");

        // Color de bordes
        style.append("-fx-border-color: ")
                .append(topColor).append(" ")
                .append(rightColor).append(" ")
                .append(bottomColor).append(" ")
                .append(leftColor).append("; ");

        // Estilo de borde sólido
        style.append(BORDER_STYLE_SOLID);

        // Aplicar el estilo construido (añadir al estilo existente si lo hubiera)
        String existingStyle = node.getStyle() != null ? node.getStyle() : "";
        if (!existingStyle.endsWith("; ") && !existingStyle.isEmpty()) existingStyle += "; ";
        node.setStyle(existingStyle + style.toString());
    }

    /**
     * Aplica el estilo de resaltado de error a un nodo.
     * Combina los estilos base (fuente, alineación) con un borde rojo grueso.
     * @param node El nodo a resaltar.
     */
    private void applyErrorStyle(Node node) {
        StringBuilder style = new StringBuilder();
        String baseStyle = ""; // Para mantener fuente/alineación

        if (node instanceof Label) {
            baseStyle = STYLE_FONT_SIZE + STYLE_FONT_BOLD + STYLE_ALIGNMENT_CENTER;
        } else if (node instanceof TextField) {
            baseStyle = STYLE_FONT_SIZE + STYLE_ALIGNMENT_CENTER;
        } else if (node instanceof Pane) {
            baseStyle = "-fx-background-color: transparent;"; // Mantener transparencia
            ((Pane) node).setPrefSize(40, 40); // Asegurar tamaño
        }

        // Añadir borde de error
        style.append(baseStyle)
                .append("-fx-border-color: ").append(BORDER_COLOR_ERROR).append("; ")
                .append("-fx-border-width: ").append(BORDER_WIDTH_BLOCK).append("; ") // Usar borde grueso para error
                .append(BORDER_STYLE_SOLID);

        node.setStyle(style.toString());
    }


    /**
     * Maneja los eventos de clic en el GridPane. (Versión Refactorizada con UserData)
     * Determina la celda clickeada usando UserData o índices del GridPane.
     * Si es una celda editable vacía (representada por un Pane),
     * reemplaza el Pane por un TextField editable.
     * Si es un TextField existente, le da el foco.
     * @param event El evento del mouse.
     */
    private void handleGridClick(MouseEvent event) {
        Node clickedNode = event.getPickResult().getIntersectedNode();
        int row = -1, col = -1; // Inicializar con valores inválidos

        // --- ESTRATEGIA PARA OBTENER FILA/COLUMNA ---
        Object userData = clickedNode.getUserData();
        if (userData instanceof int[] coords && coords.length == 2) {
            row = coords[0]; col = coords[1];
        } else {
            Integer colIndex = GridPane.getColumnIndex(clickedNode);
            Integer rowIndex = GridPane.getRowIndex(clickedNode);
            if (colIndex != null && rowIndex != null) {
                row = rowIndex; col = colIndex;
            } else {
                Parent parent = clickedNode.getParent();
                if (parent != null) {
                    colIndex = GridPane.getColumnIndex(parent);
                    rowIndex = GridPane.getRowIndex(parent);
                    if (colIndex != null && rowIndex != null) {
                        row = rowIndex; col = colIndex;
                        clickedNode = parent; // Referir al nodo padre (el que está en el grid)
                    } else {
                        System.err.println("Error Crítico: No se pudo determinar la celda clickeada (padre sin índices). Click ignorado."); return;
                    }
                } else {
                    System.err.println("Error Crítico: No se pudo determinar la celda clickeada (sin índices y sin padre). Click ignorado."); return;
                }
            }
        }

        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            System.err.println("Error: Indices de fila/columna (" + row + "," + col + ") inválidos. Click ignorado."); return;
        }

        // --- Lógica Principal del Clic ---
        Cell cell = board.getCell(row, col);
        boolean isClickOnCurrentEditingField = (currentEditingTextField != null && clickedNode == currentEditingTextField);

        if (cell.getEditable() && clickedNode instanceof Pane) { // Clic en placeholder
            if (currentEditingTextField != null) { // Forzar foco fuera del campo anterior
                Node focusTarget = sudokuGridPane; // Mover foco al GridPane
                if(focusTarget != null) focusTarget.requestFocus();
            }
            sudokuGridPane.getChildren().remove(clickedNode);
            nodeGrid[row][col] = null;
            TextField textField = createTextField(cell, row, col);
            GridPane.setRowIndex(textField, row); GridPane.setColumnIndex(textField, col);
            sudokuGridPane.getChildren().add(textField);
            nodeGrid[row][col] = textField;
            textField.requestFocus();
            currentEditingTextField = textField;
        } else if (clickedNode instanceof TextField) { // Clic en TextField existente
            if (!isClickOnCurrentEditingField) {
                if (currentEditingTextField != null) { // Forzar foco fuera del campo anterior
                    Node focusTarget = sudokuGridPane;
                    if(focusTarget != null) focusTarget.requestFocus();
                }
                clickedNode.requestFocus();
                currentEditingTextField = (TextField) clickedNode;
            }
        } else { // Clic en Label fijo u otro nodo
            if (currentEditingTextField != null) { // Forzar foco fuera del campo anterior
                Node focusTarget = sudokuGridPane;
                if(focusTarget != null) focusTarget.requestFocus();
            }
            currentEditingTextField = null;
        }
    }


    /**
     * Crea y configura un TextField para una celda editable específica.
     * Aplica estilos inline base y configura listeners.
     * @param cell La celda del modelo asociada.
     * @param row  La fila de la celda.
     * @param col  La columna de la celda.
     * @return El TextField configurado.
     */
    private TextField createTextField(Cell cell, int row, int col) {
        TextField textField = new TextField();
        // Aplicar estilos base (bordes, fuente, alineación)
        applyBaseCellStyle(textField, row, col);
        // Ajustar tamaño (puede necesitar !important o ser ajustado en applyBaseCellStyle si hay conflicto)
        textField.setPrefSize(40, 40); // Tamaño preferido
        textField.setMaxSize(40, 40); // Limitar tamaño máximo

        textField.setUserData(new int[]{row, col}); // Guardar coordenadas

        // Configurar TextFormatter (sin cambios)
        Pattern validEditingState = Pattern.compile("^[1-6]?$");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            return validEditingState.matcher(text).matches() ? change : null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        textField.setTextFormatter(textFormatter);

        // Mostrar valor actual (sin cambios)
        if (cell.getValue() != 0) {
            textField.setText(String.valueOf(cell.getValue()));
        }

        // Listener de texto (actualiza modelo y valida)
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int val = newValue.isEmpty() ? 0 : Integer.parseInt(newValue);
                if (cell.getValue() != val) {
                    this.board.setCellValue(row, col, val);
                    this.validateAndHighlightBoard(); // Validar después de cada cambio
                    this.checkWinCondition();         // Comprobar victoria
                }
            } catch (NumberFormatException e) {
                if (cell.getValue() != 0) {
                    this.board.setCellValue(row, col, 0);
                    this.validateAndHighlightBoard();
                }
            }
        });

        // Listener de teclas (sin cambios)
        textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                // La lógica de vaciado está cubierta por el listener de textProperty
            }
        });

        // Listener de foco (reemplaza por placeholder si está vacío)
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) { // Perdió el foco
                if (textField.getText().isEmpty()) {
                    this.replaceTextFieldWithPlaceholder(textField, row, col);
                } else {
                    // Si tiene contenido, validar por si acaso (aunque textProperty ya lo hizo)
                    this.validateAndHighlightBoard();
                }
                if (currentEditingTextField == textField) {
                    currentEditingTextField = null; // Ya no es el activo
                }
            }
        });

        return textField;
    }

    /**
     * Reemplaza un TextField vacío que ha perdido el foco por un Pane placeholder.
     * @param textField El TextField a reemplazar.
     * @param row La fila.
     * @param col La columna.
     */
    private void replaceTextFieldWithPlaceholder(TextField textField, int row, int col) {
        // Solo reemplazar si realmente está en la cuadrícula y vacío
        if (textField.getText().isEmpty() && nodeGrid[row][col] == textField) {
            this.sudokuGridPane.getChildren().remove(textField);
            Pane placeholder = new Pane();
            // Aplicar estilos base (bordes)
            applyBaseCellStyle(placeholder, row, col);
            placeholder.setUserData(new int[]{row, col}); // Guardar coordenadas

            GridPane.setRowIndex(placeholder, row); GridPane.setColumnIndex(placeholder, col);
            this.sudokuGridPane.getChildren().add(placeholder);
            nodeGrid[row][col] = placeholder; // Actualizar cache

            // Asegurarse de que el modelo también esté vacío
            if (board.getCell(row, col).getValue() != 0) {
                board.setCellValue(row, col, 0);
                // No es necesario validar aquí, ya se hizo al vaciar el TextField
            }
        }
    }

    /**
     * Valida el tablero y actualiza la UI para resaltar errores de forma inteligente.
     * Usa la caché de nodos y compara errores actuales con previos.
     * Aplica/Restaura estilos inline.
     */
    private void validateAndHighlightBoard() {
        Set<Pair<Integer, Integer>> newErrors = gameState.getInvalidCells();

        // --- Gestión de Estilos Inteligente ---
        // Quitar resaltado de errores que ya no existen
        Set<Pair<Integer, Integer>> errorsToRemove = new HashSet<>(currentErrors);
        errorsToRemove.removeAll(newErrors); // Celdas que estaban en error pero ya no
        for (Pair<Integer, Integer> coord : errorsToRemove) {
            Node node = nodeGrid[coord.getKey()][coord.getValue()];
            if (node != null) {
                // Reaplicar estilo base para quitar el borde rojo
                applyBaseCellStyle(node, coord.getKey(), coord.getValue());
                // Reaplicar negrita si era un Label fijo
                if(node instanceof Label && !board.getCell(coord.getKey(), coord.getValue()).getEditable()){
                    node.setStyle(node.getStyle() + STYLE_FONT_BOLD);
                }
            }
        }

        // Añadir resaltado a nuevos errores
        Set<Pair<Integer, Integer>> errorsToAdd = new HashSet<>(newErrors);
        errorsToAdd.removeAll(currentErrors); // Celdas que no estaban en error y ahora sí
        for (Pair<Integer, Integer> coord : errorsToAdd) {
            Node node = nodeGrid[coord.getKey()][coord.getValue()];
            if (node != null) {
                // Aplicar estilo de error (que incluye borde rojo y estilos base)
                applyErrorStyle(node);
            } else {
                System.err.println("Error: No se encontró nodo en la cache para resaltar: " + coord.getKey() + "," + coord.getValue());
            }
        }

        // Actualizar el conjunto de errores actual
        currentErrors = newErrors;

        // Opcional: Log de depuración
        // if (!newErrors.isEmpty()) {
        //     System.out.println("Errores actuales: " + newErrors);
        // }
    }

    /**
     * Comprueba si el juego ha sido ganado y muestra un mensaje.
     */
    private void checkWinCondition() {
        if (gameState.isGameWon()) {
            Alert winAlert = new Alert(Alert.AlertType.INFORMATION);
            winAlert.setTitle("¡Felicidades!");
            winAlert.setHeaderText("¡Sudoku Resuelto!");
            winAlert.setContentText("¡Has completado el Sudoku exitosamente!");
            winAlert.showAndWait();
            sudokuGridPane.setDisable(true); // Deshabilitar más interacción
        }
    }

    // --- Funciones de Botones ---
    private void restartCurrentGame() {
        // Reinicia iniciando un juego completamente nuevo.
        System.out.println("Reiniciar Juego - Iniciando uno nuevo...");
        startNewGame();
    }

    private void showHelp() {
        System.out.println("Mostrando Ayuda...");
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Ayuda Sudoku 6x6");
        helpAlert.setHeaderText("Reglas del Juego");
        helpAlert.setContentText("""
                Completa la cuadrícula de 6x6 con números del 1 al 6.
                - Cada fila debe contener todos los números del 1 al 6 sin repetición.
                - Cada columna debe contener todos los números del 1 al 6 sin repetición.
                - Cada bloque de 2x3 debe contener todos los números del 1 al 6 sin repetición.
                Haz clic en una celda vacía para ingresar un número. Las celdas con números en negrita son fijas.
                Usa las teclas DELETE o BACKSPACE para borrar un número ingresado.
                """);
        helpAlert.showAndWait();
    }

    private void showClue() {
        System.out.println("Pista - Funcionalidad no implementada todavía.");
        // --- Lógica básica para pista (requiere prueba y refinamiento) ---
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Cell cell = board.getCell(r, c);
                if (cell.getEditable() && cell.getValue() == 0) { // Buscar celda vacía y editable
                    int suggestion = gameState.getSuggestion(r, c);
                    if (suggestion > 0) {
                        // Encontró una pista válida
                        Node node = nodeGrid[r][c];
                        TextField tfToShowClue;

                        if (node instanceof Pane) { // Si es placeholder, reemplazarlo por TextField
                            sudokuGridPane.getChildren().remove(node);
                            tfToShowClue = createTextField(cell, r, c);
                            GridPane.setRowIndex(tfToShowClue, r); GridPane.setColumnIndex(tfToShowClue, c);
                            sudokuGridPane.getChildren().add(tfToShowClue);
                            nodeGrid[r][c] = tfToShowClue;
                        } else if (node instanceof TextField) { // Si ya es TextField (poco probable si está vacío)
                            tfToShowClue = (TextField) node;
                        } else {
                            System.err.println("Error Pista: Nodo inesperado en celda vacía editable: " + node);
                            continue; // Buscar otra celda
                        }

                        // Poner la sugerencia y opcionalmente resaltarla/enfocarla
                        tfToShowClue.setText(String.valueOf(suggestion));
                        // El listener de textProperty actualizará el modelo y validará.
                        tfToShowClue.requestFocus(); // Darle foco
                        System.out.println("Pista: Poner " + suggestion + " en (" + r + "," + c + ")");
                        // Podrías añadir un estilo temporal para la pista aquí
                        // Platform.runLater(() -> { ... estilo temporal ... });
                        return; // Salir después de dar una pista
                    }
                }
            }
        }
        // Si el bucle termina, no se encontraron pistas
        Alert noClue = new Alert(Alert.AlertType.INFORMATION, "No hay pistas obvias disponibles o el tablero está lleno/inválido.");
        noClue.showAndWait();
    }
}