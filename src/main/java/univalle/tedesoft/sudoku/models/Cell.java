package univalle.tedesoft.sudoku.models;

/**
 * Esta clase representa una única celda en el tablero de Sudoku.
 * Contiene su valor numérico (0 si está vacía) y si es editable por el jugador.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 */
public class Cell implements ICell {
    private int value;
    private boolean isEditable;

    /**
     * Constructor por defecto. Crea una celda vacía (valor 0) y editable.
     */
    public Cell() {
        this.value = 0;
        this.isEditable = true;
    }

    /**
     * Constructor para crear una celda con un valor y estado de edición iniciales.
     * @param value El valor inicial (0-6).
     * @param editable Si la celda debe ser editable.
     * @throws IllegalArgumentException si el valor no está entre 0 y 6.
     * @see IllegalArgumentException
     */
    public Cell(int value, boolean editable) {
        if (value < 0 || value > Board.GRID_SIZE) {
             throw new IllegalArgumentException("El valor de la celda debe estar entre 0 y " + Board.GRID_SIZE);
        }
        this.value = value;
        this.isEditable = editable;
    }

    /**
     * Obtiene el valor actual de la celda.
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Establece un nuevo valor para la celda.
     * @param value El nuevo valor (0-6).
     * @throws IllegalArgumentException si el valor no está entre 0 y 6.
     * @see IllegalArgumentException
     */
    public void setValue(int value) {
         if (value < 0 || value > Board.GRID_SIZE) {
             throw new IllegalArgumentException("El valor de la celda debe estar entre 0 y " + Board.GRID_SIZE);
         }
        if (this.isEditable) {
            this.value = value;
        }
    }

    /**
     * Obtiene el estado de edición de la celda.
     * @return true si la celda es editable, false si no lo es.
     */
    public boolean getEditable(){
        return this.isEditable;
    }

    /**
     * Establece el estado de edición de la celda.
     * @param editable true si la celda debe ser editable, false si no lo es.
     */
    public void setEditable(boolean editable){
        this.isEditable = editable;
    }

    /**
     * Limpia el valor de la celda si es editable.
     */
    public void clear() {
        if (this.isEditable){
            this.value = 0;
        }
    }
}
