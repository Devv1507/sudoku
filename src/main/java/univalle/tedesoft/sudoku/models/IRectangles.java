package univalle.tedesoft.sudoku.models;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public interface IRectangles {
    void createTextFieldsAndLabelsInGrid(GridPane gridPane, int size);
    TextField getTextFieldAt(int row, int col);
    void generatorOfRandomValidLabels();
}
