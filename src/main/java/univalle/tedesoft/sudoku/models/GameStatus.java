package univalle.tedesoft.sudoku.models;

public class GameStatus implements IGameStatus {
    @Override
    public boolean checkRow () {
        return false;
    }

    @Override
    public boolean checkColum () {
        return false;
    }

    @Override
    public boolean checkDiagonal () {
        return false;
    }
}
