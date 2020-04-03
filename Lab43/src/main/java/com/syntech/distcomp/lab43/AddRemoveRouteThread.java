package com.syntech.distcomp.lab43;

import java.io.IOException;

public class AddRemoveRouteThread extends RoadmapThread {

    public AddRemoveRouteThread(Roadmap roadmap) {
        super(roadmap);
    }

    public void run() {
        int routesAdded = 0;
        while (true) {
            try {
                if (Math.random() * routesAdded < 2) {
                    int[] args = roadmap.randomAddRoute();
                    log("Added new route from city #%d to city #%d with cost %d", args[0], args[1], args[2]);
                    routesAdded++;
                } else {
                    int[] args = roadmap.randomRemoveRoute();
                    log("Removed an old route from city #%d to city #%d", args[0], args[1]);
                    routesAdded--;
                }
                sleep((long) (Math.random() * 2000 + 2000));
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
