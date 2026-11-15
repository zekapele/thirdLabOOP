package thirdlab.sudoku.model;

public final class Grids {
    public static final int SIZE = 9;
    public static final int BOX = 3;
    public static final int CELLS = SIZE * SIZE;

    private Grids() {
    }

    public static int boxIndex(int row, int column) {
        return (row / BOX) * BOX + column / BOX;
    }

    public static void requireCell(int row, int column) {
        if (row < 0 || row >= SIZE || column < 0 || column >= SIZE) {
            throw new IllegalArgumentException("Cell is outside the board: " + row + "," + column);
        }
    }

    public static void requireGrid(int[][] grid) {
        if (grid == null || grid.length != SIZE) {
            throw new IllegalArgumentException("Grid must be 9x9");
        }
        for (int[] row : grid) {
            if (row == null || row.length != SIZE) {
                throw new IllegalArgumentException("Grid must be 9x9");
            }
        }
    }

    public static int[][] copy(int[][] grid) {
        requireGrid(grid);
        int[][] copy = new int[SIZE][SIZE];
        copyInto(grid, copy);
        return copy;
    }

    public static void copyInto(int[][] from, int[][] to) {
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(from[row], 0, to[row], 0, SIZE);
        }
    }

    public static String encode(int[][] grid) {
        requireGrid(grid);
        char[] chars = new char[CELLS];
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                int value = grid[row][column];
                if (value < 0 || value > 9) {
                    throw new IllegalArgumentException("Value out of range: " + value);
                }
                chars[row * SIZE + column] = (char) ('0' + value);
            }
        }
        return new String(chars);
    }

    public static int[][] decode(String digits) {
        if (digits == null || digits.length() != CELLS) {
            throw new IllegalArgumentException("Expected 81 digits");
        }
        int[][] grid = new int[SIZE][SIZE];
        for (int index = 0; index < CELLS; index++) {
            char symbol = digits.charAt(index);
            if (symbol < '0' || symbol > '9') {
                throw new IllegalArgumentException("Expected digits 0-9");
            }
            grid[index / SIZE][index % SIZE] = symbol - '0';
        }
        return grid;
    }
}
