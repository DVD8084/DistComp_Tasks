package com.syntech.distcomp.lab53;

import org.ice1000.jimgui.*;
import org.ice1000.jimgui.flag.JImWindowFlags;
import org.ice1000.jimgui.util.JniLoaderEx;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;


public class Main {

    private static class NumberArray {
        int[] values;

        static int counter = 0;
        int id;

        static int length = 10;
        static int min = 10;
        static int max = 37;

        NumberArray() {
            values = new int[length];
            for (int i = 0; i < length; i++) {
                values[i] = (int) (Math.random() * (max - min) + min);
            }
            id = counter++;
            if (sum() % 2 != 0) tweak();
        }

        void display(@NotNull JImGui imGui) {
            imGui.setWindowPos(String.valueOf(id), 30, 30 + 60 * id);
            imGui.begin(String.valueOf(id), new NativeBool(), JImWindowFlags.NoMove | JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
            StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < length; i++) {
                sb.append(values[i]);
                sb.append(i == length - 1 ? " = " : " + ");
            }
            sb.append(sum());
            imGui.text(sb.toString());
            JImGuiGen.end();
        }

        void tweak() {
            int index = (int) (Math.random() * length);
            if ((Math.random() < 0.5 && values[index] < max) || values[index] == min) {
                values[index]++;
            } else {
                values[index]--;
            }
        }

        int sum() {
            return Arrays.stream(values).sum();
        }
    }

    private static class LetterThread extends Thread {
        NumberArray[] strings;
        CyclicBarrier barrier;
        AtomicBoolean shouldRun;

        static int counter = 0;
        int id;

        LetterThread(NumberArray[] strings, CyclicBarrier barrier, AtomicBoolean shouldRun) {
            this.strings = strings;
            this.barrier = barrier;
            this.shouldRun = shouldRun;
            id = counter++;
        }

        @Override
        public void run() {
            try {
                barrier.await();
                while (shouldRun.get()) {
                    strings[id].tweak();
                    barrier.await();
                }
            } catch (InterruptedException | BrokenBarrierException ignored) {
            }
        }
    }

    private JImGui imGui;

    public Main() {
        JniLoaderEx.loadGlfw();
        int width = 1125, height = 720;
        try (JImGui gui = new JImGui(width, height, "Lab53")) {

            imGui = gui;

            String fontName = "consola.ttf";
            File fontFile = new File(fontName);
            if (!fontFile.exists()) {
                String fontPath = "fonts/" + fontName;
                InputStream fontInput = Main.class.getClassLoader().getResourceAsStream(fontPath);
                assert fontInput != null;
                Files.copy(fontInput, fontFile.getAbsoluteFile().toPath());
            }

            NativeShort glyphRange = imGui.getIO().getFonts().getGlyphRangesForCyrillic();

            JImFont font = imGui.getIO().getFonts().addFontFromFile(fontName, 36, glyphRange);

            int amount = 4;

            NumberArray[] strings = new NumberArray[amount];
            for (int i = 0; i < amount; i++) {
                strings[i] = new NumberArray();
            }

            AtomicBoolean shouldRun = new AtomicBoolean(true);

            CyclicBarrier barrier = new CyclicBarrier(strings.length, () -> {
                boolean equal = true;
                for (NumberArray string : strings) {
                    if (string.sum() != strings[0].sum()) {
                        equal = false;
                        break;
                    }
                }
                shouldRun.set(!equal);
            });

            LetterThread[] threads = new LetterThread[amount];
            for (int i = 0; i < amount; i++) {
                threads[i] = new LetterThread(strings, barrier, shouldRun);
            }

            boolean started = false;

            imGui.initBeforeMainLoop();
            while (!imGui.windowShouldClose()) {

                imGui.initNewFrame();

                if (!started) {
                    imGui.begin("Start", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                    if (imGui.button("Поехали!")) {
                        for (LetterThread thread : threads) {
                            thread.start();
                        }
                        started = true;
                    }
                    JImGuiGen.end();
                }

                for (NumberArray string : strings) {
                    string.display(imGui);
                }

                imGui.render();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        new Main();
    }
}
