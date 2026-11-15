package thirdlab.sudoku.generator;

import thirdlab.sudoku.model.Difficulty;
import thirdlab.sudoku.model.GeneratedPuzzle;
import thirdlab.sudoku.model.Grids;
import thirdlab.sudoku.solver.SudokuSolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public final class SudokuGenerator {
    private static final Logger LOG = Logger.getLogger(SudokuGenerator.class.getName());

    private final SudokuSolver solver = new SudokuSolver();

    public GeneratedPuzzle generate(Difficulty difficulty, Random random) {
        if (difficulty == null || random == null) {
            throw new IllegalArgumentException("difficulty and random are required");
        }
        long started = System.nanoTime();
        LOG.info(() -> "Generating puzzle, difficulty=" + difficulty + ", targetClues=" + difficulty.clues());
        int[][] solution = solver.complete(random);
        int[][] puzzle = Grids.copy(solution);
        List<Integer> order = new ArrayList<>(Grids.CELLS);
        for (int index = 0; index < Grids.CELLS; index++) {
            order.add(index);
        }
        Collections.shuffle(order, random);
        int removed = 0;
        int targetRemove = Grids.CELLS - difficulty.clues();
        for (int index : order) {
            if (removed >= targetRemove) {
                break;
            }
            int row = index / Grids.SIZE;
            int column = index % Grids.SIZE;
            if (puzzle[row][column] == 0) {
                continue;
            }
            int mirrorRow = Grids.SIZE - 1 - row;
            int mirrorColumn = Grids.SIZE - 1 - column;
            boolean canPair = (mirrorRow != row || mirrorColumn != column)
                    && puzzle[mirrorRow][mirrorColumn] != 0
                    && removed + 2 <= targetRemove;
            if (canPair && removeIfUnique(puzzle, row, column, mirrorRow, mirrorColumn)) {
                removed += 2;
                continue;
            }
            if (removeIfUnique(puzzle, row, column, -1, -1)) {
                removed++;
            }
        }
        int clues = Grids.CELLS - removed;
        int removedCount = removed;
        long tookMs = (System.nanoTime() - started) / 1_000_000L;
        LOG.info(() -> "Puzzle ready, clues=" + clues + ", removed=" + removedCount + ", tookMs=" + tookMs);
        return new GeneratedPuzzle(puzzle, solution, difficulty, clues);
    }

    /**
     * Digs one cell, or a symmetric pair, and keeps the change only when the puzzle still has one solution.
     */
    private boolean removeIfUnique(int[][] puzzle, int row, int column, int mirrorRow, int mirrorColumn) {
        int first = puzzle[row][column];
        int second = 0;
        boolean pair = mirrorRow >= 0;
        puzzle[row][column] = 0;
        if (pair) {
            second = puzzle[mirrorRow][mirrorColumn];
            puzzle[mirrorRow][mirrorColumn] = 0;
        }
        if (solver.countSolutions(puzzle, 2) == 1) {
            return true;
        }
        puzzle[row][column] = first;
        if (pair) {
            puzzle[mirrorRow][mirrorColumn] = second;
        }
        return false;
    }
}
