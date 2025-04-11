package univalle.tedesoft.sudoku.views;

import javafx.geometry.Pos;
import univalle.tedesoft.sudoku.Main;
import univalle.tedesoft.sudoku.controllers.GameController;
import univalle.tedesoft.sudoku.models.Board;
import univalle.tedesoft.sudoku.models.Cell;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Representa la ventana principal y la LÓGICA DE VISTA del juego Sudoku (MVC).
 * Carga FXML, renderiza el tablero, maneja interacciones de UI directas
 * y delega eventos lógicos al GameController.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @version 1.3 (Refactorizado MVC)
 */
public class GameView extends Stage {
    // Constantes de Estilo
    private static final int GRID_SIZE = Board.GRID_SIZE;
    private static final int BLOCK_ROWS = Board.BLOCK_ROWS;
    private static final int BLOCK_COLS = Board.BLOCK_COLS;
    private static final String STYLE_FONT_SIZE = "-fx-font-size: 16px;";
    private static final String STYLE_FONT_BOLD = "-fx-font-weight: bold;";
    private static final String STYLE_ALIGNMENT_CENTER = "-fx-alignment: center;";
    private static final String BORDER_COLOR_NORMAL = "lightgray";
    private static final String BORDER_COLOR_BLOCK = "black";
    private static final String BORDER_COLOR_ERROR = "red";
    private static final String BORDER_WIDTH_NORMAL = "0.5px";
    private static final String BORDER_WIDTH_BLOCK = "2px";
    private static final String BORDER_STYLE_SOLID = "-fx-border-style: solid;";
    private static final String HIGHLIGHT_BACKGROUND_COLOR = "lightblue";

    // Referencias
    private final GridPane sudokuGridPane;
    private final GameController controller;

    // Estado Interno de la Vista
    private Node[][] nodeGrid = new Node[GRID_SIZE][GRID_SIZE]; // Cache de nodos UI para acceso rápido
    private TextField currentEditingTextField = null; // Campo de texto activo actualmente
    private Set<Pair<Integer, Integer>> currentErrorCoords = new HashSet<>(); // Coords con error resaltado
    private Set<Pair<Integer, Integer>> currentlyHighlightedCoords = new HashSet<>(); // resaltar celdas por hover

    /**
     * Constructor privado Singleton. Carga el archivo FXML.
     * @throws IOException Si falla la carga FXML.
     */
    private GameView() throws IOException {
        URL fxmlUrl = Main.class.getResource("sudoku-view.fxml");
        if (fxmlUrl == null) {
            throw new IOException("No se pudo encontrar el archivo FXML: sudoku-view.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(fxmlLoader.load());

        this.controller = fxmlLoader.getController();
        if (this.controller == null) {
            throw new IOException("El controlador no se pudo obtener desde FXMLLoader.");
        }

        // Obtenemos la referencia al GridPane desde el controlador
        this.sudokuGridPane = this.controller.getSudokuGridPane();
        if (this.sudokuGridPane == null) {
            throw new IOException("El GridPane no se pasó correctamente al controlador.");
        }
        this.controller.setView(this);

        // Añadimos el handler de clics en la grid
        this.sudokuGridPane.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleGridClick);

        this.setTitle("6x6 Sudoku Game");
        this.setScene(scene);
        this.setResizable(false);
    }

    /**
     * Renderiza completamente el tablero en el GridPane basándose en los datos proporcionados.
     * @param gridData Matriz 2D con la información de cada celda.
     */
    public void renderBoard(Cell[][] gridData) {
        // Limpiar contenido anterior
        sudokuGridPane.getChildren().clear();
        // Resetear caché de nodos
        nodeGrid = new Node[GRID_SIZE][GRID_SIZE];
        // Evitar que haya campos activos al re-renderizar
        currentEditingTextField = null;
        // Limpiar resaltados de hover anteriores
        this.currentlyHighlightedCoords.clear();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell cellData = gridData[row][col];
                Node cellNode;

                if (!cellData.getEditable()) {
                    // Celda Fija: Crear un Label
                    Label label = new Label(String.valueOf(cellData.getValue()));
                    label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    // Añadir manejadores de hover
                    label.setOnMouseEntered(this::handleMouseEntered);
                    label.setOnMouseExited(this::handleMouseExited);
                    cellNode = label;
                } else {
                    // Celda Editable
                    if (cellData.getValue() != 0) {
                        // Con valor inicial: Crear TextField prellenado
                        TextField tf = this.createTextField(row, col);
                        tf.setText(String.valueOf(cellData.getValue()));
                        cellNode = tf;
                    } else {
                        // Vacía: Crear un Pane placeholder cliqueable
                        cellNode = this.createPlaceholderPane(row, col);;
                    }
                }

                // Añadir el nodo creado al GridPane
                GridPane.setRowIndex(cellNode, row);
                GridPane.setColumnIndex(cellNode, col);
                sudokuGridPane.getChildren().add(cellNode);

                // Guardar referencia en la caché de la vista
                nodeGrid[row][col] = cellNode;
            }
        }

