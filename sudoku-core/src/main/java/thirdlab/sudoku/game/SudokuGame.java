package thirdlab.sudoku.game;

import thirdlab.sudoku.generator.SudokuGenerator;
import thirdlab.sudoku.model.CheckResult;
import thirdlab.sudoku.model.CheckStatus;
import thirdlab.sudoku.model.Difficulty;
import thirdlab.sudoku.model.GeneratedPuzzle;
import thirdlab.sudoku.model.Grids;
import thirdlab.sudoku.model.MoveResult;
import thirdlab.sudoku.model.Position;
import thirdlab.sudoku.rules.SudokuRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

public final class SudokuGame {
    private static final Logger LOG = Logger.getLogger(SudokuGame.class.getName());

    private final Difficulty difficulty;
    private final int[][] current;
    private final boolean[][] given;
    private final int[][] solution;

    private SudokuGame(Difficulty difficulty, int[][] current, boolean[][] given, int[][] solution) {
        this.difficulty = difficulty;
        this.current = Grids.copy(current);
        this.solution = Grids.copy(solution);
        this.given = copyMask(given);
        validate();
    }

    public static SudokuGame create(Difficulty difficulty, Random random) {
        GeneratedPuzzle generated = new SudokuGenerator().generate(difficulty, random);
        boolean[][] clues = new boolean[Grids.SIZE][Grids.SIZE];
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                clues[row][column] = generated.puzzle()[row][column] != 0;
            }
        }
        SudokuGame game = new SudokuGame(difficulty, generated.puzzle(), clues, generated.solution());
        LOG.info(() -> "New game, difficulty=" + difficulty + ", clues=" + game.clueCount());
        return game;
    }

    public static SudokuGame restore(Difficulty difficulty, String currentDigits, String givenMask, String solutionDigits) {
        LOG.info(() -> "Restoring game, difficulty=" + difficulty);
        return new SudokuGame(difficulty, Grids.decode(currentDigits), mask(givenMask), Grids.decode(solutionDigits));
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public int clueCount() {
        int count = 0;
        for (boolean[] row : given) {
            for (boolean clue : row) {
                if (clue) {
                    count++;
                }
            }
        }
        return count;
    }

    public int emptyCount() {
        int count = 0;
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                if (current[row][column] == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public int get(int row, int column) {
        Grids.requireCell(row, column);
        return current[row][column];
    }

    public boolean isGiven(int row, int column) {
        Grids.requireCell(row, column);
        return given[row][column];
    }

    public Optional<Position> firstEmpty() {
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                if (current[row][column] == 0) {
                    return Optional.of(new Position(row, column));
                }
            }
        }
        return Optional.empty();
    }

    public MoveResult enter(int row, int column, int value) {
        Grids.requireCell(row, column);
        if (value < 0 || value > 9) {
            LOG.warning(() -> "Rejected value " + value + " at " + row + "," + column);
            return MoveResult.REJECTED_VALUE;
        }
        if (given[row][column]) {
            LOG.fine(() -> "Rejected edit of clue at " + row + "," + column);
            return MoveResult.REJECTED_GIVEN;
        }
        current[row][column] = value;
        LOG.fine(() -> "Cell " + row + "," + column + " set to " + value);
        return value == 0 ? MoveResult.CLEARED : MoveResult.PLACED;
    }

    public CheckResult check() {
        List<Position> conflicts = SudokuRules.conflicts(current);
        if (!conflicts.isEmpty()) {
            LOG.info(() -> "Check failed, conflicts=" + conflicts.size());
            return new CheckResult(CheckStatus.INVALID, conflicts);
        }
        if (!SudokuRules.isComplete(current)) {
            LOG.info(() -> "Check incomplete, empty=" + emptyCount());
            return new CheckResult(CheckStatus.INCOMPLETE, List.of());
        }
        if (!matchesSolution()) {
            LOG.warning("Complete grid does not match the generated solution");
            return new CheckResult(CheckStatus.INVALID, differingCells());
        }
        LOG.info("Solution accepted");
        return new CheckResult(CheckStatus.SOLVED, List.of());
    }

    public int[][] values() {
        return Grids.copy(current);
    }

    public String currentDigits() {
        return Grids.encode(current);
    }

    public String solutionDigits() {
        return Grids.encode(solution);
    }

    public String givenMask() {
        char[] chars = new char[Grids.CELLS];
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                chars[row * Grids.SIZE + column] = given[row][column] ? '1' : '0';
            }
        }
        return new String(chars);
    }

    private boolean matchesSolution() {
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                if (current[row][column] != solution[row][column]) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<Position> differingCells() {
        List<Position> cells = new ArrayList<>();
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                if (current[row][column] != 0 && current[row][column] != solution[row][column]) {
                    cells.add(new Position(row, column));
                }
            }
        }
        return cells;
    }

    private void validate() {
        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty is required");
        }
        if (!SudokuRules.isComplete(solution) || !SudokuRules.isValidPartial(solution)) {
            throw new IllegalArgumentException("Solution is not a complete Sudoku grid");
        }
        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                int value = current[row][column];
                if (value < 0 || value > 9) {
                    throw new IllegalArgumentException("Value out of range");
                }
                if (given[row][column] && value != solution[row][column]) {
                    throw new IllegalArgumentException("Clue does not match the solution");
                }
            }
        }
    }

    private static boolean[][] copyMask(boolean[][] mask) {
        if (mask == null || mask.length != Grids.SIZE) {
            throw new IllegalArgumentException("Given mask must be 9x9");
        }
        boolean[][] copy = new boolean[Grids.SIZE][Grids.SIZE];
        for (int row = 0; row < Grids.SIZE; row++) {
            if (mask[row] == null || mask[row].length != Grids.SIZE) {
                throw new IllegalArgumentException("Given mask must be 9x9");
            }
            System.arraycopy(mask[row], 0, copy[row], 0, Grids.SIZE);
        }
        return copy;
    }

    private static boolean[][] mask(String givenMask) {
        if (givenMask == null || givenMask.length() != Grids.CELLS) {
            throw new IllegalArgumentException("Expected 81 clue flags");
        }
        boolean[][] given = new boolean[Grids.SIZE][Grids.SIZE];
        for (int index = 0; index < Grids.CELLS; index++) {
            char symbol = givenMask.charAt(index);
            if (symbol != '0' && symbol != '1') {
                throw new IllegalArgumentException("Clue mask must contain 0 and 1");
            }
            given[index / Grids.SIZE][index % Grids.SIZE] = symbol == '1';
        }
        return given;
    }
}
