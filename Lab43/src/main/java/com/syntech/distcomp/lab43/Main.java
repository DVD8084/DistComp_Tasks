package com.syntech.distcomp.lab43;

public class Main {

    public Main() throws InterruptedException {
        Roadmap r = new Roadmap(5);
        for (int i = 0; i < 4; i++) {
            int[] args = r.randomAddRoute();
            System.out.printf("Added new route from city #%d to city #%d with cost %d\n", args[0], args[1], args[2]);
        }
        for (int i = 0; i < 5; i++) {
            switch (i) {
                case 0: new ChangeCostThread(r).start(); break;
                case 1: new AddRemoveRouteThread(r).start(); break;
                case 2: new AddRemoveCityThread(r).start(); break;
                default: new FindRouteThread(r).start(); break;
            }
            Thread.sleep((long) (Math.random() * 200 + 100));
        }

    }

    public static void main(String... args) throws InterruptedException {
        new Main();
    }
}
