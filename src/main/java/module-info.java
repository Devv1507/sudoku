module univalle.tedesoft.sudoku {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens univalle.tedesoft.sudoku to javafx.fxml;
    opens univalle.tedesoft.sudoku.controllers to javafx.fxml;
    exports univalle.tedesoft.sudoku;
}