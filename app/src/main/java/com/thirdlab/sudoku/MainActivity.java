package com.thirdlab.sudoku;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.button.MaterialButton;
import com.thirdlab.sudoku.databinding.ActivityMainBinding;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import thirdlab.sudoku.game.SudokuGame;
import thirdlab.sudoku.model.CheckResult;
import thirdlab.sudoku.model.Difficulty;
import thirdlab.sudoku.model.MoveResult;

public final class MainActivity extends AppCompatActivity {
    private static final Logger LOG = Logger.getLogger(MainActivity.class.getName());
    private static final String STATE_HAS_GAME = "hasGame";
    private static final String STATE_CURRENT = "current";
    private static final String STATE_GIVEN = "given";
    private static final String STATE_SOLUTION = "solution";
    private static final String STATE_DIFFICULTY = "difficulty";
    private static final String STATE_ROW = "row";
    private static final String STATE_COLUMN = "column";
    private static final String STATE_SOLVED = "solved";

    private final ExecutorService workers = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "sudoku-generator");
        thread.setDaemon(true);
        return thread;
    });

    private ActivityMainBinding binding;
    private MaterialButton[] digits = new MaterialButton[0];
    private SudokuGame game;
    private int generation;
    private boolean busy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        digits = new MaterialButton[]{
                binding.digit1, binding.digit2, binding.digit3,
                binding.digit4, binding.digit5, binding.digit6,
                binding.digit7, binding.digit8, binding.digit9
        };
        setupControls();
        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_HAS_GAME)) {
            restore(savedInstanceState);
        } else {
            startNewGame(Difficulty.MEDIUM);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_HAS_GAME, game != null);
        if (game == null) {
            return;
        }
        outState.putString(STATE_CURRENT, game.currentDigits());
        outState.putString(STATE_GIVEN, game.givenMask());
        outState.putString(STATE_SOLUTION, game.solutionDigits());
        outState.putString(STATE_DIFFICULTY, game.difficulty().name());
        outState.putInt(STATE_ROW, binding.board.selectedRow());
        outState.putInt(STATE_COLUMN, binding.board.selectedColumn());
        outState.putBoolean(STATE_SOLVED, binding.board.isSolved());
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9) {
            onDigit(keyCode - KeyEvent.KEYCODE_0);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_0) {
            onDigit(0);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        workers.shutdownNow();
        super.onDestroy();
    }

    private void setupControls() {
        binding.toolbar.inflateMenu(R.menu.main_menu);
        binding.toolbar.setOnMenuItemClickListener(this::onMenu);
        for (int index = 0; index < digits.length; index++) {
            int value = index + 1;
            digits[index].setOnClickListener(view -> onDigit(value));
        }
        binding.newGame.setOnClickListener(view -> startNewGame(selectedDifficulty()));
        binding.check.setOnClickListener(view -> onCheck());
        binding.clear.setOnClickListener(view -> onDigit(0));
    }

    private boolean onMenu(MenuItem item) {
        if (item.getItemId() == R.id.action_language) {
            toggleLanguage();
            return true;
        }
        return false;
    }

    private void toggleLanguage() {
        String current = currentLanguage();
        String next = "uk".equals(current) ? "en" : "uk";
        LOG.info(() -> "Switching language to " + next);
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next));
    }

    private String currentLanguage() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (!locales.isEmpty() && locales.get(0) != null) {
            return locales.get(0).getLanguage();
        }
        return Locale.getDefault().getLanguage();
    }

    private void startNewGame(Difficulty difficulty) {
        int token = ++generation;
        setBusy(true);
        binding.board.setSolved(false);
        binding.status.setText(R.string.status_generating);
        binding.status.setTextColor(color(R.color.statusText));
        LOG.info(() -> "Requested new game, difficulty=" + difficulty);
        workers.execute(() -> {
            try {
                SudokuGame created = SudokuGame.create(difficulty, new java.util.Random());
                runOnUiThread(() -> {
                    if (token != generation || isDestroyed()) {
                        return;
                    }
                    showGame(created);
                    setBusy(false);
                });
            } catch (RuntimeException exception) {
                LOG.log(Level.SEVERE, "Generation failed", exception);
                runOnUiThread(() -> {
                    if (token != generation || isDestroyed()) {
                        return;
                    }
                    binding.status.setText(R.string.status_failed);
                    binding.status.setTextColor(color(R.color.statusBad));
                    setBusy(false);
                });
            }
        });
    }

    private void restore(Bundle state) {
        Difficulty difficulty = Difficulty.valueOf(state.getString(STATE_DIFFICULTY, Difficulty.MEDIUM.name()));
        checkDifficulty(difficulty);
        try {
            SudokuGame restored = SudokuGame.restore(
                    difficulty,
                    state.getString(STATE_CURRENT),
                    state.getString(STATE_GIVEN),
                    state.getString(STATE_SOLUTION));
            showGame(restored);
            binding.board.select(state.getInt(STATE_ROW, -1), state.getInt(STATE_COLUMN, -1));
            binding.board.setSolved(state.getBoolean(STATE_SOLVED));
            if (state.getBoolean(STATE_SOLVED)) {
                binding.status.setText(R.string.status_solved);
                binding.status.setTextColor(color(R.color.statusOk));
            }
        } catch (RuntimeException exception) {
            LOG.log(Level.WARNING, "Could not restore the game", exception);
            startNewGame(difficulty);
        }
    }

    private void showGame(SudokuGame created) {
        game = created;
        checkDifficulty(created.difficulty());
        binding.board.setGame(created);
        binding.board.setSolved(false);
        created.firstEmpty().ifPresentOrElse(
                cell -> binding.board.select(cell.row(), cell.column()),
                () -> binding.board.select(-1, -1));
        showReady();
    }

    private void onDigit(int value) {
        if (busy || game == null) {
            return;
        }
        int row = binding.board.selectedRow();
        int column = binding.board.selectedColumn();
        if (row < 0 || column < 0) {
            binding.status.setText(R.string.status_select);
            binding.status.setTextColor(color(R.color.statusText));
            return;
        }
        MoveResult result = game.enter(row, column, value);
        if (result == MoveResult.REJECTED_GIVEN) {
            binding.status.setText(R.string.status_given);
            binding.status.setTextColor(color(R.color.statusBad));
            return;
        }
        if (result == MoveResult.REJECTED_VALUE) {
            return;
        }
        binding.board.setSolved(false);
        showReady();
        binding.board.invalidate();
    }

    private void onCheck() {
        if (busy || game == null) {
            return;
        }
        CheckResult result = game.check();
        LOG.info(() -> "Player checked the board, status=" + result.status());
        switch (result.status()) {
            case SOLVED -> {
                binding.status.setText(R.string.status_solved);
                binding.status.setTextColor(color(R.color.statusOk));
                binding.board.setSolved(true);
            }
            case INCOMPLETE -> {
                binding.status.setText(getString(R.string.status_incomplete, game.emptyCount()));
                binding.status.setTextColor(color(R.color.statusText));
                binding.board.setSolved(false);
            }
            case INVALID -> {
                binding.status.setText(getString(R.string.status_invalid, result.conflicts().size()));
                binding.status.setTextColor(color(R.color.statusBad));
                binding.board.setSolved(false);
            }
        }
        binding.board.invalidate();
    }

    private void showReady() {
        binding.status.setText(getString(R.string.status_ready, difficultyLabel(game.difficulty())));
        binding.status.setTextColor(color(R.color.statusText));
    }

    private Difficulty selectedDifficulty() {
        int checked = binding.difficultyGroup.getCheckedButtonId();
        if (checked == R.id.difficultyEasy) {
            return Difficulty.EASY;
        }
        if (checked == R.id.difficultyHard) {
            return Difficulty.HARD;
        }
        return Difficulty.MEDIUM;
    }

    private void checkDifficulty(Difficulty difficulty) {
        int button = switch (difficulty) {
            case EASY -> R.id.difficultyEasy;
            case MEDIUM -> R.id.difficultyMedium;
            case HARD -> R.id.difficultyHard;
        };
        binding.difficultyGroup.check(button);
    }

    private String difficultyLabel(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> getString(R.string.difficulty_easy);
            case MEDIUM -> getString(R.string.difficulty_medium);
            case HARD -> getString(R.string.difficulty_hard);
        };
    }

    private void setBusy(boolean value) {
        busy = value;
        binding.board.setEnabled(!value);
        binding.newGame.setEnabled(!value);
        binding.check.setEnabled(!value);
        binding.clear.setEnabled(!value);
        binding.difficultyGroup.setEnabled(!value);
        for (MaterialButton button : digits) {
            button.setEnabled(!value);
        }
    }

    private int color(int resource) {
        return ContextCompat.getColor(this, resource);
    }
}
