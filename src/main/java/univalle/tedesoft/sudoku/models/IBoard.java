package univalle.tedesoft.sudoku.models;

/**
 * Define el tablero completo de Sudoku (6x6).
 * Establece las operaciones fundamentales como inicialización, acceso y modificación a celdas/bloques.
 * @author David Valencia
 * @author Santiago Guerrero
 */
public interface IBoard {
    /**
     * Inicializa o reinicializa el tablero con un nuevo puzzle de Sudoku.
     */
    void initializeBoard();

    /**
     * Obtiene una representación (snapshot) de la cuadrícula actual del tablero.
     * La implementación debe devolver una copia para evitar modificaciones externas no deseadas.
     * @return Una nueva matriz de celdas representando el estado actual del tablero.
     */
    Cell[][] getGridSnapshot();

    /**
     * Obtiene la interfaz ICell de la celda en la posición global especificada.
     * @param row La fila global (0 a GRID_SIZE - 1).
     * @param col La columna global (0 a GRID_SIZE - 1).
     * @return La celda en esa posición.
     * @throws IndexOutOfBoundsException si los índices están fuera del rango.
     */
    ICell getCell(int row, int col);

    /**
     * Establece el valor de una celda en la posición global especificada, si es editable.
     * @param row   La fila global (0 a GRID_SIZE - 1).
     * @param col   La columna global (0 a GRID_SIZE - 1).
     * @param value El valor a establecer (0 para vacío, 1-GRID_SIZE).
     * @return true si el valor fue establecido, false en caso contrario (cuando no es editable).
     * @throws IndexOutOfBoundsException si los índices están fuera del rango.
     * @throws IllegalArgumentException si el valor no está entre el tamaño máximo de la grilla.
     */
    boolean setCellValue(int row, int col, int value);

    /**
     * Obtiene el bloque que contiene la celda en la posición global especificada.
     * @param row Fila global (0 a GRID_SIZE - 1).
     * @param col Columna global (0 a GRID_SIZE - 1).
     * @return el bloque correspondiente.
     * @throws IndexOutOfBoundsException si los índices están fuera de rango.
     */
    IBlock getBlockAt(int row, int col);

    /**
     * Obtiene un bloque específico por sus índices de bloque.
     * @param blockRow Indice de fila del bloque (0 a NUM_BLOCK_ROWS - 1).
     * @param blockCol Indice de columna del bloque (0 a NUM_BLOCK_COLS - 1).
     * @return el bloque solicitado.
     * @throws IndexOutOfBoundsException si los índices de bloque están fuera de rango.
     */
    Block getBlock(int blockRow, int blockCol);

    /**
     * Cuenta el número de celdas vacías (valor 0) que son actualmente editables en el tablero.
     * @return El número de celdas editables y vacías.
     */
    int countEmptyEditableCells();

}