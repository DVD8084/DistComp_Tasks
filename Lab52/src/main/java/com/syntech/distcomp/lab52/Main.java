package com.syntech.distcomp.lab52;

import org.ice1000.jimgui.*;
import org.ice1000.jimgui.flag.JImWindowFlags;
import org.ice1000.jimgui.util.JniLoaderEx;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;


public class Main {

    private static class LetterString {
        String value = "";

        static int counter = 0;
        int id;

        static int length = 24;

        LetterString() {
            StringBuilder sb = new StringBuilder();
            int abCount = 0;
            int cdCount = 0;
            for (int i = 0; i < length; i++) {
                int chr = (int) (Math.random() * 4);
                if (chr % 2 == 0) { abCount++; } else { cdCount++; }
                if (abCount == length - length / 2) chr = (int) (Math.random() * 2) + 2;
                if (cdCount == length - length / 2) chr = (int) (Math.random() * 2);
                sb.append((char) (chr + 'A'));
            }
            sb.append(' ');
            value = sb.toString();
            id = counter++;
        }

        void display(@NotNull JImGui imGui) {
            imGui.setWindowPos(String.valueOf(id), 30, 30 + 60 * id);
            imGui.begin(String.valueOf(id), new NativeBool(), JImWindowFlags.NoMove | JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
            imGui.text(value + String.format("(A%d, B%d)", count('A'), count('B')));
            JImGuiGen.end();
        }

        void tweak() {
            int chr = (int) (Math.random() * (length - 1)) + 1;
            switch (value.charAt(chr)) {
                case 'A': value = value.substring(0, chr) + 'C' + value.substring(chr + 1); break;
                case 'B': value = value.substring(0, chr) + 'D' + value.substring(chr + 1); break;
                case 'C': value = value.substring(0, chr) + 'A' + value.substring(chr + 1); break;
                case 'D': value = value.substring(0, chr) + 'B' + value.substring(chr + 1); break;
            }
        }

        long count(char chr) {
            return value.chars().filter(ch -> ch == chr).count();
        }

        boolean stopCondition() {
            return count('A') == count('B');
        }
    }

    private static class LetterThread extends Thread {
        LetterString[] strings;
        CyclicBarrier barrier;
        AtomicBoolean shouldRun;

        static int counter = 0;
        int id;

        LetterThread(LetterString[] strings, CyclicBarrier barrier, AtomicBoolean shouldRun) {
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
        int width = 1080, height = 720;
        try (JImGui gui = new JImGui(width, height, "Lab52")) {

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

            LetterString[] strings = new LetterString[amount];
            for (int i = 0; i < amount; i++) {
                strings[i] = new LetterString();
            }

            AtomicBoolean shouldRun = new AtomicBoolean(true);

            CyclicBarrier barrier = new CyclicBarrier(strings.length, () -> {
                int count = 0;
                for (LetterString string : strings) {
                    if (string.stopCondition()) {
                        count++;
                    }
                }
                shouldRun.set(count < 3);
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

                for (LetterString string : strings) {
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
