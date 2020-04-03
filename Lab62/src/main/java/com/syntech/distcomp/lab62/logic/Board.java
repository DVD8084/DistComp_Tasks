package com.syntech.distcomp.lab62.logic;

import com.syntech.distcomp.lab62.graphics.Graphics;
import com.syntech.distcomp.lab62.graphics.Color;
import org.ice1000.jimgui.*;
import org.ice1000.jimgui.flag.JImWindowFlags;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Board {

    private final int height, width;
    private int[][] field, newField;
    private int[] updateCount;
    private CyclicBarrier threadBarrier;
    private CyclicBarrier displayBarrier;
    private final Color[] colors;

    public Board(int height, int width, int colors) {
        this.height = height;
        this.width = width;
        this.field = new int[height][width];
        this.newField = new int[height][width];
        this.updateCount = new int[colors];
        this.threadBarrier = new CyclicBarrier(colors, () -> {
            synchronized(this) {
                for (int row = 0; row < height; row++) {
                    System.arraycopy(newField[row], 0, field[row], 0, width);
                    Arrays.fill(newField[row], -1);
                }
            }
            try {
                this.displayBarrier.await();
                //Thread.sleep(100);
            } catch (InterruptedException | BrokenBarrierException ignored) {}
        });
        this.displayBarrier = new CyclicBarrier(2);
        this.colors = new Color[]{
                Color.BOARD_BG,
                Color.RED,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.MAGENTA,
                Color.BLACK
        };
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                field[row][col] = Math.random() < 0.75 ? 0 : (int) (Math.random() * colors + 1);
            }
            Arrays.fill(newField[row], -1);
        }
    }

    public synchronized void display(@NotNull JImGui imGui, String name, float size) {
        float spacingX = imGui.getStyle().getItemSpacingX();
        float spacingY = imGui.getStyle().getItemSpacingY();
        float paddingX = imGui.getStyle().getFramePaddingX();
        float paddingY = imGui.getStyle().getFramePaddingY();

        imGui.getStyle().setItemSpacingX(0);
        imGui.getStyle().setItemSpacingY(0);
        imGui.getStyle().setFramePaddingX(0);
        imGui.getStyle().setFramePaddingY(0);

        imGui.begin(name, new NativeBool(), JImWindowFlags.NoMove | JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
        for (int row = height - 1; row >= 0; row--) {
            for (int col = 0; col < width; col++) {
                displayCell(imGui, size, row, col);
                if (col < width - 1) imGui.sameLine();
            }
        }
        JImGuiGen.end();

        imGui.getStyle().setItemSpacingX(spacingX);
        imGui.getStyle().setItemSpacingY(spacingY);
        imGui.getStyle().setFramePaddingX(paddingX);
        imGui.getStyle().setFramePaddingY(paddingY);
    }

    public void await() {
        try {
            displayBarrier.await();
        } catch (InterruptedException | BrokenBarrierException ignored) {
        }
    }

    private void displayCell(@NotNull JImGui imGui, float size, int row, int col) {
        //Graphics.display(imGui, drawables[get(row, col)], size, Color.BOARD_BG, col * height + row);
        Graphics.display(imGui, size, colors[get(row, col)], col * height + row);
    }

    private int get(int row, int col) {
        try {
            return field[row][col];
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }

    private void set(int piece, int thread, int row, int col) throws InterruptedException, BrokenBarrierException {
        synchronized(this) {
            if (piece != -1 && newField[row][col] == -1) newField[row][col] = piece;
        }
        updateCount[thread]++;
        if (updateCount[thread] == width * height) {
            updateCount[thread] = 0;
            threadBarrier.await();
        }
    }

    public void update(int thread, int row, int col) throws InterruptedException, BrokenBarrierException {
        int neighborCount = 0;
        boolean colorMet = false;
        int birthCell = thread + 1;
        synchronized(this) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (!(get(row + i, col + j) == 0)) {
                        if (!(i == 0 && j == 0)) {
                            neighborCount++;
                            if (get(row + i, col + j) == thread + 1) colorMet = true;
                        } else {
                            birthCell = get(row + i, col + j);
                        }
                    }
                }
            }
        }
        if (neighborCount == 3 && colorMet) set(birthCell, thread, row, col);
        else if (neighborCount == 3) set(-1, thread, row, col);
        else if (neighborCount < 2 || neighborCount > 3) set(0, thread, row, col);
        else set(get(row, col), thread, row, col);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
