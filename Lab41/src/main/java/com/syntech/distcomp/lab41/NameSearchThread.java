package com.syntech.distcomp.lab41;

import java.io.IOException;

public class NameSearchThread extends DBThread {

    public NameSearchThread(Database database) {
        super(database);
    }

    public void run() {
        while (true) {
            try {
                String name = getRandomName();
                String phone = database.findPhone(name);
                if (phone == null) {
                    log("Could not find number of " + name);
                } else {
                    log("Found number of " + name + ": " + phone);
                }
                sleep((long) (Math.random() * 1000 + 1000));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
