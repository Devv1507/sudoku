package univalle.tedesoft.sudoku.models;

import javafx.util.Pair;

import java.util.Set;

public interface IGameState {
    boolean isBoardValid();
    Set<Pair<Integer, Integer>> getInvalidCells();
    boolean isBoardFull();
    boolean isGameWon();
    int getClue (int row, int col);
}
