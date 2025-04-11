package univalle.tedesoft.sudoku.models;

import javafx.util.Pair;

import java.util.Set;

public interface IBlock {
    Cell getCell(int row, int col);
    Set<Pair<Integer, Integer>> getInvalidCellsInBlock();
}
