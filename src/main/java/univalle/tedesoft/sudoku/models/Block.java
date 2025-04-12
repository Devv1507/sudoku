package univalle.tedesoft.sudoku.models;

import java.util.HashSet;
import java.util.Set;

import javafx.util.Pair;

/**
 * Representa un bloque de 2x3 dentro del tablero de Sudoku 6x6.
 * Contiene referencias a las celdas de este bloque y puede validar su contenido interno.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 */
public class Block implements IBlock {
    /**
     * Constante del número de filas en un bloque.
     * En este caso, 2 filas.
     */
    public static final int BLOCK_ROWS = 2;
    /**
     * Constante del número de columnas en un bloque.
     * En este caso, 3 columnas.
     */
    public static final int BLOCK_COLS = 3;

    /**
     * Matriz de referencias a las celdas del tablero principal que pertenecen a este bloque.
     * Cada bloque tiene 2 filas y 3 columnas.
     * @see Cell
     */
    private final Cell[][] cells = new Cell[BLOCK_ROWS][BLOCK_COLS];
    /**
     * Coordenadas de inicio de la fila de este bloque en el tablero principal (6x6).
     */
    private final int startRow;
    /**
     * Coordenadas de inicio de la columna de este bloque en el tablero principal (6x6).
     */
    private final int startCol;

    /**
     * Constructor para un bloque de Sudoku.
     * @param boardGrid La cuadrícula completa de celdas del tablero principal.
     * @param startRow  La fila inicial de este bloque en el tablero principal. En este caso, 0, 2 y 4.
     * @param startCol  La columna inicial de este bloque en el tablero principal. En este caso, 0 y 3.
     * @throws IndexOutOfBoundsException si los índices de inicio o la cuadrícula del tablero son inválidos.
     */
    public Block(Cell[][] boardGrid, int startRow, int startCol) {
        if (startRow < 0 || startRow + BLOCK_ROWS > Board.GRID_SIZE || startCol < 0 || startCol + BLOCK_COLS > Board.GRID_SIZE) {
            throw new IndexOutOfBoundsException("Coordenadas de inicio de bloque inválidas: (" + startRow + ", " + startCol + ")");
        }
        if (boardGrid == null || boardGrid.length != Board.GRID_SIZE || boardGrid[0].length != Board.GRID_SIZE) {
             throw new IllegalArgumentException("La cuadrícula del tablero proporcionada es inválida.");
        }

        // Llenar la matriz local 'cells' con referencias a las celdas correspondientes del 'boardGrid'.
        for (int row = 0; row < BLOCK_ROWS; row++) {
            for (int col = 0; col < BLOCK_COLS; col++) {
                this.cells[row][col] = boardGrid[startRow + row][startCol + col];
            }
        }

        this.startRow = startRow;
        this.startCol = startCol;
    }

    /**
     * Obtiene la celda en la posición local especificada dentro de este bloque.
     * @param localRow La fila local dentro del bloque (0 a BLOCK_ROWS - 1).
     * @param localCol La columna local dentro del bloque (0 a BLOCK_COLS - 1).
     * @return El valor de la celda en la posición especificada.
     * @throws IndexOutOfBoundsException si los índices locales están fuera de rango.
     */
    public Cell getCell(int localRow, int localCol) {
        if (localRow < 0 || localRow >= BLOCK_ROWS || localCol < 0 || localCol >= BLOCK_COLS) {
            throw new IndexOutOfBoundsException("Índices locales de celda fuera de rango en bloque: (" + localRow + ", " + localCol + ")");
        }
        return this.cells[localRow][localCol];
    }

    /**
     * Verifica si los números dentro de este bloque son válidos (sin repeticiones, ignorando 0).
     * @return true si el bloque es válido, false si contiene números repetidos (distintos de 0).
     */
    public boolean isValid() {
        return this.getInvalidCellsInBlock().isEmpty();
    }

    /**
     * Encuentra y devuelve las coordenadas locales de las celdas que violan la regla de unicidad dentro de este bloque.
     * Una celda se considera inválida si su valor (distinto de 0) se repite dentro del bloque.
     * @return Un conjunto de pares (Pair<Integer, Integer>) representando las coordenadas locales (fila, columna)
     *         de las celdas inválidas dentro del bloque. Si el bloque es válido, el conjunto estará vacío.
     */
    public Set<Pair<Integer, Integer>> getInvalidCellsInBlock() {
        Set<Pair<Integer, Integer>> invalidCellsLocal = new HashSet<>();
        Set<Integer> seen = new HashSet<>();

        for (int row = 0; row < BLOCK_ROWS; row++) {
            for (int col = 0; col < BLOCK_COLS; col++) {
                int value = this.cells[row][col].getValue();
                if (value != 0) {
                    if (!seen.add(value)) { // Si add devuelve false, el valor ya existía en este bloque
                        // Marcar esta celda como duplicada
                        invalidCellsLocal.add(new Pair<>(row, col));
                        // Marcar también la(s) celda(s) anterior(es) con el mismo número en este bloque
                        for (int prev_row = 0; prev_row <= row; prev_row++) {
                             int prev_column_limit = (prev_row == row) ? col : BLOCK_COLS; // Solo hasta c en la misma fila
                            for (int prev_column = 0; prev_column < prev_column_limit; prev_column++) {
                                if (cells[prev_row][prev_column].getValue() == value) {
                                    invalidCellsLocal.add(new Pair<>(prev_row, prev_column));
                                }
                            }
                        }
                    }
                }
            }
        }
        return invalidCellsLocal;
    }

    /**
     * Obtiene la fila inicial de este bloque en el tablero global.
     * @return la fila inicial (0, 2 o 4).
     */
    public int getStartRow() {
        return this.startRow;
    }

    /**
     * Obtiene la columna inicial de este bloque en el tablero global.
     * @return la columna inicial (0 o 3).
     */
    public int getStartCol() {
        return this.startCol;
    }

    // @Override
    // public String toString() {
    //     StringBuilder sb = new StringBuilder("Block[(" + startRow + "," + startCol + ")]:\n");
    //     for (int r = 0; r < BLOCK_ROWS; r++) {
    //         sb.append("  | ");
    //         for (int c = 0; c < BLOCK_COLS; c++) {
    //             Cell cell = cells[r][c];
    //             char editableMarker = cell.isEditable() ? ' ' : '*';
    //             String valueStr = cell.getValue() == 0 ? "X" : String.valueOf(cell.getValue());
    //             sb.append(valueStr).append(editableMarker).append(" ");
    //         }
    //         sb.append("|\n");
    //     }
    //     return sb.toString();
    // }
}