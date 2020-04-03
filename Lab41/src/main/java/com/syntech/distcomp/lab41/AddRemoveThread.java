package com.syntech.distcomp.lab41;

import java.io.IOException;

public class AddRemoveThread extends DBThread {

    public AddRemoveThread(Database database) {
        super(database);
    }

    public void run() {
        int entriesAdded = 0;
        while (true) {
            try {
                if (Math.random() * entriesAdded < 2) {
                    database.addEntry(getRandomName(), getRandomPhone());
                    log("Added new entry");
                    entriesAdded++;
                } else {
                    database.removeEntry(0);
                    log("Removed an old entry");
                    entriesAdded--;
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
