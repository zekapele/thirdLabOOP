package thirdlab.sudoku.model;

public enum Difficulty {
    EASY(42),
    MEDIUM(34),
    HARD(28);

    private final int clues;

    Difficulty(int clues) {
        this.clues = clues;
    }

    public int clues() {
        return clues;
    }
}
