package univalle.tedesoft.sudoku.models;

/**
 * Interfaz de celda individual en el tablero de Sudoku.
 * Expone métodos para obtener y establecer su valor y estado de edición.
 * @author David Valencia
 * @author Santiago Guerrero
 */
public interface ICell {

    /**
     * Obtiene el valor numérico actual de la celda.
     * @return El valor de la celda (0 si está vacía, 1-6 en caso contrario).
     */
    int getValue();

    /**
     * Establece un nuevo valor para la celda, si es editable.
     * Si la celda no es editable, la implementación no puede cambiar el valor.
     * @param value El nuevo valor (0-6). La implementación debería validar este rango.
     * @throws IllegalArgumentException si el valor está fuera del rango permitido (0-6).
     */
    void setValue(int value);

    /**
     * Verifica si la celda es editable por el usuario.
     * @return true si la celda es editable, false en caso contrario.
     */
    boolean getEditable();

    /**
     * Establece si la celda debe ser editable o no.
     * @param isEditable true para hacerla editable, false para hacerla fija.
     */
    void setEditable(boolean isEditable);
}