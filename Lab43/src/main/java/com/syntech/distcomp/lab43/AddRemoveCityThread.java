package com.syntech.distcomp.lab43;

public class AddRemoveCityThread extends RoadmapThread {

    public AddRemoveCityThread(Roadmap roadmap) {
        super(roadmap);
    }

    public void run() {
        int citiesAdded = 0;
        while (true) {
            try {
                if (Math.random() * citiesAdded < 3) {
                    int[] args = roadmap.randomAddCity();
                    log("Added new city #%d", args[0]);
                    citiesAdded++;
                } else {
                    int[] args = roadmap.randomRemoveCity();
                    log("Removed an old city #%d, moving the indexes down a bit", args[0]);
                    citiesAdded--;
                }
                sleep((long) (Math.random() * 5000 + 5000));
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
