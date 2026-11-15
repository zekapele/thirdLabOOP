package thirdlab.sudoku.solver;

import thirdlab.sudoku.model.Grids;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

/**
 * Backtracking solver with bit masks and minimum-remaining-values ordering.
 * One instance is not safe to share between threads.
 */
public final class SudokuSolver {
    private static final int ALL_DIGITS = 0x1FF;

    private final int[] rows = new int[Grids.SIZE];
    private final int[] columns = new int[Grids.SIZE];
    private final int[] boxes = new int[Grids.SIZE];
    private final int[][] grid = new int[Grids.SIZE][Grids.SIZE];

    private int solutions;
    private int limit;
    private Random random;
    private int[][] stored;

    public Optional<int[][]> solve(int[][] puzzle) {
        if (!prepare(puzzle, 1, null) || !search()) {
            return Optional.empty();
        }
        return Optional.of(stored);
    }

    public int countSolutions(int[][] puzzle, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        if (!prepare(puzzle, limit, null)) {
            return 0;
        }
        search();
        return solutions;
    }

    public int[][] complete(Random randomSource) {
        if (randomSource == null) {
            throw new IllegalArgumentException("random is required");
        }
        int[][] empty = new int[Grids.SIZE][Grids.SIZE];
        if (!prepare(empty, 1, randomSource) || !search()) {
            throw new IllegalStateException("Failed to generate a complete grid");
        }
        return stored;
    }

    private boolean prepare(int[][] puzzle, int solutionLimit, Random randomSource) {
        Grids.requireGrid(puzzle);
        limit = solutionLimit;
        random = randomSource;
        solutions = 0;
        stored = solutionLimit == 1 ? new int[Grids.SIZE][Grids.SIZE] : null;
        Arrays.fill(rows, 0);
        Arrays.fill(columns, 0);
        Arrays.fill(boxes, 0);
        for (int row = 0; row < Grids.SIZE; row++) {
            Arrays.fill(grid[row], 0);
        }
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                int value = puzzle[row][column];
                if (value == 0) {
                    continue;
                }
                if (value < 1 || value > 9 || !fits(row, column, value)) {
                    return false;
                }
                place(row, column, value);
            }
        }
        return true;
    }

    private boolean search() {
        if (solutions >= limit) {
            return true;
        }
        int bestRow = -1;
        int bestColumn = -1;
        int bestMask = 0;
        int bestCount = 10;
        scan:
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                if (grid[row][column] != 0) {
                    continue;
                }
                int mask = candidateMask(row, column);
                int count = Integer.bitCount(mask);
                if (count == 0) {
                    return false;
                }
                if (count < bestCount) {
                    bestCount = count;
                    bestMask = mask;
                    bestRow = row;
                    bestColumn = column;
                    if (count == 1) {
                        break scan;
                    }
                }
            }
        }
        if (bestRow < 0) {
            if (solutions == 0 && stored != null) {
                Grids.copyInto(grid, stored);
            }
            solutions++;
            return solutions >= limit;
        }
        for (int value : digitOrder(bestMask)) {
            place(bestRow, bestColumn, value);
            boolean stop = search();
            undo(bestRow, bestColumn, value);
            if (stop) {
                return true;
            }
        }
        return false;
    }

    private int[] digitOrder(int mask) {
        int count = Integer.bitCount(mask);
        int[] order = new int[count];
        int index = 0;
        for (int value = 1; value <= 9; value++) {
            if ((mask & bit(value)) != 0) {
                order[index++] = value;
            }
        }
        if (random != null) {
            for (int i = order.length - 1; i > 0; i--) {
                int swap = random.nextInt(i + 1);
                int tmp = order[i];
                order[i] = order[swap];
                order[swap] = tmp;
            }
        }
        return order;
    }

    private int candidateMask(int row, int column) {
        int used = rows[row] | columns[column] | boxes[Grids.boxIndex(row, column)];
        return (~used) & ALL_DIGITS;
    }

    private boolean fits(int row, int column, int value) {
        int bit = bit(value);
        return (rows[row] & bit) == 0
                && (columns[column] & bit) == 0
                && (boxes[Grids.boxIndex(row, column)] & bit) == 0;
    }

    private void place(int row, int column, int value) {
        int bit = bit(value);
        grid[row][column] = value;
        rows[row] |= bit;
        columns[column] |= bit;
        boxes[Grids.boxIndex(row, column)] |= bit;
    }

    private void undo(int row, int column, int value) {
        int bit = bit(value);
        grid[row][column] = 0;
        rows[row] &= ~bit;
        columns[column] &= ~bit;
        boxes[Grids.boxIndex(row, column)] &= ~bit;
    }

    private static int bit(int value) {
        return 1 << (value - 1);
    }
}
