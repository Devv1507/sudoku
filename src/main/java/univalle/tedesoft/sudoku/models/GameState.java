package univalle.tedesoft.sudoku.models;

import java.util.HashSet;
import java.util.Set;

import javafx.util.Pair;

/**
 * Clase responsable de validar el estado actual del tablero de Sudoku.
 * Utiliza los objetos `Block` del tablero para la validación de bloques.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 */
public class GameState implements IGameState {
    /**
     * Referencia al tablero de Sudoku que se está validando.
     * @see Board
     */
    private final Board board;

    /**
     * Constructor que asocia este estado del juego con un tablero específico.
     * @param board El tablero de Sudoku a validar.
     */
    public GameState(Board board) {
        this.board = board;
    }

    /**
     * Verifica si el tablero completo es válido según las reglas del Sudoku.
     * @return true si no hay números repetidos en ninguna fila, columna o bloque, false si hay algún error.
     */
    public boolean isBoardValid() {
        return this.getInvalidCells().isEmpty();
    }

    /**
     * Encuentra y devuelve todas las coordenadas globales de las celdas que violan las reglas del Sudoku.
     * Una celda se considera inválida si su valor (distinto de 0) se repite en su fila, columna o bloque.
     * @return Un conjunto de pares (Pair<Integer, Integer>) representando las coordenadas globales (fila, columna)
     * de las celdas inválidas. Si el tablero es válido, el conjunto estará vacío.
     */
    public Set<Pair<Integer, Integer>> getInvalidCells() {
        Set<Pair<Integer, Integer>> invalidCells = new HashSet<>();
        int size = Board.GRID_SIZE;
        // Obtener snapshot para asegurar consistencia durante la validación
        Cell[][] grid = this.board.getGridSnapshot();

        // 1. Validar Filas
        for (int row = 0; row < size; row++) {
            Set<Integer> seenInRow = new HashSet<>();
            Set<Pair<Integer, Integer>> rowDuplicates = new HashSet<>();
            for (int column = 0; column < size; column++) {
                int value = grid[row][column].getValue();
                if (value != 0) {
                    if (!seenInRow.add(value)) {
                        rowDuplicates.add(new Pair<>(row, column));
                        for(int prevC = 0; prevC < column; prevC++) {
                            if(grid[row][prevC].getValue() == value) {
                                rowDuplicates.add(new Pair<>(row, prevC));
                            }
                        }
                    }
                }
            }
            invalidCells.addAll(rowDuplicates);
        }

        // 2. Validar Columnas
        for (int column = 0; column < size; column++) {
            Set<Integer> seenInCol = new HashSet<>();
            Set<Pair<Integer, Integer>> colDuplicates = new HashSet<>();
            for (int row = 0; row < size; row++) {
                int value = grid[row][column].getValue();
                if (value != 0) {
                    if (!seenInCol.add(value)) {
                        colDuplicates.add(new Pair<>(row, column));
                        for(int prevR = 0; prevR < row; prevR++) {
                            if(grid[prevR][column].getValue() == value) {
                                colDuplicates.add(new Pair<>(prevR, column));
                            }
                        }
                    }
                }
            }
            invalidCells.addAll(colDuplicates);
        }

        // 3. Validar Bloques usando los objetos Block del Board
        for (int br = 0; br < Board.NUM_BLOCK_ROWS; br++) {
            for (int bc = 0; bc < Board.NUM_BLOCK_COLS; bc++) {
                Block block = board.getBlock(br, bc); // Obtener el bloque
                // Obtener celdas inválidas DENTRO del bloque (coordenadas locales)
                Set<Pair<Integer, Integer>> invalidLocalCells = block.getInvalidCellsInBlock();

                // Convertir coordenadas locales a globales y añadirlas al conjunto general
                int startRow = block.getStartRow();
                int startCol = block.getStartCol();
                for (Pair<Integer, Integer> localCoord : invalidLocalCells) {
                    int globalRow = startRow + localCoord.getKey();
                    int globalCol = startCol + localCoord.getValue();
                    invalidCells.add(new Pair<>(globalRow, globalCol));
                }
            }
        }

        return invalidCells;
    }

    /**
     * Verifica si el tablero está completamente lleno (sin celdas vacías).
     * @return true si todas las celdas tienen un valor distinto de 0, false en caso contrario.
     */
    public boolean isBoardFull() {
        // Podemos usar el grid del board directamente aquí, ya que solo leemos valores.
        for (int row = 0; row < Board.GRID_SIZE; row++) {
            for (int col = 0; col < Board.GRID_SIZE; col++) {
                if (this.board.getCell(row, col).getValue() == 0) {
                    return false; // Encontró una celda vacía
                }
            }
        }
        return true; // No se encontraron celdas vacías
    }

    /**
     * Comprueba si el juego ha sido completado exitosamente.
     * El tablero debe estar lleno y ser válido.
     * @return true si el Sudoku está resuelto, false en caso contrario.
     */
    public boolean isGameWon() {
        if (this.isBoardFull() && this.isBoardValid()) {
            return true;
            //Agregue esto por unos errores que me exigian una segunda condicio, se puede borrar si se encuentra otra solucion
        }else{
            return false;
        }
    }

    /**
     * Sugiere un número válido para una celda vacía específica.
     * @param row La fila de la celda vacía.
     * @param col La columna de la celda vacía.
     * @return Un número válido (1-6) que puede ir en esa celda, o 0 si no procede.
     */
    public int getClue(int row, int col) {
        Cell cell = this.board.getCell(row, col);
        if (!cell.getEditable() || cell.getValue() != 0) {
            return 0;
        }
        for (int num = 1; num <= Board.GRID_SIZE; num++) {
            if (this.isPlacementPotentiallyValid(row, col, num)) {
                return num;
            }
        }
        return 0;
    }

    /**
     * Función auxiliar para verificar si un número *podría* ser colocado
     * en una celda vacía sin violar inmediatamente las reglas.
     */
    private boolean isPlacementPotentiallyValid(int row, int col, int num) {
        // Usa el estado actual del board directamente
        // Comprobar fila
        for (int c = 0; c < Board.GRID_SIZE; c++) {
            if (this.board.getCell(row,c).getValue() == num) {
                return false;
            }
        }
        // Comprobar columna
        for (int r = 0; r < Board.GRID_SIZE; r++) {
            if (this.board.getCell(r,col).getValue() == num) {
                return false;
            }
        }
        // Comprobar bloque
        Block block = this.board.getBlockAt(row, col); // Obtener el bloque relevante
        for(int r=0; r<Block.BLOCK_ROWS; r++){
            for(int c=0; c<Block.BLOCK_COLS; c++){
                if(block.getCell(r, c).getValue() == num) {
                    return false;
                }
            }
        }
        return true;
    }
}