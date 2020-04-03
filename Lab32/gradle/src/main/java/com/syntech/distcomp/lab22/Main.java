package com.syntech.distcomp.lab22;

import org.ice1000.jimgui.*;
import org.ice1000.jimgui.flag.JImWindowFlags;
import org.ice1000.jimgui.util.JniLoaderEx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;


public class Main {

    private JImGui imGui;

    private static class Soldier extends Thread {

        Stash from, to;
        int speed;
        Soldier(Stash from, Stash to, int speed) {
            this.from = from;
            this.to = to;
            this.speed = speed;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    to.put(from.get());
                    sleep((long) (Math.random() * speed * 2 + speed));
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private static class Seller extends Thread {

        AtomicInteger totalMoney = new AtomicInteger(0);
        Stash from;
        int speed;
        Seller(Stash from, int speed) {
            this.from = from;
            this.speed = speed;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    totalMoney.addAndGet(from.get().getCost());
                    sleep((long) (Math.random() * speed * 2 + speed));
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public int getTotalMoney() {
            return totalMoney.get();
        }
    }

    public Main() {

        Thing[] warehouseThings = new Thing[]{
                new Thing("Лопата", 100),
                new Thing("Мешок картохи", 20),
                new Thing("Нож кухонный", 150),
                new Thing("Топорик маникюрный", 200),
                new Thing("Трёхстволка", 2500),
                new Thing("Гранатомёт одноручный", 10000),
                new Thing("Ракетница (с глушителем)", 12500),
                new Thing("Кубический метр обогащённого урана", 100000),
                new Thing("БТР-10.24", 3500000),
                new Thing("Машинка для генерации идиотских названий б/у", 125)
        };

        Stash warehouse = new Stash(warehouseThings.length);
        for (Thing thing : warehouseThings) {
            warehouse.put(thing);
        }
        Stash ivanovStash = new Stash(3);
        Stash petrovStash = new Stash(5);
        Soldier ivanov = new Soldier(warehouse, ivanovStash, 250);
        Soldier petrov = new Soldier(ivanovStash, petrovStash, 500);
        Seller theSeller = new Seller(petrovStash, 1000);

        boolean started = false;

        JniLoaderEx.loadGlfw();
        int width = 1080, height = 720;
        try (JImGui gui = new JImGui(width, height, "Lab22")) {

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

                if (!started) {
                    imGui.begin("Start", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                    if (imGui.button("Поехали!")) {
                        ivanov.start();
                        petrov.start();
                        theSeller.start();
                        started = true;
                    }
                    JImGuiGen.end();
                }

                imGui.begin("Warehouse", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                imGui.text("На складе:");
                warehouse.display(imGui);
                JImGuiGen.end();

                imGui.begin("Ivanov", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                imGui.text("Иванов несёт в руках:");
                ivanovStash.display(imGui);
                JImGuiGen.end();

                imGui.begin("Petrov", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                imGui.text("Петров засовывает в грузовик:");
                petrovStash.display(imGui);
                JImGuiGen.end();

                imGui.begin("TheSeller", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                imGui.text(String.format("Нечепорчук насчитал аж %d грывень! Ого!", theSeller.getTotalMoney()));
                JImGuiGen.end();

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
