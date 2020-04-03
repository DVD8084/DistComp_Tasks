package com.syntech.distcomp.lab43;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Roadmap {
    private static class Route {
        int cost;
        int destination;

        private Route(int cost, int destination) {
            this.cost = cost;
            this.destination = destination;
        }
    }

    ArrayList<LinkedList<Route>> routes;

    public Roadmap(int cityCount) {
        routes = new ArrayList<>();
        routes.ensureCapacity(cityCount);
        for (int i = 0; i < cityCount; i++) {
            routes.add(new LinkedList<>());
        }
    }

    private boolean routeExists(int from, int to) {
        if (from == to) return true;
        for (Route route : routes.get(from)) {
            if (route.destination == to) {
                return true;
            }
        }
        return false;
    }

    public synchronized void setCost(int from, int to, int cost) {
        for (Route route : routes.get(from)) {
            if (route.destination == to) {
                route.cost = cost;
            }
        }
        for (Route route : routes.get(to)) {
            if (route.destination == from) {
                route.cost = cost;
            }
        }
    }

    public synchronized void addRoute(int from, int to, int cost) {
        if (!routeExists(from, to)) {
            routes.get(from).add(new Route(cost, to));
            routes.get(to).add(new Route(cost, from));
        }
    }

    public synchronized void removeRoute(int from, int to) {
        routes.get(from).removeIf(route -> route.destination == to);
        routes.get(to).removeIf(route -> route.destination == from);
    }

    public synchronized void addCity() {
        routes.add(new LinkedList<>());
    }

    public synchronized void removeCity(int i) {
        routes.remove(i);
        for (LinkedList<Route> routeList : routes) {
            routeList.removeIf(route -> route.destination == i);
            for (Route route : routeList) {
                if (route.destination > i) route.destination--;
            }
        }
    }

    public synchronized int getCost(int from, int to) {
        if (from == to) return 0;
        boolean searchStopped = true;
        int[] costs = new int[routes.size()];
        int[] lengths = new int[routes.size()];
        int length = 1;
        Arrays.fill(costs, -1);
        costs[from] = 0;
        for (Route route : routes.get(from)) {
            if (costs[route.destination] == -1) {
                searchStopped = false;
                if (route.destination == to) {
                    return route.cost;
                }
                costs[route.destination] = route.cost;
                lengths[route.destination] = length;
            }
        }
        while (!searchStopped) {
            searchStopped = true;
            length++;
            for (int i = 0; i < routes.size(); i++) {
                if (lengths[i] == length - 1) {
                    for (Route route : routes.get(i)) {
                        if (costs[route.destination] == -1) {
                            searchStopped = false;
                            if (route.destination == to) {
                                return costs[i] + route.cost;
                            }
                            costs[route.destination] = costs[i] + route.cost;
                            lengths[route.destination] = length;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public synchronized int[] randomAddRoute() {
        int[] args = new int[]{
                (int)(Math.random() * routes.size()),
                (int)(Math.random() * routes.size()),
                (int)(Math.random() * 10 + 5)
        };
        while (routeExists(args[0], args[1])) {
            args = new int[]{
                    (int)(Math.random() * routes.size()),
                    (int)(Math.random() * routes.size()),
                    (int)(Math.random() * 10 + 5)
            };
        }
        addRoute(args[0], args[1], args[2]);
        return args;
    }

    public synchronized int[] randomRemoveRoute() {
        int[] args = new int[]{
                (int)(Math.random() * routes.size()),
                (int)(Math.random() * routes.size())
        };
        while (!routeExists(args[0], args[1])) {
            args = new int[]{
                    (int)(Math.random() * routes.size()),
                    (int)(Math.random() * routes.size()),
                    (int)(Math.random() * 10 + 5)
            };
        }
        removeRoute(args[0], args[1]);
        return args;
    }

    public synchronized int[] randomSetCost() {
        int[] args = new int[]{
                (int)(Math.random() * routes.size()),
                (int)(Math.random() * routes.size()),
                (int)(Math.random() * 10 + 5)
        };
        while (!routeExists(args[0], args[1])) {
            args = new int[]{
                    (int)(Math.random() * routes.size()),
                    (int)(Math.random() * routes.size()),
                    (int)(Math.random() * 10 + 5)
            };
        }
        setCost(args[0], args[1], args[2]);
        return args;
    }

    public synchronized int[] randomAddCity() {
        addCity();
        return new int[]{routes.size() - 1};
    }

    public synchronized int[] randomRemoveCity() {
        int[] args = new int[]{(int)(Math.random() * routes.size())};
        removeCity(args[0]);
        return args;
    }

    public synchronized int[] randomGetCost() {
        int[] args = new int[]{
                (int)(Math.random() * routes.size()),
                (int)(Math.random() * routes.size()),
                0
        };
        args[2] = getCost(args[0], args[1]);
        return args;
    }

}
