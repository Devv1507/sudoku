package univalle.tedesoft.sudoku.models;

import javafx.util.Pair;
import java.util.Set;

/**
 * Evalua el estado del juego Sudoku.
 * Permite verificar la validez del tablero, si está completo, si se ha ganado
 * y obtener sugerencias o pistas.
 * @author David Valencia
 * @author Santiago Guerrero
 */
public interface IGameState {

    /**
     * Verifica si el tablero completo es válido según las reglas del Sudoku.
     * Una tabla es válida si no hay números repetidos (distintos de 0) en ninguna fila,
     * columna o bloque.
     * @return true si el tablero es actualmente válido, false si contiene errores.
     */
    boolean isBoardValid();

    /**
     * Encuentra y devuelve todas las coordenadas globales de las celdas que actualmente
     * violan las reglas del Sudoku (duplicados en fila, columna o bloque).
     * @return Un conjunto de pares (Pair<Integer, Integer>) representando las coordenadas globales
     *         (fila, columna) de las celdas inválidas. Si el tablero es válido, el conjunto estará vacío.
     */
    Set<Pair<Integer, Integer>> getInvalidCells();

    /**
     * Verifica si el tablero está completamente lleno (sin celdas vacías con valor 0).
     * @return true si todas las celdas tienen un valor distinto de 0, false en caso contrario.
     */
    boolean isBoardFull();

    /**
     * Comprueba si el juego ha sido completado exitosamente.
     * Esto requiere que el tablero esté completamente lleno y sea válido.
     * @return true si el Sudoku está resuelto correctamente, false en caso contrario.
     */
    boolean isGameWon();

    /**
     * Sugiere un número válido que podría colocarse en una celda editable vacía específica.
     * La sugerencia se basa en encontrar un número que no viole
     * inmediatamente las reglas de Sudoku en esa fila, columna y bloque.
     *
     * @param row La fila de la celda (0 a GRID_SIZE - 1).
     * @param col La columna de la celda (0 a GRID_SIZE - 1).
     * @return Un número válido sugerido si la celda es editable, está vacía y
     * se encuentra una sugerencia; devuelve 0 en caso contrario (celda no editable,
     * no vacía, o ninguna sugerencia simple encontrada).
     */
    int getClue (int row, int col);
}