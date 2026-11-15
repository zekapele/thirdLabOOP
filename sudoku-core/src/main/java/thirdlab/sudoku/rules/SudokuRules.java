package thirdlab.sudoku.rules;

import thirdlab.sudoku.model.Grids;
import thirdlab.sudoku.model.Position;

import java.util.ArrayList;
import java.util.List;

public final class SudokuRules {
    private SudokuRules() {
    }

    public static boolean canPlace(int[][] grid, int row, int column, int value) {
        Grids.requireGrid(grid);
        Grids.requireCell(row, column);
        if (value < 1 || value > 9) {
            return false;
        }
        for (int index = 0; index < Grids.SIZE; index++) {
            if (index != column && grid[row][index] == value) {
                return false;
            }
            if (index != row && grid[index][column] == value) {
                return false;
            }
        }
        int boxRow = (row / Grids.BOX) * Grids.BOX;
        int boxColumn = (column / Grids.BOX) * Grids.BOX;
        for (int r = boxRow; r < boxRow + Grids.BOX; r++) {
            for (int c = boxColumn; c < boxColumn + Grids.BOX; c++) {
                if ((r != row || c != column) && grid[r][c] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isComplete(int[][] grid) {
        Grids.requireGrid(grid);
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                if (grid[row][column] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static List<Position> conflicts(int[][] grid) {
        Grids.requireGrid(grid);
        boolean[][] bad = new boolean[Grids.SIZE][Grids.SIZE];
        for (int index = 0; index < Grids.SIZE; index++) {
            markUnit(grid, bad, rowCells(index));
            markUnit(grid, bad, columnCells(index));
            markUnit(grid, bad, boxCells(index));
        }
        List<Position> result = new ArrayList<>();
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                if (bad[row][column]) {
                    result.add(new Position(row, column));
                }
            }
        }
        return result;
    }

    public static boolean isValidPartial(int[][] grid) {
        return conflicts(grid).isEmpty();
    }

    private static void markUnit(int[][] grid, boolean[][] bad, int[][] cells) {
        int[] seenAt = new int[10];
        for (int value = 1; value <= 9; value++) {
            seenAt[value] = -1;
        }
        for (int index = 0; index < cells.length; index++) {
            int row = cells[index][0];
            int column = cells[index][1];
            int value = grid[row][column];
            if (value == 0) {
                continue;
            }
            if (value < 1 || value > 9) {
                bad[row][column] = true;
                continue;
            }
            if (seenAt[value] == -1) {
                seenAt[value] = index;
                continue;
            }
            bad[row][column] = true;
            if (seenAt[value] >= 0) {
                int previous = seenAt[value];
                bad[cells[previous][0]][cells[previous][1]] = true;
                seenAt[value] = -2;
            }
        }
    }

    private static int[][] rowCells(int row) {
        int[][] cells = new int[Grids.SIZE][2];
        for (int column = 0; column < Grids.SIZE; column++) {
            cells[column][0] = row;
            cells[column][1] = column;
        }
        return cells;
    }

    private static int[][] columnCells(int column) {
        int[][] cells = new int[Grids.SIZE][2];
        for (int row = 0; row < Grids.SIZE; row++) {
            cells[row][0] = row;
            cells[row][1] = column;
        }
        return cells;
    }

    private static int[][] boxCells(int box) {
        int[][] cells = new int[Grids.SIZE][2];
        int boxRow = (box / Grids.BOX) * Grids.BOX;
        int boxColumn = (box % Grids.BOX) * Grids.BOX;
        int index = 0;
        for (int row = boxRow; row < boxRow + Grids.BOX; row++) {
            for (int column = boxColumn; column < boxColumn + Grids.BOX; column++) {
                cells[index][0] = row;
                cells[index][1] = column;
                index++;
            }
        }
        return cells;
    }
}
