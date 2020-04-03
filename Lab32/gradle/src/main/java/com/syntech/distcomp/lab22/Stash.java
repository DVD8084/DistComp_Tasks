package com.syntech.distcomp.lab22;

import org.ice1000.jimgui.JImGui;

import java.util.ArrayList;

public class Stash {

    private int maxSize;

    public Stash(int maxSize) {
        stash = new ArrayList<>(maxSize);
        this.maxSize = maxSize;
    }

    private ArrayList<Thing> stash;

    private synchronized int getStashSize() {
        return stash.size();
    }

    public synchronized Thing get() {
        while (getStashSize() <= 0) {
            try {
                wait();
            }
            catch (InterruptedException ignored) {
            }
        }
        Thing thing = stash.remove(0);
        notify();
        return thing;
    }

    public synchronized void put(Thing thing) {
        while (getStashSize() >= maxSize) {
            try {
                wait();
            }
            catch (InterruptedException ignored) {
            }
        }
        stash.add(thing);
        notify();
    }

    public synchronized void display(JImGui imGui) {
        if (stash.size() == 0) {
            imGui.text("  ничегошеньки!");
        } else for (Thing thing : stash) {
            imGui.text(String.format("  %s (%d грывень)", thing.getName(), thing.getCost()));
        }
    }
}
