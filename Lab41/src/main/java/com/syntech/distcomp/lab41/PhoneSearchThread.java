package com.syntech.distcomp.lab41;

import java.io.IOException;

public class PhoneSearchThread extends DBThread {

    public PhoneSearchThread(Database database) {
        super(database);
    }

    public void run() {
        while (true) {
            try {
                String phone = getRandomPhone();
                String name = database.findName(phone);
                if (name == null) {
                    log("Could not find owner of number " + phone);
                } else {
                    log("Found owner of number " + phone + ": " + name);
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
