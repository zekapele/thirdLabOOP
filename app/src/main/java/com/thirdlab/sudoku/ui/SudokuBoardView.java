package com.thirdlab.sudoku.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.thirdlab.sudoku.R;

import java.util.List;

import thirdlab.sudoku.game.SudokuGame;
import thirdlab.sudoku.model.Grids;
import thirdlab.sudoku.model.Position;
import thirdlab.sudoku.rules.SudokuRules;

public final class SudokuBoardView extends View {
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int boardColor;
    private final int givenColor;
    private final int selectedColor;
    private final int peerColor;
    private final int sameColor;
    private final int conflictColor;
    private final int gridColor;
    private final int boldColor;
    private final int givenDigitColor;
    private final int userDigitColor;
    private final int conflictDigitColor;
    private final int solvedColor;

    private SudokuGame game;
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private boolean solved;
    private float originLeft;
    private float originTop;
    private float boardSize;

    public SudokuBoardView(Context context) {
        this(context, null);
    }

    public SudokuBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        setClickable(true);
        boardColor = color(R.color.boardBackground);
        givenColor = color(R.color.cellGiven);
        selectedColor = color(R.color.cellSelected);
        peerColor = color(R.color.cellPeer);
        sameColor = color(R.color.cellSame);
        conflictColor = color(R.color.cellConflict);
        gridColor = color(R.color.gridLine);
        boldColor = color(R.color.gridBold);
        givenDigitColor = color(R.color.digitGiven);
        userDigitColor = color(R.color.digitUser);
        conflictDigitColor = color(R.color.digitConflict);
        solvedColor = color(R.color.statusOk);
        textPaint.setTextAlign(Paint.Align.CENTER);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    public void setGame(@Nullable SudokuGame game) {
        this.game = game;
        invalidate();
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
        invalidate();
    }

    public boolean isSolved() {
        return solved;
    }

    public void select(int row, int column) {
        if (row < 0 || column < 0) {
            selectedRow = -1;
            selectedColumn = -1;
        } else {
            Grids.requireCell(row, column);
            selectedRow = row;
            selectedColumn = column;
        }
        invalidate();
    }

    public int selectedRow() {
        return selectedRow;
    }

    public int selectedColumn() {
        return selectedColumn;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        measureBoard();
        float left = originLeft;
        float top = originTop;
        float size = boardSize;
        float cell = size / Grids.SIZE;
        boolean[][] conflicts = conflictMask();

        for (int row = 0; row < Grids.SIZE; row++) {
            for (int column = 0; column < Grids.SIZE; column++) {
                fillPaint.setColor(cellColor(row, column, conflicts));
                canvas.drawRect(left + column * cell, top + row * cell,
                        left + (column + 1) * cell, top + (row + 1) * cell, fillPaint);
                drawDigit(canvas, row, column, conflicts[row][column], left, top, cell);
            }
        }
        for (int index = 0; index <= Grids.SIZE; index++) {
            boolean bold = index % Grids.BOX == 0;
            linePaint.setColor(bold ? boldColor : gridColor);
            linePaint.setStrokeWidth(bold ? dp(2.4f) : dp(1f));
            float offset = index * cell;
            canvas.drawLine(left, top + offset, left + size, top + offset, linePaint);
            canvas.drawLine(left + offset, top, left + offset, top + size, linePaint);
        }
        if (selectedRow >= 0) {
            linePaint.setColor(solved ? solvedColor : userDigitColor);
            linePaint.setStrokeWidth(dp(2.2f));
            float inset = dp(1.4f);
            canvas.drawRect(
                    left + selectedColumn * cell + inset,
                    top + selectedRow * cell + inset,
                    left + (selectedColumn + 1) * cell - inset,
                    top + (selectedRow + 1) * cell - inset,
                    linePaint);
        }
        if (solved) {
            linePaint.setColor(solvedColor);
            linePaint.setStrokeWidth(dp(3.5f));
            canvas.drawRect(left, top, left + size, top + size, linePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || game == null) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }
        measureBoard();
        if (boardSize <= 0f) {
            return true;
        }
        float cell = boardSize / Grids.SIZE;
        int column = (int) ((event.getX() - originLeft) / cell);
        int row = (int) ((event.getY() - originTop) / cell);
        if (row >= 0 && row < Grids.SIZE && column >= 0 && column < Grids.SIZE) {
            select(row, column);
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void drawDigit(Canvas canvas, int row, int column, boolean conflict, float left, float top, float cell) {
        if (game == null) {
            return;
        }
        int value = game.get(row, column);
        if (value == 0) {
            return;
        }
        textPaint.setTextSize(cell * 0.52f);
        textPaint.setFakeBoldText(game.isGiven(row, column));
        if (conflict) {
            textPaint.setColor(conflictDigitColor);
        } else if (game.isGiven(row, column)) {
            textPaint.setColor(givenDigitColor);
        } else {
            textPaint.setColor(userDigitColor);
        }
        float x = left + column * cell + cell / 2f;
        float y = top + row * cell + cell / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(Integer.toString(value), x, y, textPaint);
    }

    private int cellColor(int row, int column, boolean[][] conflicts) {
        boolean selected = row == selectedRow && column == selectedColumn;
        if (conflicts[row][column]) {
            return conflictColor;
        }
        if (selected) {
            return selectedColor;
        }
        if (sameDigit(row, column)) {
            return sameColor;
        }
        if (sameHouse(row, column)) {
            return peerColor;
        }
        if (game != null && game.isGiven(row, column)) {
            return givenColor;
        }
        return boardColor;
    }

    private boolean sameDigit(int row, int column) {
        if (game == null || selectedRow < 0) {
            return false;
        }
        int selectedValue = game.get(selectedRow, selectedColumn);
        return selectedValue != 0 && !(row == selectedRow && column == selectedColumn)
                && game.get(row, column) == selectedValue;
    }

    private boolean sameHouse(int row, int column) {
        if (selectedRow < 0) {
            return false;
        }
        return row == selectedRow
                || column == selectedColumn
                || Grids.boxIndex(row, column) == Grids.boxIndex(selectedRow, selectedColumn);
    }

    private boolean[][] conflictMask() {
        boolean[][] mask = new boolean[Grids.SIZE][Grids.SIZE];
        if (game == null) {
            return mask;
        }
        List<Position> conflicts = SudokuRules.conflicts(game.values());
        for (Position position : conflicts) {
            mask[position.row()][position.column()] = true;
        }
        return mask;
    }

    private void measureBoard() {
        float pad = dp(4f);
        boardSize = Math.max(0f, Math.min(getWidth(), getHeight()) - pad * 2f);
        originLeft = (getWidth() - boardSize) / 2f;
        originTop = (getHeight() - boardSize) / 2f;
    }

    private int color(int resource) {
        return ContextCompat.getColor(getContext(), resource);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
