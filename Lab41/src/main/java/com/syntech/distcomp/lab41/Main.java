package com.syntech.distcomp.lab41;

import java.io.IOException;

public class Main {

    public Main() throws InterruptedException, IOException {
        Database db = new Database("db.txt");

        for (int i = 0; i < 8; i++) {
            switch ((int) (Math.random() * 4)) {
                case 0: new NameSearchThread(db).start(); break;
                case 1: new PhoneSearchThread(db).start(); break;
                default: new AddRemoveThread(db).start(); break;
            }
            Thread.sleep((long) (Math.random() * 50 + 100));
        }
    }

    public static void main(String... args) throws InterruptedException, IOException {
        new Main();
    }
}
