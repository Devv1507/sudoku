package univalle.tedesoft.sudoku.models;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import java.util.ArrayList;
import java.util.List;

public class Rectangles implements IRectangles {
    @Override
    public void createTextFieldsAndLabelsInGrid(GridPane gridPane, int size){
        //gridPane.getChildren().clear();
        List<int[]> selectedCells = new ArrayList<>();
        //List<int[]> selectedCells2 = new ArrayList<>();
        for(int row = 0; row < size; row++){
            List<int[]> acumulatorOfGroup1 = new ArrayList<>();
            List<int[]> acumulatorOfGroup2 = new ArrayList<>();
            for(int col = 0; col < 3; col++){
                //System.out.println(row + " " +col);}
                acumulatorOfGroup1.add(new int[]{row, col});
                acumulatorOfGroup2.add(new int[]{row+3, col+3});
                System.out.println((row) +" "+ (col+3));
            }
            //for(int col = 3; col < 6; col++){
                //System.out.println(row + " " +col);
            //}

        }
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                TextField textField = new TextField();
                textField.setMaxWidth(90);
                textField.setMaxHeight(50);
                textField.setStyle("-fx-font-size: 14px; " +
                                    "-fx-alignment: center;" +
                        "-fx-background-color: transparent; " +
                                "-fx-border-color: transparent; ");
                gridPane.add(textField, col, row);
                GridPane.setHalignment(textField, HPos.CENTER);
                GridPane.setValignment(textField, VPos.CENTER);
            }
        }
    }
    @Override
    public TextField getTextFieldAt(int row, int col){
        return null;
    }
    @Override
    public void generatorOfRandomValidLabels(){


    }
}
