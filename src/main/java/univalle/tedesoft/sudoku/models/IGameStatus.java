package univalle.tedesoft.sudoku.models;

public interface IGameStatus {
    boolean checkRow();
    boolean checkColum();
    boolean checkDiagonal();
}
