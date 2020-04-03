package com.syntech.distcomp.lab43;

public class ChangeCostThread extends RoadmapThread {

    public ChangeCostThread(Roadmap roadmap) {
        super(roadmap);
    }

    public void run() {
        while (true) {
            try {
                int[] args = roadmap.randomSetCost();
                log("Changed route cost from city #%d to city #%d to %d", args[0], args[1], args[2]);
                sleep((long) (Math.random() * 2000 + 1500));
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
