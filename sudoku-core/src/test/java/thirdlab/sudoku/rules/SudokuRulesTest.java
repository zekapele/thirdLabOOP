package thirdlab.sudoku.rules;

import org.junit.jupiter.api.Test;
import thirdlab.sudoku.model.Position;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuRulesTest {
    @Test
    void rejectsTheSameDigitInARowColumnOrBox() {
        int[][] grid = new int[9][9];
        grid[0][0] = 5;
        assertTrue(SudokuRules.canPlace(grid, 0, 0, 5));
        assertFalse(SudokuRules.canPlace(grid, 0, 8, 5));
        assertFalse(SudokuRules.canPlace(grid, 8, 0, 5));
        assertFalse(SudokuRules.canPlace(grid, 2, 2, 5));
        assertTrue(SudokuRules.canPlace(grid, 3, 3, 5));
    }

    @Test
    void marksEveryRepeatedDigit() {
        int[][] grid = new int[9][9];
        grid[0][0] = 1;
        grid[0][1] = 1;
        grid[4][4] = 7;
        grid[5][4] = 7;

        List<Position> conflicts = SudokuRules.conflicts(grid);

        assertTrue(conflicts.contains(new Position(0, 0)));
        assertTrue(conflicts.contains(new Position(0, 1)));
        assertTrue(conflicts.contains(new Position(4, 4)));
        assertTrue(conflicts.contains(new Position(5, 4)));
        assertFalse(SudokuRules.isValidPartial(grid));
    }

    @Test
    void emptyGridIsAnIncompleteValidBoard() {
        int[][] grid = new int[9][9];
        assertTrue(SudokuRules.conflicts(grid).isEmpty());
        assertFalse(SudokuRules.isComplete(grid));
    }
}
