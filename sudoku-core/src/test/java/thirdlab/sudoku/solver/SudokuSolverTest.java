package thirdlab.sudoku.solver;

import org.junit.jupiter.api.Test;
import thirdlab.sudoku.model.Grids;
import thirdlab.sudoku.rules.SudokuRules;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuSolverTest {
    private static final String PUZZLE =
            "530070000"
                    + "600195000"
                    + "098000060"
                    + "800060003"
                    + "400803001"
                    + "700020006"
                    + "060000280"
                    + "000419005"
                    + "000080079";

    private static final String SOLUTION =
            "534678912"
                    + "672195348"
                    + "198342567"
                    + "859761423"
                    + "426853791"
                    + "713924856"
                    + "961537284"
                    + "287419635"
                    + "345286179";

    @Test
    void solvesTheKnownPuzzleAndRejectsASecondSolution() {
        SudokuSolver solver = new SudokuSolver();
        int[][] puzzle = Grids.decode(PUZZLE);

        Optional<int[][]> solved = solver.solve(puzzle);

        assertTrue(solved.isPresent());
        assertArrayEquals(Grids.decode(SOLUTION), solved.get());
        assertEquals(1, solver.countSolutions(puzzle, 2));
        assertTrue(SudokuRules.isComplete(solved.get()));
        assertTrue(SudokuRules.isValidPartial(solved.get()));
    }

    @Test
    void returnsZeroWhenTheGivensAlreadyConflict() {
        int[][] puzzle = new int[9][9];
        puzzle[0][0] = 4;
        puzzle[0][1] = 4;

        assertEquals(0, new SudokuSolver().countSolutions(puzzle, 2));
        assertTrue(new SudokuSolver().solve(puzzle).isEmpty());
    }

    @Test
    void stopsAfterTheRequestedNumberOfSolutions() {
        assertEquals(2, new SudokuSolver().countSolutions(new int[9][9], 2));
    }

    @Test
    void completeGridFollowsTheRules() {
        int[][] grid = new SudokuSolver().complete(new Random(7));

        assertTrue(SudokuRules.isComplete(grid));
        assertTrue(SudokuRules.isValidPartial(grid));
        assertEquals(1, new SudokuSolver().countSolutions(grid, 2));
    }
}
