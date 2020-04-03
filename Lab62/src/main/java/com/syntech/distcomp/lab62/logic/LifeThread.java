package com.syntech.distcomp.lab62.logic;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BrokenBarrierException;

public class LifeThread extends Thread {
    private Board board;
    private int row, col;
    private final int id;

    public LifeThread(@NotNull Board board, int id) {
        this.board = board;
        this.id = id;
        this.row = 0;
        this.col = 0;
    }

    public void setCoords(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public void run() {
        while (true) {
            try {
                board.update(id, row, col);
                //sleep(0);
            } catch (InterruptedException | BrokenBarrierException e) {
                break;
            }
            row += 1;
            if (row >= board.getHeight()) {
                row -= board.getHeight();
                col += 1;
                if (col >= board.getWidth()) {
                    col -= board.getWidth();
                }
            }
        }
    }
}
