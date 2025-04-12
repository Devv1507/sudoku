package univalle.tedesoft.sudoku.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import univalle.tedesoft.sudoku.views.GameView;

import java.io.IOException;

public class WelcomeController {

    @FXML
    public void onActionStartButton(ActionEvent actionEvent) throws IOException {
        GameView gameView = GameView.getInstance();
        gameView.show();
    }

    @FXML
    void onInstruction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inst");
        alert.setHeaderText(null);
        String helpContent = """
           Completa la cuadrícula de 6x6 con números del 1 al 6.
           - Cada fila debe contener todos los números del 1 al 6 sin repetición.
           - Cada columna debe contener todos los números del 1 al 6 sin repetición.
           - Cada bloque de 2x3 debe contener todos los números del 1 al 6 sin repetición.
           Haz clic en una celda vacía para ingresar un número. Las celdas con números en negrita son fijas.
           Usa las teclas DELETE o BACKSPACE para borrar un número ingresado.

           Botones:
           - Reiniciar: Inicia un puzzle de Sudoku completamente nuevo.
           - Limpiar: Borra todos los números ingresados por el usuario en el puzzle actual.
           - Ayuda: Muestra una pista (si es posible).
           - ?: Muestra esta ventana.
           """; // El contenido ahora reside aquí
        alert.getDialogPane().setContent(helpContent);
        alert.showAndWait();
       ;
    }
}
