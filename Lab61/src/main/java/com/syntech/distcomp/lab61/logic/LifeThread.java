package com.syntech.distcomp.lab61.logic;

import java.util.concurrent.BrokenBarrierException;

public class LifeThread extends Thread {
    private Board board;
    private int row, col;
    private final int id;

    public LifeThread(Board board, int id) {
        this.board = board;
        this.id = id;
        this.row = id % 2;
        this.col = id % 4 / 2;
    }

    @Override
    public void run() {
        while (true) {
            try {
                board.update(id, row, col);
                sleep(0);
            } catch (InterruptedException | BrokenBarrierException e) {
                break;
            }
            row += 2;
            if (row >= board.getHeight()) {
                row -= board.getHeight();
                col += 2;
                if (col >= board.getWidth()) {
                    col -= board.getWidth();
                }
            }
        }
    }
}
