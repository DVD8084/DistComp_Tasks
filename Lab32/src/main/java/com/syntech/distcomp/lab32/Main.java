package com.syntech.distcomp.lab32;

import org.ice1000.jimgui.*;
import org.ice1000.jimgui.flag.JImWindowFlags;
import org.ice1000.jimgui.util.JniLoaderEx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;


public class Main {

    private JImGui imGui;

    private static class Barber extends Thread {

        private final Semaphore semaphore;
        private int speed;
        private LinkedList<Customer> queue;
        private Customer currentCustomer;
        Barber(int speed, Semaphore semaphore, LinkedList<Customer> queue) {
            this.speed = speed;
            this.semaphore = semaphore;
            this.queue = queue;
            this.currentCustomer = null;
        }

        @Override
        public void run() {
            while (true) {
                if (queue.isEmpty()) {
                    try {
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        break;
                    }
                } else {
                    try {
                        sleep((long) (Math.random() * speed / 10 + speed / 5));
                        currentCustomer = queue.poll();
                        sleep((long) (Math.random() * speed * 2 + speed));
                        currentCustomer.wakeUp();
                        currentCustomer = null;
                        sleep((long) (Math.random() * speed / 10 + speed / 5));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void wakeUp() {
            semaphore.release();
        }

        public Customer getCurrentCustomer() {
            return currentCustomer;
        }
    }

    private static class Customer extends Thread {

        private final Semaphore semaphore;
        private AtomicInteger reviewBook;
        private LinkedList<Customer> queue;
        private Barber barber;

        Customer(Semaphore semaphore, LinkedList<Customer> queue, Barber barber, AtomicInteger reviewBook) {
            this.semaphore = semaphore;
            this.queue = queue;
            this.barber = barber;
            this.reviewBook = reviewBook;
        }

        @Override
        public void run() {
            queue.addLast(this);
            try {
                semaphore.acquire();
                barber.wakeUp();
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            reviewBook.incrementAndGet();
        }

        public void wakeUp() {
            semaphore.release();
        }
    }

    public Main(int barberSpeed, int customerSpeed) {

        LinkedList<Customer> queue = new LinkedList<>();
        AtomicInteger reviewBook = new AtomicInteger();
        Barber barber = new Barber(barberSpeed, new Semaphore(1), queue);

        boolean started = false;

        JniLoaderEx.loadGlfw();
        int width = 1280, height = 720;
        try (JImGui gui = new JImGui(width, height, "Lab32")) {

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

            JImFont font = imGui.getIO().getFonts().addFontFromFile(fontName, 30, glyphRange);

            imGui.initBeforeMainLoop();

            long startTime = System.currentTimeMillis();
            long nextCustomerTime = (long) (Math.random() * customerSpeed * 2 + customerSpeed);

            while (!imGui.windowShouldClose()) {

                imGui.initNewFrame();

                if (!started) {
                    imGui.begin("Start", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                    if (imGui.button("Поехали!")) {
                        barber.start();
                        started = true;
                    }
                    JImGuiGen.end();
                } else if (System.currentTimeMillis() - startTime > nextCustomerTime) {
                    startTime = System.currentTimeMillis();
                    nextCustomerTime = (long) (Math.random() * customerSpeed * 2 + customerSpeed);
                    new Customer(new Semaphore(1), queue, barber, reviewBook).start();
                }

                imGui.begin("Queue", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                imGui.text(String.format("Людей в очереди: %d", queue.size()));
                JImGuiGen.end();

                imGui.begin("Barber", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                imGui.text("Парикмахер " + (barber.getCurrentCustomer() != null ? "занят!" : "свободен!"));
                JImGuiGen.end();

                imGui.begin("ReviewBook", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                imGui.text(String.format("Приятных отзывов оставлено: %d", reviewBook.get()));
                JImGuiGen.end();

                imGui.render();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        new Main(1000, 1280);
    }
}
