package univalle.tedesoft.sudoku.models;

public interface ICell {
    int getValue();
    void setValue(int value);
    boolean getEditable();

    /**
     * Setter for editable boolean.
     * @param isEditable
     */
    void setEditable(boolean isEditable);
}
