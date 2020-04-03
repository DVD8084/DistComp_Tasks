package com.syntech.distcomp.lab43;

public class FindRouteThread extends RoadmapThread {

    public FindRouteThread(Roadmap roadmap) {
        super(roadmap);
    }

    public void run() {
        while (true) {
            try {
                int[] args = roadmap.randomGetCost();
                if (args[2] == -1) {
                    log("Route from city #%d to city #%d not found!", args[0], args[1]);
                } else {
                    log("Route from city #%d to city #%d costs %d", args[0], args[1], args[2]);
                }
                sleep((long) (Math.random() * 1000 + 1000));
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
