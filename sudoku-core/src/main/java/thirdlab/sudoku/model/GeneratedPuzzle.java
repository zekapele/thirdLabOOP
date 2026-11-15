package thirdlab.sudoku.model;

public record GeneratedPuzzle(int[][] puzzle, int[][] solution, Difficulty difficulty, int clues) {
    public GeneratedPuzzle {
        puzzle = Grids.copy(puzzle);
        solution = Grids.copy(solution);
    }
}
