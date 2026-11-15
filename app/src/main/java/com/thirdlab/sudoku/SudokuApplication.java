package com.thirdlab.sudoku;

import android.app.Application;

import com.thirdlab.sudoku.logging.AndroidLogHandler;

import java.util.logging.Logger;

import thirdlab.sudoku.logging.LogConfig;

public final class SudokuApplication extends Application {
    private static final Logger LOG = Logger.getLogger(SudokuApplication.class.getName());

    @Override
    public void onCreate() {
        super.onCreate();
        LogConfig.init();
        LogConfig.useHandler(new AndroidLogHandler());
        LOG.info("Sudoku started, logging is configured");
    }
}
