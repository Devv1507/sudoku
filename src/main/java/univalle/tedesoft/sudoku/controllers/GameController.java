package univalle.tedesoft.sudoku.controllers;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import univalle.tedesoft.sudoku.models.Board;
import univalle.tedesoft.sudoku.models.Cell;
import univalle.tedesoft.sudoku.models.GameState;

import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

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
     * Método de inicialización llamado automáticamente después de cargar el FXML.
     * Configura el tablero inicial y los manejadores de eventos.
     */
    @FXML
    public void initialize() {
        this.board = new Board();
        this.gameState = new GameState(board);

        // Configurar manejador de clics para el GridPane
        this.sudokuGridPane.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleGridClick);

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
        // TO-DO: esto aún esta construyendose
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Iniciar un nuevo juego borrará el progreso actual. ¿Continuar?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Genera la lógica del tablero (números iniciales, celdas fijas)
            this.board.initializeBoard();
            // Actualiza la interfaz gráfica con el nuevo tablero
            this.updateGridUI();
        }
    }

    /**
     * Limpia el GridPane y lo rellena con Labels (para celdas fijas)
     * y placeholders (para celdas editables) basados en el estado actual del tablero.
     */
    private void updateGridUI() {
        // Limpiar celdas anteriores
        this.sudokuGridPane.getChildren().clear();
        // Resetear referencia al TextField activo
        this.currentEditingTextField = null;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell cell = this.board.getCell(row, col);
                Node cellNode;

                if (!cell.isEditable()) {
                    //Celda fija: Crear un Label
                    Label label = new Label(String.valueOf(cell.getValue()));
                    label.setAlignment(Pos.CENTER);
                    // Ocupar toda la celda
                    label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
                    cellNode = label;
                } else {
                    /**
                     * Celda editable: Crear un placeholder (Pane vacío) que recibirá clics
                     * Opcionalmente, si la celda tiene un valor (p.ej. de un juego guardado), mostrarlo
                     */
                    Pane placeholder = new Pane();
                    placeholder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    /*
                     * Podríamos añadir un TextField aquí si ya tiene valor, pero la lógica actual lo crea al hacer clic
                     * Si queremos mostrar números ingresados previamente al reiniciar UI:
                     */
                    // if (cell.getValue() != 0) {
                    //     TextField existingTf = createTextField(cell, row, col);
                    //     existingTf.setText(String.valueOf(cell.getValue()));
                    //     cellNode = existingTf;
                    // } else {
                    //     /**
                    //      *
                    //      */
                    //     cellNode = placeholder;
                    // }
                }

                //Añadir el nodo (Label o Pane) al GridPane
                GridPane.setRowIndex(cellNode, row);
                GridPane.setColumnIndex(cellNode, col);
                GridPane.setHalignment(cellNode, HPos.CENTER);
                GridPane.setValignment(cellNode, VPos.CENTER);
                this.sudokuGridPane.getChildren().add(cellNode);
            }
        }
        //Aplicar validación inicial (resaltar errores si el tablero generado tiene alguno, aunque no debería)
        this.validateAndHighlightBoard();
    }


    /**
     * Maneja los eventos de clic en el GridPane.
     * Determina la celda clickeada y, si es editable y no contiene ya un TextField,
     * reemplaza el placeholder por un TextField editable.
     * @param event El evento del mouse.
     */
    private void handleGridClick(MouseEvent event) {
        Node clickedNode = event.getPickResult().getIntersectedNode();
        // Asegurarse de que el clic fue dentro de una celda del GridPane (no en las líneas) y obtener la fila/columna
        Integer colIndex = GridPane.getColumnIndex(clickedNode);
        Integer rowIndex = GridPane.getRowIndex(clickedNode);
        if (colIndex == null || rowIndex == null) {
            if (clickedNode.getParent() instanceof GridPane) {
                // Si el nodo clickeado es hijo directo, obtener índices
                colIndex = GridPane.getColumnIndex(clickedNode);
                rowIndex = GridPane.getRowIndex(clickedNode);
            } else if (clickedNode.getParent() != null && clickedNode.getParent().getParent() instanceof GridPane){
                // Si el nodo clickeado es nieto (p.ej., texto dentro de TextField)
                colIndex = GridPane.getColumnIndex(clickedNode.getParent());
                rowIndex = GridPane.getRowIndex(clickedNode.getParent());
            } else {
                // No se pudo determinar la celda
                return;
            }
        }
        if (colIndex == null || rowIndex == null) {
            System.err.println("Error: No se pudo determinar la celda clickeada.");
            return;
        }


        int row = rowIndex;
        int col = colIndex;

        Cell cell = board.getCell(row, col);

        //Si la celda es editable y el nodo clickeado NO es ya un TextField
        if (cell.isEditable() && !(clickedNode instanceof TextField) && !(clickedNode.getParent() instanceof TextField) ) {
            //Si había otro TextField en edición, validar su contenido antes de cambiar
            if (currentEditingTextField != null) {
                //Forzar validación del TextField anterior si perdió el foco
                this.validateAndHighlightBoard();
            }

            //Eliminar el nodo actual (placeholder Pane o Label si hubiera error)
            sudokuGridPane.getChildren().remove(clickedNode);

            // Crear y añadir el nuevo TextField
            TextField textField = createTextField(cell, row, col);
            GridPane.setRowIndex(textField, row);
            GridPane.setColumnIndex(textField, col);
            sudokuGridPane.getChildren().add(textField);
            // Poner el foco en el nuevo TextField
            textField.requestFocus();
            // Actualizar referencia
            currentEditingTextField = textField;
        } else if (clickedNode instanceof TextField) {
            // Si se hizo clic en un TextField existente, asegurarse de que tiene el foco
            clickedNode.requestFocus();
            currentEditingTextField = (TextField) clickedNode;
        } else if (clickedNode.getParent() instanceof TextField) {
            // Click en el texto dentro del TextField
            clickedNode.getParent().requestFocus();
            currentEditingTextField = (TextField) clickedNode.getParent();
        }
        // Si la celda no es editable, no hacer nada
    }


    /**
     * Crea y configura un TextField para una celda editable específica.
     *
     * @param cell La celda del modelo asociada.
     * @param row  La fila de la celda.
     * @param col  La columna de la celda.
     * @return El TextField configurado.
     */
    private TextField createTextField(Cell cell, int row, int col) {
        TextField textField = new TextField();
        textField.setAlignment(Pos.CENTER);
        // Ajustar tamaño según sea necesario
        textField.setMaxSize(40, 40);
        textField.setPrefSize(40, 40);
        textField.setStyle("-fx-font-size: 16px;");

        // Guardar fila y columna en el TextField para referencia futura
        textField.setUserData(new int[]{row, col});

        // Configurar TextFormatter para permitir solo números del 1 al 6 y vacío
        Pattern validEditingState = Pattern.compile("^[1-6]?$");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return change;
            } else {
                return null;
            }
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        textField.setTextFormatter(textFormatter);

        // Mostrar valor actual de la celda (si existe)
        if (cell.getValue() != 0) {
            textField.setText(String.valueOf(cell.getValue()));
        }


        // Listener para actualizar el modelo cuando el texto cambia
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int val = newValue.isEmpty() ? 0 : Integer.parseInt(newValue);
                // Solo actualizar si el valor es realmente diferente para evitar ciclos
                if (cell.getValue() != val) {
                    this.board.setCellValue(row, col, val);
                    // Validar después de cada cambio
                    this.validateAndHighlightBoard();
                }
            } catch (NumberFormatException e) {
                // Si no es número válido (debería prevenirlo el formatter), poner 0
                this.board.setCellValue(row, col, 0);
                this.validateAndHighlightBoard();
            }
        });

        // Listener para manejar la tecla DELETE/BACKSPACE para vaciar la celda
        textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                if (textField.getText().length() == 1 && (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE)) {
                    // El listener de textProperty se activará cuando se borre
                } else if (textField.getText().isEmpty() && (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE)) {
                    // Ya está vacío, limpiar modelo si acaso no lo estaba
                    if (cell.getValue() != 0) {
                        this.board.setCellValue(row, col, 0);
                        this.validateAndHighlightBoard();
                    }
                }
            }
        });

        // Listener para cuando el TextField pierde el foco
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                // Cuando pierde el foco, si está vacío, reemplazarlo por un Pane (placeholder)
                // para que el siguiente clic lo recree.
                if (textField.getText().isEmpty()) {
                    this.replaceTextFieldWithPlaceholder(textField, row, col);
                } else {
                    // Si tiene contenido válido, solo validar y quitar foco
                    this.validateAndHighlightBoard(); // Asegurarse de que la validación se ejecute
                }
                if (currentEditingTextField == textField) {
                    currentEditingTextField = null; // Ya no es el TextField activo
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
        if (textField.getText().isEmpty()) {
            this.sudokuGridPane.getChildren().remove(textField);
            Pane placeholder = new Pane();
            placeholder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setRowIndex(placeholder, row);
            GridPane.setColumnIndex(placeholder, col);
            this.sudokuGridPane.getChildren().add(placeholder);
            // Asegurarse de que el modelo también esté vacío
            if(board.getCell(row, col).getValue() != 0) {
                board.setCellValue(row, col, 0);
                this.validateAndHighlightBoard();
            }
        }
    }


    /**
     * Valida el estado actual del tablero usando GameState y actualiza la UI
     * para resaltar las celdas con errores.
     */
    private void validateAndHighlightBoard() {
        // Limpiar resaltados anteriores
        for (Node node : sudokuGridPane.getChildren()) {
            node.setStyle(node.getStyle().replace("-fx-border-color: red;", "").replace("-fx-border-width: 2px;", ""));
        }

        // Obtener celdas inválidas
        var invalidCoordinates = gameState.getInvalidCells(); // GameState necesita acceso al board actualizado

        if (!invalidCoordinates.isEmpty()) {
            System.out.println("Errores encontrados en: " + invalidCoordinates); // Log de depuración
        }

        // Resaltar celdas inválidas
        for (var coord : invalidCoordinates) {
            int r = coord.getKey();
            int c = coord.getValue();
            Node node = getNodeFromGridPane(sudokuGridPane, c, r);
            if (node != null) {
                String currentStyle = node.getStyle();
                if (!currentStyle.contains("-fx-border-color: red;")) {
                    if (node instanceof TextField) {
                        node.setStyle("-fx-font-size: 16px; -fx-border-color: red; -fx-border-width: 2px;");
                    } else if (node instanceof Label) {
                        node.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-border-color: red; -fx-border-width: 2px;");
                    } else {
                        node.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    }
                }
            } else {
                System.err.println("Error: No se encontró nodo en la cuadrícula para resaltar: " + r + "," + c);
            }
        }
    }

    /**
     * Obtiene el nodo (Label, TextField, Pane) en una posición específica del GridPane.
     * @param gridPane El GridPane a buscar.
     * @param col      La columna.
     * @param row      La fila.
     * @return El Node encontrado, o null si no hay ninguno en esa celda.
     */
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            Integer c = GridPane.getColumnIndex(node);
            Integer r = GridPane.getRowIndex(node);
            if (c != null && r != null && c == col && r == row) {
                return node;
            }
        }
        return null;
    }

    private void restartCurrentGame() {
        // Lógica para reiniciar el tablero actual a su estado inicial (si se guardó)
        System.out.println("Reiniciar Juego - Funcionalidad no implementada");
        // Posiblemente llamar a board.resetToInitialState() y luego updateGridUI(), es como iniciar uno nuevo.
        this.startNewGame();
    }

    private void showHelp() {
        // Lógica para mostrar una ventana o panel de ayuda
        System.out.println("Ayuda - Funcionalidad no implementada");
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Ayuda Sudoku 6x6");
        helpAlert.setHeaderText("Reglas del Juego");
        helpAlert.setContentText("Completa la cuadrícula de 6x6 con números del 1 al 6.\n" +
                "- Cada fila debe contener todos los números del 1 al 6 sin repetición.\n" +
                "- Cada columna debe contener todos los números del 1 al 6 sin repetición.\n" +
                "- Cada bloque de 2x3 debe contener todos los números del 1 al 6 sin repetición.\n" +
                "Haz clic en una celda vacía para ingresar un número. Las celdas con números en negrita son fijas.");
        helpAlert.showAndWait();
    }

    private void showClue() {
        // Lógica para obtener y mostrar una pista (HU-5)
        System.out.println("Pista - Funcionalidad no implementada");
        // 1. Encontrar una celda vacía y editable
        // 2. Pedir a GameState una sugerencia válida para esa celda
        // 3. Mostrar la sugerencia (ej. rellenar temporalmente el TextField)
    }
}
