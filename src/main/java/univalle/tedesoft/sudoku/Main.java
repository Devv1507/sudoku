package univalle.tedesoft.sudoku;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import univalle.tedesoft.sudoku.views.GameView;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        GameView gameView = GameView.getInstance();
        gameView.show();
    }
    //Hice que funcionara, pero dejo este comentario para denotar algunos problemas en la ejecucion y no olvidarlos la proxima vez que editemos el proyecto, haré un commit
    //en la rama define-controller, pero no fusionaré los cambios ATT Santiago.
    //-Debemos remasterizar la forma en como se crean los textfields, porque quedan espacios que no haces nada, que molestan la estetica.
    //-A mi impresion los labels son demasiado grandes.
    //-La logica que intentamos aplicar para las instrucciones no funciona, el mensaje no se muestra aún.
    //-Hay que discutir lo de las interfaces, no consigo discernir cuales son las funciones mas primitivas.
    //-Ningun boton fuera del de iniciar nuevo juego, funciona.
    public static void main(String[] args) {
        launch();
    }
}