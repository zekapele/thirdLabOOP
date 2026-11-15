package thirdlab.sudoku.game;

import org.junit.jupiter.api.Test;
import thirdlab.sudoku.model.CheckStatus;
import thirdlab.sudoku.model.Difficulty;
import thirdlab.sudoku.model.Grids;
import thirdlab.sudoku.model.MoveResult;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuGameTest {
    @Test
    void checkReportsIncompleteWrongAndSolvedBoards() {
        SudokuGame game = SudokuGame.create(Difficulty.EASY, new Random(3));
        var empty = game.firstEmpty();
        assertTrue(empty.isPresent());
        int row = empty.get().row();
        int column = empty.get().column();

        assertEquals(CheckStatus.INCOMPLETE, game.check().status());
        assertEquals(MoveResult.REJECTED_GIVEN, game.enter(firstClue(game).row(), firstClue(game).column(), 1));
        assertEquals(MoveResult.REJECTED_VALUE, game.enter(row, column, 10));

        assertEquals(MoveResult.PLACED, game.enter(row, column, conflictingDigit(game, row, column)));
        assertEquals(CheckStatus.INVALID, game.check().status());
        assertEquals(MoveResult.CLEARED, game.enter(row, column, 0));

        fillSolution(game);
        assertEquals(CheckStatus.SOLVED, game.check().status());
    }

    @Test
    void restoreKeepsMovesAndClues() {
        SudokuGame game = SudokuGame.create(Difficulty.MEDIUM, new Random(9));
        var empty = game.firstEmpty().orElseThrow();
        int correct = Grids.decode(game.solutionDigits())[empty.row()][empty.column()];
        game.enter(empty.row(), empty.column(), correct);

        SudokuGame restored = SudokuGame.restore(
                game.difficulty(),
                game.currentDigits(),
                game.givenMask(),
                game.solutionDigits());

        assertEquals(game.currentDigits(), restored.currentDigits());
        assertEquals(game.givenMask(), restored.givenMask());
        assertTrue(restored.isGiven(firstClue(game).row(), firstClue(game).column()));
        assertEquals(correct, restored.get(empty.row(), empty.column()));
    }

    private static void fillSolution(SudokuGame game) {
        int[][] solution = Grids.decode(game.solutionDigits());
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                if (!game.isGiven(row, column)) {
                    game.enter(row, column, solution[row][column]);
                }
            }
        }
    }

    private static thirdlab.sudoku.model.Position firstClue(SudokuGame game) {
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                if (game.isGiven(row, column)) {
                    return new thirdlab.sudoku.model.Position(row, column);
                }
            }
        }
        throw new AssertionError("Puzzle has no clues");
    }

    private static int conflictingDigit(SudokuGame game, int row, int column) {
        for (int index = 0; index < Grids.SIZE; index++) {
            if (index != column && game.get(row, index) != 0) {
                return game.get(row, index);
            }
            if (index != row && game.get(index, column) != 0) {
                return game.get(index, column);
            }
        }
        throw new AssertionError("Expected a clue in the same row or column");
    }
}