        // Aplicar el estilo inicial a todas las celdas DESPUÉS de que estén todas en la caché nodeGrid
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                this.updateCellStyle(r, c);
            }
        }

        // Reaplicar el estado de errores visualmente
        highlightErrors(this.currentErrorCoords);
    }

    /**
     * Actualiza los estilos de las celdas para mostrar cuáles tienen errores.
     * Actualiza el estado del error y llama a updateCellStyle.
     * Lanza un mensaje de error al ingresar valores inválidos.
     * @param newErrorCoords Conjunto de coordenadas (fila, columna) de las celdas con errores.
     */
    public void highlightErrors(Set<Pair<Integer, Integer>> newErrorCoords) {
        Set<Pair<Integer, Integer>> oldErrorCoords = this.currentErrorCoords;
        this.currentErrorCoords = new HashSet<>(newErrorCoords);

        // Determinar qué celdas cambiaron su estado de error
        Set<Pair<Integer, Integer>> changedCoords = new HashSet<>(oldErrorCoords);
        // Unión de coordenadas viejas y nuevas
        changedCoords.addAll(newErrorCoords);

        // Actualizar el estilo de todas las celdas afectadas
        for (Pair<Integer, Integer> coord : changedCoords) {
            this.updateCellStyle(coord.getKey(), coord.getValue());
        }

        if (!newErrorCoords.isEmpty()) {
            showDialog(Alert.AlertType.ERROR, "Errores detectados",
                    "Algunas celdas son inválidas", "Revisa los valores marcados en rojo.");
        }
    }

    /**
     * Muestra un diálogo de alerta estándar.
     * @param type Tipo de alerta (INFORMATION, CONFIRMATION, etc.).
     * @param title Título de la ventana del diálogo.
     * @param header Texto del encabezado.
     * @param content Texto principal del cuerpo.
     * @return Un Optional que contiene el ButtonType presionado por el usuario (útil para CONFIRMATION).
     */
    public Optional<ButtonType> showDialog(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        // Podríamos añadirle icono o hacerlo modal si fuera necesario
        // alert.initOwner(this); // Asociar al Stage principal
        return alert.showAndWait();
    }

    /**
     * Habilita o deshabilita la interacción del usuario con el GridPane.
     * @param disable true para deshabilitar, false para habilitar.
     */
    public void setGridDisabled(boolean disable) {
        this.sudokuGridPane.setDisable(disable);
    }

    /**
     * Manejador para eventos de clic dentro del GridPane.
     * Determina la celda clickeada y, si es editable, la prepara para edición
     * (reemplazando Pane por TextField o dando foco). Delega el evento al controlador.
     * @param event El evento del ratón.
     */
    private void handleGridClick(MouseEvent event) {
        Node clickedNode = event.getPickResult().getIntersectedNode();
        int[] coords = getClickedCellCoords(clickedNode); // Intenta obtener fila y columna

        if (coords == null) {
            // Clic fuera de una celda reconocible (i.e. en las líneas de la grilla)
            // Si había un campo en edición, quitarle el foco
            if (this.currentEditingTextField != null) {
                // Mover foco a la grilla como contenedor
                this.sudokuGridPane.requestFocus();
                this.currentEditingTextField = null;
            }
            return; // Ignorar este clic
        }

        int row = coords[0];
        int col = coords[1];

        // --- Lógica de UI para manejar la edición ---
        Node targetNodeInGrid = nodeGrid[row][col]; // Nodo actual en esa posición según nuestra caché

        if (targetNodeInGrid instanceof Pane) {
            // Clic en un placeholder (celda editable vacía) -> Convertir a TextField
            this.switchToTextField(targetNodeInGrid, row, col);

        } else if (targetNodeInGrid instanceof TextField) {
            // Clic en un TextField existente
            if (this.currentEditingTextField != targetNodeInGrid) {
                // Si se estaba editando otro campo, quitarle el foco
                if (this.currentEditingTextField != null) {
                    this.sudokuGridPane.requestFocus(); // Mover foco fuera del campo anterior
                }
                // Dar foco al campo clickeado
                targetNodeInGrid.requestFocus();
                this.currentEditingTextField = (TextField) targetNodeInGrid;
            }
            // No hacer nada si se clickea en el mismo campo que ya tiene foco

        } else {
            // Si se estaba editando un campo, quitarle el foco
            if (this.currentEditingTextField != null) {
                this.sudokuGridPane.requestFocus();
                this.currentEditingTextField = null;
            }
        }
    }

    /**
     * Reemplaza un nodo (presumiblemente un Pane) con un TextField editable en la misma celda.
     * Llama a updateCellStyle para aplicar el estilo correcto.
     * @param nodeToReplace El nodo (Pane) a quitar.
     * @param row La fila.
     * @param col La columna.
     */
    private void switchToTextField(Node nodeToReplace, int row, int col) {
        if (this.currentEditingTextField != null && this.currentEditingTextField != nodeToReplace) {
            this.sudokuGridPane.requestFocus();
        }
        TextField textField = this.createTextField(row, col); // Ya tiene handlers de hover
        this.sudokuGridPane.getChildren().remove(nodeToReplace);
        GridPane.setRowIndex(textField, row);
        GridPane.setColumnIndex(textField, col);
        this.sudokuGridPane.getChildren().add(textField);
        this.nodeGrid[row][col] = textField; // Actualizar caché

        // Aplicar estilo correcto (puede estar resaltado por hover o tener error)
        this.updateCellStyle(row, col);

        textField.requestFocus();
        this.currentEditingTextField = textField; // Marcar como campo en edición
    }

    /**
     * Reemplaza un TextField vacío que ha perdido el foco con un Pane placeholder.
     * @param textField El TextField a reemplazar.
     * @param row La fila.
     * @param col La columna.
     */
    private void replaceTextFieldWithPlaceholder(TextField textField, int row, int col) {
        if (this.nodeGrid[row][col] == textField) {
            this.sudokuGridPane.getChildren().remove(textField);
            Pane placeholder = createPlaceholderPane(row, col);
            GridPane.setRowIndex(placeholder, row);
            GridPane.setColumnIndex(placeholder, col);
            this.sudokuGridPane.getChildren().add(placeholder);
            this.nodeGrid[row][col] = placeholder; // Actualizar caché

            // Restaurar estilo base (o de error si lo tenía)
            this.updateCellStyle(row, col);
        }
        if(this.currentEditingTextField == textField) {
            this.currentEditingTextField = null;
        }
    }


    /**
     * Crea y configura un TextField para una celda editable. Incluye listeners.
     * @param row Fila donde estará el TextField.
     * @param col Columna donde estará el TextField.
     * @return El TextField configurado.
     */
    private TextField createTextField(int row, int col) {
        TextField textField = new TextField();
        textField.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // que crezca a todo lo disponible
        textField.setAlignment(Pos.CENTER); // alinear texto al centro
        textField.setUserData(new int[]{row, col});

        // Filtro y listeners existentes
        Pattern validEditingState = Pattern.compile("^[1-6]?$");
        UnaryOperator<TextFormatter.Change> filter = change ->
                validEditingState.matcher(change.getControlNewText()).matches() ? change : null;
        textField.setTextFormatter(new TextFormatter<>(filter));
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.controller.cellValueChanged(row, col, newValue);
        });
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && textField.getText().isEmpty()) {
                replaceTextFieldWithPlaceholder(textField, row, col);
            } else if (!isNowFocused && this.currentEditingTextField == textField) {
                this.currentEditingTextField = null;
            } else if (isNowFocused) {
                this.currentEditingTextField = textField;
            }
        });
        // Añadir handlers de hover
        textField.setOnMouseEntered(this::handleMouseEntered);
        textField.setOnMouseExited(this::handleMouseExited);

        return textField;
    }

    /**
     * Crea un Pane que sirve como placeholder visual para celdas editables vacías.
     * @param row Fila.
     * @param col Columna.
     * @return El Pane configurado.
     */
    private Pane createPlaceholderPane(int row, int col) {
        Pane placeholder = new Pane();
        placeholder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // NO aplicar estilo base aquí
        placeholder.setUserData(new int[]{row, col});

        // Añadir handlers de hover
        placeholder.setOnMouseEntered(this::handleMouseEntered);
        placeholder.setOnMouseExited(this::handleMouseExited);

        return placeholder;
    }


    /**
     * Intenta obtener las coordenadas (fila, columna) de la celda asociada a un nodo de la UI.
     * @param node El nodo de la UI (puede ser Label, TextField, Pane o incluso contenido interno).
     * @return Un array `int[]{row, col}` o `null` si no se pueden determinar.
     */
    private int[] getClickedCellCoords(Node node) {
        // Estrategia 1: UserData (establecido en createTextField/createPlaceholderPane)
        Object userData = node.getUserData();
        if (userData instanceof int[] coords && coords.length == 2) {
            // Validar que los índices estén en rango por si acaso
            if (coords[0] >= 0 && coords[0] < GRID_SIZE && coords[1] >= 0 && coords[1] < GRID_SIZE) {
                return coords;
            }
        }

        // Estrategia 2: Índices de GridPane del propio nodo
        Integer colIndex = GridPane.getColumnIndex(node);
        Integer rowIndex = GridPane.getRowIndex(node);
        if (rowIndex != null && colIndex != null) {
            // Validar que el nodo sea uno de los que están directamente en nuestra caché
            if (nodeGrid[rowIndex][colIndex] == node) {
                return new int[]{rowIndex, colIndex};
            }
        }

        // Estrategia 3: Índices de GridPane del padre (si el clic fue en contenido interno)
        Parent parent = node.getParent();
        if (parent != null) {
            colIndex = GridPane.getColumnIndex(parent);
            rowIndex = GridPane.getRowIndex(parent);
            if (rowIndex != null && colIndex != null) {
                // Validar que el padre sea uno de los nodos en nuestra caché
                if (nodeGrid[rowIndex][colIndex] == parent) {
                    return new int[]{rowIndex, colIndex};
                }
            }
        }

        System.err.println("No se pudieron determinar las coordenadas para el nodo: " + node);
        return null;
    }


    /**
     * Aplica el estilo completo a una celda basándose en su estado actual
     * (fija/editable, error, resaltado por hover).
     * REEMPLAZA a applyBaseCellStyle, applyErrorStyle, restoreBaseStyle.
     *
     * @param row Fila de la celda.
     * @param col Columna de la celda.
     */
    private void updateCellStyle(int row, int col) {
        // Asegurarse de que el nodo exista en la caché
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE || nodeGrid == null || nodeGrid[row][col] == null) {
            //System.err.println("Intento de actualizar estilo para celda inválida o nodo nulo en (" + row + "," + col + ")");
            return; // Salir si el nodo no está listo o los índices son inválidos
        }
        Node node = nodeGrid[row][col];

        boolean isFixed = !this.controller.isCellEditable(row, col);
        boolean isError = this.currentErrorCoords.contains(new Pair<>(row, col));
        boolean isHighlighted = this.currentlyHighlightedCoords.contains(new Pair<>(row, col));

        // 1. Base Font/Alignment Styles
        StringBuilder styleBuilder = new StringBuilder();
        if (node instanceof Labeled || node instanceof TextField) {
            styleBuilder.append(STYLE_ALIGNMENT_CENTER); // Centrado
            styleBuilder.append(STYLE_FONT_SIZE);      // Tamaño de fuente base
            if (isFixed && node instanceof Labeled) {   // Negrita solo para Labels fijos
                styleBuilder.append(STYLE_FONT_BOLD);
            }
        }

        // 2. Border Styles (Determinado por error y posición de bloque)
        String topBorderWidth, rightBorderWidth, bottomBorderWidth, leftBorderWidth;
        String topBorderColor, rightBorderColor, bottomBorderColor, leftBorderColor;

        if (isError) {
            // Estilo de borde de error (rojo y grueso)
            topBorderWidth = rightBorderWidth = bottomBorderWidth = leftBorderWidth = BORDER_WIDTH_BLOCK;
            topBorderColor = rightBorderColor = bottomBorderColor = leftBorderColor = BORDER_COLOR_ERROR;

        } else {
            // Estilo de borde normal (delgado gris, más grueso negro en límites de bloque)
            topBorderWidth = BORDER_WIDTH_NORMAL; // Borde superior siempre normal (o BORDER_WIDTH_BLOCK si row == 0?) - dejémoslo normal
            rightBorderWidth = (col + 1) % BLOCK_COLS == 0 ? BORDER_WIDTH_BLOCK : BORDER_WIDTH_NORMAL;
            bottomBorderWidth = (row + 1) % BLOCK_ROWS == 0 ? BORDER_WIDTH_BLOCK : BORDER_WIDTH_NORMAL;
            leftBorderWidth = BORDER_WIDTH_NORMAL; // Borde izquierdo siempre normal (o BORDER_WIDTH_BLOCK si col == 0?) - dejémoslo normal

            topBorderColor = BORDER_COLOR_NORMAL;
            rightBorderColor = (col + 1) % BLOCK_COLS == 0 ? BORDER_COLOR_BLOCK : BORDER_COLOR_NORMAL;
            bottomBorderColor = (row + 1) % BLOCK_ROWS == 0 ? BORDER_COLOR_BLOCK : BORDER_COLOR_NORMAL;
            leftBorderColor = BORDER_COLOR_NORMAL;
        }

        // Añadir estilos de borde al builder
        styleBuilder.append(" -fx-border-width: ").append(topBorderWidth).append(" ").append(rightBorderWidth).append(" ").append(bottomBorderWidth).append(" ").append(leftBorderWidth).append(";");
        styleBuilder.append(" -fx-border-color: ").append(topBorderColor).append(" ").append(rightBorderColor).append(" ").append(bottomBorderColor).append(" ").append(leftBorderColor).append(";");
        styleBuilder.append(" ").append(BORDER_STYLE_SOLID).append(";"); // Asegurar punto y coma

        // 3. Background Style (Determinado por resaltado de hover)
        if (isHighlighted) {
            styleBuilder.append(" -fx-background-color: ").append(HIGHLIGHT_BACKGROUND_COLOR).append(";");
        } else {
            // Fondo transparente por defecto si no está resaltado
            styleBuilder.append(" -fx-background-color: transparent;");
            // Fondo blanco para TextFields:
             if (node instanceof TextField) {
                 styleBuilder.append(" -fx-background-color: transparent;");
             }
        }

        // Aplicar el estilo final al nodo
        node.setStyle(styleBuilder.toString().trim());
    }

    /**
     * Manejador para cuando el ratón entra en una celda.
     * Calcula las nuevas celdas a resaltar y actualiza sus estilos.
     * @param event El evento del ratón.
     */
    private void handleMouseEntered(MouseEvent event) {
        Node sourceNode = (Node) event.getSource();
        int[] coords = getClickedCellCoords(sourceNode);
        if (coords == null) return; // No se pudo identificar la celda

        int enterRow = coords[0];
        int enterCol = coords[1];

        // 1. Recordar qué celdas estaban resaltadas antes
        Set<Pair<Integer, Integer>> oldHighlights = new HashSet<>(this.currentlyHighlightedCoords);

        // 2. Limpiar el estado de resaltado actual
        this.currentlyHighlightedCoords.clear();

        // 3. Calcular y establecer el nuevo estado de resaltado (fila y columna)
        // Añadir toda la fila
        for (int c = 0; c < GRID_SIZE; c++) {
            this.currentlyHighlightedCoords.add(new Pair<>(enterRow, c));
        }
        // Añadir toda la columna
        for (int r = 0; r < GRID_SIZE; r++) {
            this.currentlyHighlightedCoords.add(new Pair<>(r, enterCol));
        }

        // 4. Determinar todas las celdas afectadas (las que dejaron de estar resaltadas + las nuevas)
        Set<Pair<Integer, Integer>> affectedCoords = new HashSet<>(oldHighlights);
        affectedCoords.addAll(this.currentlyHighlightedCoords); // Unión de viejas y nuevas

        // 5. Actualizar el estilo de todas las celdas afectadas
        for (Pair<Integer, Integer> coord : affectedCoords) {
            this.updateCellStyle(coord.getKey(), coord.getValue());
        }
    }

    /**
     * Manejador para cuando el ratón sale de una celda.
     * Limpia el estado de resaltado y actualiza los estilos de las celdas que estaban resaltadas.
     * @param event El evento del ratón.
     */
    private void handleMouseExited(MouseEvent event) {
        // 1. Recordar qué celdas estaban resaltadas
        Set<Pair<Integer, Integer>> oldHighlights = new HashSet<>(this.currentlyHighlightedCoords);

        // 2. Limpiar el estado de resaltado
        this.currentlyHighlightedCoords.clear();

        // 3. Actualizar el estilo de las celdas que *estaban* resaltadas para quitarles el fondo
        for (Pair<Integer, Integer> coord : oldHighlights) {
            this.updateCellStyle(coord.getKey(), coord.getValue());
        }
    }

    private static class GameViewHolder {
        private static GameView INSTANCE;
    }

    public static GameView getInstance() throws IOException {
        if (GameViewHolder.INSTANCE == null) {
            GameViewHolder.INSTANCE = new GameView();
            return GameViewHolder.INSTANCE;
        } else {
            return GameViewHolder.INSTANCE;
        }
    }
}