package thirdlab.sudoku.model;

import java.util.List;

public record CheckResult(CheckStatus status, List<Position> conflicts) {
    public CheckResult {
        conflicts = List.copyOf(conflicts);
    }
}
