package com.syntech.distcomp.lab1;

import org.ice1000.jimgui.*;
import org.ice1000.jimgui.flag.JImWindowFlags;
import org.ice1000.jimgui.util.JniLoaderEx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;


public class Main {

    private static Integer percentage = 50;

    private static JImGui imGui;

    private static SliderThread thread1, thread2;

    private static NativeInt priority1, priority2;

    private static JImVec4 InactiveColor;
    private static JImVec4 ActiveColor;

    private static int semaphore = 0;

    private static int window = 1;

    private static boolean shouldDisplayPopup = false;

    public Main() {

        thread1 = new SliderThread(10);
        thread2 = new SliderThread(90);

        JniLoaderEx.loadGlfw();

        priority1 = new NativeInt();
        priority2 = new NativeInt();
        priority1.modifyValue(Thread.NORM_PRIORITY);
        priority2.modifyValue(Thread.NORM_PRIORITY);

        InactiveColor = new JImVec4(0.1f, 0.2f, 0.3f, 1.0f);
        ActiveColor = new JImVec4(0.2f, 0.4f, 0.6f, 1.0f);

        int width = 1080, height = 720;
        try (JImGui gui = new JImGui(width, height, "Project")) {

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

            JImFont font = imGui.getIO().getFonts().addFontFromFile(fontName, 16, glyphRange);

            imGui.initBeforeMainLoop();

            while (!imGui.windowShouldClose()) {

                imGui.initNewFrame();

                drawSlider();
                switch (window) {
                    case 1:
                        drawWindowA();
                        break;
                    case 2:
                        drawWindowB();
                        break;
                    default:
                        break;
                }
                drawPopup();

                imGui.render();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        thread1.interrupt();
        thread2.interrupt();

    }

    private static void drawWindowA() {
        imGui.begin("WindowA", new NativeBool(), JImWindowFlags.AlwaysAutoResize);

        imGui.inputInt("Thread 1 Priority", priority1);
        imGui.inputInt("Thread 2 Priority", priority2);

        if (priority1.accessValue() < Thread.MIN_PRIORITY) priority1.modifyValue(Thread.MIN_PRIORITY);
        if (priority1.accessValue() > Thread.MAX_PRIORITY) priority1.modifyValue(Thread.MAX_PRIORITY);
        if (priority2.accessValue() < Thread.MIN_PRIORITY) priority2.modifyValue(Thread.MIN_PRIORITY);
        if (priority2.accessValue() > Thread.MAX_PRIORITY) priority2.modifyValue(Thread.MAX_PRIORITY);

        thread1.setPriority(priority1.accessValue());
        thread2.setPriority(priority2.accessValue());

        if (thread1.isRunning() || thread2.isRunning()) {
            if (imGui.button("STOP")) {
                thread1.interrupt();
                thread2.interrupt();
                thread1 = new SliderThread(10);
                thread2 = new SliderThread(90);
                thread1.setPriority(priority1.accessValue());
                thread2.setPriority(priority2.accessValue());
            }
        } else {
            if (imGui.button("START")) {
                thread1.start();
                thread2.start();
            }
            imGui.sameLine();
            if (imGui.button("SWITCH")) {
                window = 2;
            }
        }

        JImGuiGen.end();
    }

    private static void drawWindowB() {
        imGui.begin("WindowB", new NativeBool(),JImWindowFlags.AlwaysAutoResize);

        if (imGui.button("START 1")) {
            if (semaphore == 0) {
                semaphore++;
                thread1.start();
            } else {
                shouldDisplayPopup = true;
            }
        }
        imGui.sameLine();
        if (imGui.button("START 2")) {
            if (semaphore == 0) {
                semaphore++;
                thread2.start();
            } else {
                shouldDisplayPopup = true;
            }
        }
        imGui.sameLine();
        if (thread1.isRunning() && imGui.button("STOP 1")) {
            thread1.interrupt();
            thread1 = new SliderThread(10);
            thread1.setPriority(Thread.MIN_PRIORITY);
            semaphore--;
        } else if (thread2.isRunning() && imGui.button("STOP 2")) {
            thread2.interrupt();
            thread2 = new SliderThread(90);
            thread2.setPriority(Thread.MAX_PRIORITY);
            semaphore--;
        } else if (semaphore == 0 && imGui.button("SWITCH")) {
            window = 1;
        }

        JImGuiGen.end();
    }

    private static void drawSlider() {
        imGui.begin("Slider", new NativeBool(),
                JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);

        imGui.getStyle().setItemSpacingX(0);
        imGui.getStyle().setFramePaddingX(1);

        synchronized(percentage) {
            int p = percentage;

            if (p > 0) {
                imGui.pushStyleColor(JImStyleColors.Button, ActiveColor);
                imGui.pushStyleColor(JImStyleColors.ButtonHovered, ActiveColor);
                imGui.pushStyleColor(JImStyleColors.ButtonActive, ActiveColor);
            }

            for (int i = 0; i < 100; i++) {

                JImGui.pushID(i);
                imGui.button("");
                JImGuiGen.popID();
                imGui.sameLine();

                if (i + 1 == p) {
                    JImGuiGen.popStyleColor(3);
                    imGui.pushStyleColor(JImStyleColors.Button, InactiveColor);
                    imGui.pushStyleColor(JImStyleColors.ButtonHovered, InactiveColor);
                    imGui.pushStyleColor(JImStyleColors.ButtonActive, InactiveColor);
                }

            }

            JImGuiGen.popStyleColor(3);

            imGui.getStyle().setItemSpacingX(5);
            imGui.getStyle().setFramePaddingX(5);

            imGui.text(String.format(" [ %d ]", percentage));
        }

        JImGuiGen.end();
    }


    private static void drawPopup() {
        NativeBool alwaysTrue = new NativeBool();
        alwaysTrue.modifyValue(true);
        if (shouldDisplayPopup) imGui.openPopup("Blocked");
        if (imGui.beginPopupModal("Blocked", alwaysTrue, JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize)) {
            imGui.text("Access blocked by thread!");
            if (imGui.button("OK")) JImGuiGen.closeCurrentPopup();
            JImGuiGen.end();
        }
        if (!imGui.isPopupOpen("Blocked")) {
            shouldDisplayPopup = false;
        }
    }

    private static class SliderThread extends Thread {

        private int value = 50;

        private boolean running = false;

        public SliderThread(int value) {
            if (value < 0) value = 0;
            if (value > 100) value = 100;
            this.value = value;
        }

        public void run() {
            running = true;
            while (true) {
                synchronized (percentage) {
                    int p = percentage;
                    percentage = (p < value) ? p + 1 : (p > value) ? p - 1 : p;
                    try {
                        sleep(10);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
            running = false;
        }

        public boolean isRunning() {
            return running;
        }

    }

    public static void main(String... args) {
        new Main();
    }
}
