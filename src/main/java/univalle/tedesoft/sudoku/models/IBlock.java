package univalle.tedesoft.sudoku.models;

import javafx.util.Pair;
import java.util.Set;

/**
 * Interfaz para un bloque (subcuadrícula de 2x3) dentro del tablero de Sudoku.
 * Permite acceder a sus celdas y validar su contenido interno.
 * @author David Valencia
 * @author Santiago Guerrero
 */
public interface IBlock {
    /**
     * Obtiene la celda en la posición local especificada dentro de este bloque.
     * @param row La fila local dentro del bloque (0 a BLOCK_ROWS - 1).
     * @param col La columna local dentro del bloque (0 a BLOCK_COLS - 1).
     * @return La celda en la posición especificada.
     * @throws IndexOutOfBoundsException si los índices locales están fuera de rango.
     */
    Cell getCell(int row, int col);

    /**
     * Encuentra y devuelve las coordenadas locales de las celdas repetidas dentro del bloque.
     * Una celda se considera inválida si su valor se repite dentro del bloque.
     * @return Un conjunto de pares representando las coordenadas locales (fila, columna)
     * de las celdas inválidas dentro del bloque.
     */
    Set<Pair<Integer, Integer>> getInvalidCellsInBlock();
}