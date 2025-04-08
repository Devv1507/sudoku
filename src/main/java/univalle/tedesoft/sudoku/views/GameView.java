package univalle.tedesoft.sudoku.views;

import univalle.tedesoft.sudoku.Main;
import univalle.tedesoft.sudoku.controllers.GameController;
import univalle.tedesoft.sudoku.models.Board;
import univalle.tedesoft.sudoku.models.Cell;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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

    // Referencias
    private final GridPane sudokuGridPane;
    private final GameController controller;

    // Estado Interno de la Vista
    private Node[][] nodeGrid = new Node[GRID_SIZE][GRID_SIZE]; // Cache de nodos UI para acceso rápido
    private TextField currentEditingTextField = null; // Campo de texto activo actualmente
    private Set<Pair<Integer, Integer>> currentErrorCoords = new HashSet<>(); // Coords con error resaltado

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

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell cellData = gridData[row][col];
                Node cellNode;

                if (!cellData.getEditable()) {
                    // Celda Fija: Crear un Label
                    Label label = new Label(String.valueOf(cellData.getValue()));
                    label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    applyBaseCellStyle(label, row, col); // Estilo base (bordes, fuente normal)
                    label.setStyle(label.getStyle() + STYLE_FONT_BOLD); // Añadir negrita
                    cellNode = label;
                } else {
                    // Celda Editable
                    if (cellData.getValue() != 0) {
                        // Con valor inicial: Crear TextField prellenado
                        TextField tf = createTextField(row, col); // Pasa coords para listeners
                        tf.setText(String.valueOf(cellData.getValue()));
                        cellNode = tf;
                    } else {
                        // Vacía: Crear un Pane placeholder cliqueable
                        Pane placeholder = createPlaceholderPane(row, col);
                        cellNode = placeholder;
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
        // Después de renderizar, aplicar el resaltado de errores actual (si lo hay)
        highlightErrors(this.currentErrorCoords);
    }

    /**
     * Actualiza los estilos de las celdas para mostrar cuáles tienen errores.
     * Elimina el resaltado de errores antiguos y aplica el nuevo.
     * @param newErrorCoords Conjunto de coordenadas (fila, columna) de las celdas con errores.
     */
    public void highlightErrors(Set<Pair<Integer, Integer>> newErrorCoords) {
        // 1. Quitar resaltado de errores que ya NO existen
        Set<Pair<Integer, Integer>> errorsToRemove = new HashSet<>(this.currentErrorCoords);
        errorsToRemove.removeAll(newErrorCoords); // Celdas que estaban en error pero ya no
        for (Pair<Integer, Integer> coord : errorsToRemove) {
            Node node = nodeGrid[coord.getKey()][coord.getValue()];
            if (node != null) {
                restoreBaseStyle(node, coord.getKey(), coord.getValue());
            }
        }

        // 2. Añadir resaltado a NUEVOS errores
        Set<Pair<Integer, Integer>> errorsToAdd = new HashSet<>(newErrorCoords);
        errorsToAdd.removeAll(this.currentErrorCoords); // Celdas que no estaban en error y ahora sí
        for (Pair<Integer, Integer> coord : errorsToAdd) {
            Node node = nodeGrid[coord.getKey()][coord.getValue()];
            if (node != null) {
                applyErrorStyle(node); // Aplica el estilo rojo/grueso
            }
        }
        // Las celdas que SIGUEN en error ya tienen el estilo, no se tocan.

        // 3. Actualizar el conjunto de errores actual de la vista
        this.currentErrorCoords = new HashSet<>(newErrorCoords);
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
            if (currentEditingTextField != null) {
                // Mover foco a la grilla como contenedor
                sudokuGridPane.requestFocus();
                currentEditingTextField = null;
            }
            return; // Ignorar este clic
        }

        int row = coords[0];
        int col = coords[1];

        // Informar al controlador que se hizo clic en esta celda (puede necesitarlo)
        controller.cellClicked(row, col);

        // --- Lógica de UI para manejar la edición ---
        Node targetNodeInGrid = nodeGrid[row][col]; // Nodo actual en esa posición según nuestra caché

        if (targetNodeInGrid instanceof Pane) {
            // Clic en un placeholder (celda editable vacía) -> Convertir a TextField
            switchToTextField(targetNodeInGrid, row, col);

        } else if (targetNodeInGrid instanceof TextField) {
            // Clic en un TextField existente
            if (currentEditingTextField != targetNodeInGrid) {
                // Si se estaba editando otro campo, quitarle el foco
                if (currentEditingTextField != null) {
                    sudokuGridPane.requestFocus(); // Mover foco fuera del campo anterior
                }
                // Dar foco al campo clickeado
                targetNodeInGrid.requestFocus();
                currentEditingTextField = (TextField) targetNodeInGrid;
            }
            // Si se clickea en el mismo campo que ya tiene foco, no hacer nada

        } else { // Clic en Label (fijo) u otro nodo no editable
            // Si se estaba editando un campo, quitarle el foco
            if (currentEditingTextField != null) {
                sudokuGridPane.requestFocus(); // Mover foco fuera
                currentEditingTextField = null;
            }
        }
    }

    /**
     * Reemplaza un nodo (presumiblemente un Pane) con un TextField editable en la misma celda.
     * @param nodeToReplace El nodo (Pane) a quitar.
     * @param row La fila.
     * @param col La columna.
     */
    private void switchToTextField(Node nodeToReplace, int row, int col) {
        // Si se estaba editando otro campo, quitarle el foco primero
        if (currentEditingTextField != null && currentEditingTextField != nodeToReplace) {
            sudokuGridPane.requestFocus();
        }

        // Crear el nuevo TextField
        TextField textField = createTextField(row, col);

        // Reemplazar el nodo en el GridPane
        sudokuGridPane.getChildren().remove(nodeToReplace);
        GridPane.setRowIndex(textField, row);
        GridPane.setColumnIndex(textField, col);
        sudokuGridPane.getChildren().add(textField);

        // Actualizar la caché de nodos de la vista
        nodeGrid[row][col] = textField;

        // Dar foco y marcar como campo en edición
        textField.requestFocus();
        currentEditingTextField = textField;

        // Aplicar estilo de error si corresponde (la celda estaba vacía, no debería tener error, pero por si acaso)
        if (currentErrorCoords.contains(new Pair<>(row, col))) {
            applyErrorStyle(textField);
        }
    }

    /**
     * Reemplaza un TextField vacío que ha perdido el foco con un Pane placeholder.
     * @param textField El TextField a reemplazar.
     * @param row La fila.
     * @param col La columna.
     */
    private void replaceTextFieldWithPlaceholder(TextField textField, int row, int col) {
        // Asegurarse de que el nodo correcto está siendo reemplazado
        if (nodeGrid[row][col] == textField) {
            sudokuGridPane.getChildren().remove(textField);
            Pane placeholder = createPlaceholderPane(row, col);
            GridPane.setRowIndex(placeholder, row);
            GridPane.setColumnIndex(placeholder, col);
            sudokuGridPane.getChildren().add(placeholder);
            nodeGrid[row][col] = placeholder; // Actualizar caché

            // Restaurar estilo base (si tenía error, quitarlo)
            restoreBaseStyle(placeholder, row, col);
        }
        // Si este era el campo activo, ya no lo es
        if(currentEditingTextField == textField) {
            currentEditingTextField = null;
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
        applyBaseCellStyle(textField, row, col); // Estilo base inicial
        textField.setPrefSize(40, 40);
        textField.setMaxSize(40, 40);
        textField.setUserData(new int[]{row, col}); // Guardar coords para identificación

        // Filtro para aceptar solo números del 1 al 6 o vacío
        Pattern validEditingState = Pattern.compile("^[1-6]?$");
        UnaryOperator<TextFormatter.Change> filter = change ->
                validEditingState.matcher(change.getControlNewText()).matches() ? change : null;
        textField.setTextFormatter(new TextFormatter<>(filter));

        // Listener: Cambio de Texto -> INFORMAR AL CONTROLADOR
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            controller.cellValueChanged(row, col, newValue); // Delegar al controlador
            // La vista podría reaccionar visualmente aquí si fuera necesario (ej. cambiar color mientras se escribe)
        });

        // Listener: Pérdida de Foco -> Reemplazar si está vacío
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && textField.getText().isEmpty()) {
                // Perdió foco y está vacío -> Reemplazar por placeholder
                replaceTextFieldWithPlaceholder(textField, row, col);
                // No es necesario notificar al controlador de nuevo, cellValueChanged ya lo hizo con ""
            } else if (!isNowFocused && currentEditingTextField == textField) {
                // Perdió foco pero no está vacío, asegurarse que ya no es el "activo"
                currentEditingTextField = null;
            } else if (isNowFocused) {
                // Ganó foco, marcarlo como el activo
                currentEditingTextField = textField;
            }
        });

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
        // Estilo base
        applyBaseCellStyle(placeholder, row, col);
        // Coordenadas para identificarlo al hacer clic
        placeholder.setUserData(new int[]{row, col});
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
     * Aplica los estilos base (bordes, fuente, alineación) a un nodo de celda.
     * Usado para inicializar y para quitar el resaltado de error.
     */
    private void applyBaseCellStyle(Node node, int row, int col) {
        StringBuilder style = new StringBuilder();
        // Estilos comunes de fuente/alineación
        if (node instanceof Labeled) {
            ((Labeled) node).setAlignment(Pos.CENTER);
            style.append(STYLE_FONT_SIZE);
            // Quitamos la negrita por defecto aquí, se añade explícitamente para Labels fijos
            node.setStyle(node.getStyle().replace(STYLE_FONT_BOLD, "")); // Asegurar que no esté
        } else if (node instanceof Pane) {
            node.setStyle("-fx-background-color: transparent;"); // Fondo transparente
            ((Pane) node).setPrefSize(40, 40); // Tamaño deseado
        }

        // Calcular bordes normales y de bloque
        String topBorder = BORDER_WIDTH_NORMAL;
        String rightBorder = (col + 1) % BLOCK_COLS == 0 ? BORDER_WIDTH_BLOCK : BORDER_WIDTH_NORMAL;
        String bottomBorder = (row + 1) % BLOCK_ROWS == 0 ? BORDER_WIDTH_BLOCK : BORDER_WIDTH_NORMAL;
        String leftBorder = BORDER_WIDTH_NORMAL;
        String topColor = BORDER_COLOR_NORMAL;
        String rightColor = (col + 1) % BLOCK_COLS == 0 ? BORDER_COLOR_BLOCK : BORDER_COLOR_NORMAL;
        String bottomColor = (row + 1) % BLOCK_ROWS == 0 ? BORDER_COLOR_BLOCK : BORDER_COLOR_NORMAL;
        String leftColor = BORDER_COLOR_NORMAL;

        // Construir estilos de borde
        style.append("-fx-border-width: ").append(topBorder).append(" ").append(rightBorder).append(" ").append(bottomBorder).append(" ").append(leftBorder).append("; ");
        style.append("-fx-border-color: ").append(topColor).append(" ").append(rightColor).append(" ").append(bottomColor).append(" ").append(leftColor).append("; ");
        style.append(BORDER_STYLE_SOLID);

        // Aplicar el estilo completo
        node.setStyle(style.toString());
    }

    /**
     * Aplica el estilo visual de error a un nodo (borde rojo grueso).
     * Mantiene la fuente/alineación base si aplica.
     */
    private void applyErrorStyle(Node node) {
        StringBuilder style = new StringBuilder();
        // Mantener estilos base de fuente/alineación
        if (node instanceof Label) { // Label fijo (no debería tener error, pero por completitud)
            style.append(STYLE_FONT_SIZE).append(STYLE_FONT_BOLD).append(STYLE_ALIGNMENT_CENTER);
        } else if (node instanceof TextField) { // TextField editable
            style.append(STYLE_FONT_SIZE).append(STYLE_ALIGNMENT_CENTER);
        } else if (node instanceof Pane) { // Placeholder (editable vacío)
            style.append("-fx-background-color: transparent;");
            ((Pane) node).setPrefSize(40, 40);
        }

        // Añadir borde de error (rojo y grueso)
        style.append("-fx-border-color: ").append(BORDER_COLOR_ERROR).append("; ")
                .append("-fx-border-width: ").append(BORDER_WIDTH_BLOCK).append("; ") // Borde grueso para error
                .append(BORDER_STYLE_SOLID);

        node.setStyle(style.toString());
    }

    /**
     * Restaura el estilo base de una celda, asegurándose de quitar el resaltado de error
     * y de reaplicar la negrita si era un Label fijo.
     */
    private void restoreBaseStyle(Node node, int row, int col) {
        applyBaseCellStyle(node, row, col); // Aplica bordes y fuente/alineación normales

        // Si el nodo es un Label, debemos verificar si era fijo (no editable) para ponerle negrita.
        if (node instanceof Label) {
            // Consultar al controlador si la celda original era editable o no
            boolean isEditable = controller.isCellEditable(row, col);
            if (!isEditable) {
                // Era fija, restaurar negrita además del estilo base
                node.setStyle(node.getStyle() + STYLE_FONT_BOLD);
            }
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