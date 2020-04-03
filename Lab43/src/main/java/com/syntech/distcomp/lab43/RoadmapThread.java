package com.syntech.distcomp.lab43;


public abstract class RoadmapThread extends Thread {

    protected Roadmap roadmap;
    private static int counter = 0;
    private int id;

    public RoadmapThread(Roadmap roadmap) {
        this.roadmap = roadmap;
        this.id = counter++;
    }

    protected void log(String message, Object... args) {
        System.out.printf(String.format("[%s #%d] %s\n", this.getClass().getTypeName(), id, message), args);
    }

}
