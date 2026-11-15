package thirdlab.sudoku.model;

public record Position(int row, int column) {
    public Position {
        Grids.requireCell(row, column);
    }
}
