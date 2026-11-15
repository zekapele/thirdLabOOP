package thirdlab.sudoku.generator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import thirdlab.sudoku.logging.LogConfig;
import thirdlab.sudoku.model.Difficulty;
import thirdlab.sudoku.model.GeneratedPuzzle;
import thirdlab.sudoku.model.Grids;
import thirdlab.sudoku.rules.SudokuRules;
import thirdlab.sudoku.solver.SudokuSolver;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuGeneratorTest {
    @BeforeAll
    static void configureLogging() {
        LogConfig.init();
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    @Timeout(30)
    void puzzleHasOneSolutionAndKeepsTheClues(Difficulty difficulty) {
        GeneratedPuzzle generated = new SudokuGenerator().generate(difficulty, new Random(21));

        assertTrue(generated.clues() >= difficulty.clues());
        assertTrue(generated.clues() <= difficulty.clues() + 4);
        assertEquals(generated.clues(), countClues(generated.puzzle()));
        assertTrue(SudokuRules.isValidPartial(generated.puzzle()));
        assertEquals(1, new SudokuSolver().countSolutions(generated.puzzle(), 2));
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                int clue = generated.puzzle()[row][column];
                if (clue != 0) {
                    assertEquals(generated.solution()[row][column], clue);
                }
            }
        }
    }

    @org.junit.jupiter.api.Test
    void theSameSeedProducesTheSamePuzzle() {
        SudokuGenerator generator = new SudokuGenerator();
        GeneratedPuzzle first = generator.generate(Difficulty.EASY, new Random(5));
        GeneratedPuzzle second = generator.generate(Difficulty.EASY, new Random(5));

        assertEquals(Grids.encode(first.puzzle()), Grids.encode(second.puzzle()));
    }

    private static int countClues(int[][] puzzle) {
        int clues = 0;
        for (int[] row : puzzle) {
            for (int value : row) {
                if (value != 0) {
                    clues++;
                }
            }
        }
        return clues;
    }
}
