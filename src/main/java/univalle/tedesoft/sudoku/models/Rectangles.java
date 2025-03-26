package univalle.tedesoft.sudoku.models;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class Rectangles implements IRectangles {
    @Override
    public void createTextFieldsAndLabelsInGrid(GridPane gridPane, int size){
        List<int[]> orderCells = new ArrayList<>();
        for(int row = 0; row < size; row++){
            for(int col = 0; col < 3; col++){
                orderCells.add(new int[]{row, col});
            }
        }
        for(int row = 0; row < size; row++){
            for(int col = 3; col < size; col++){
                orderCells.add(new int[]{row, col});
            }
        }
        Random random = new Random();
        List<Integer> selectedCells = new ArrayList<>();
        for (int i = 0; i < orderCells.size(); i += 6) {
            int randomNumber = random.nextInt((i+5) - i + 1) + i;
            int randomNumber2;
            do {
                randomNumber2 = random.nextInt((i+5) - i + 1) + i;
            } while (randomNumber2 == randomNumber);
            selectedCells.add(randomNumber);
            selectedCells.add(randomNumber2);
            //System.out.println(randomNumber);
            //System.out.println(randomNumber2);
        }
        //Funcion por borrar, solo para testear
        for (int i = 0; i < orderCells.size(); i++) {
            int[] cell = orderCells.get(i);
            //System.out.println(cell[i]);
            int row = cell[0];
            int col = cell[1];
            System.out.println( i +" "+"Fila: " + row + ", Columna: " + col);
        }
        for (int i = 0; i < orderCells.size(); i++) {
            int[] cell = orderCells.get(i);
            int row = cell[0];
            int col = cell[1];
            String id = row + "," + col;
            if (selectedCells.contains(i)) {
                Label label = new Label("X");
                label.setId(id);
                label.setStyle("-fx-font-size: 14px;"+ " -fx-font-weight: bold;");
                gridPane.add(label, col, row);
                GridPane.setHalignment(label, HPos.CENTER);
                GridPane.setValignment(label, VPos.CENTER);
            } else {
                TextField textField = new TextField();
                textField.setId(id);
                textField.setMaxWidth(90);
                textField.setMaxHeight(50);
                textField.setStyle("-fx-font-size: 14px;" + " -fx-alignment: center;" + "-fx-background-color: transparent; " + "-fx-border-color: transparent; ");
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
