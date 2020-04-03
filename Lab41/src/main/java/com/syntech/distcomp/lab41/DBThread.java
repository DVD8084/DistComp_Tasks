package com.syntech.distcomp.lab41;


public abstract class DBThread extends Thread {

    protected Database database;
    private static int counter = 0;
    private int id;

    public DBThread(Database database) {
        this.database = database;
        this.id = counter++;
    }

    protected String getRandomPhone() {
        StringBuilder phone = new StringBuilder("38044555");
        for (int i = 0; i < 4; i++) {
            phone.append(Math.random() > 0.5 ? '1' : '0');
        }
        return phone.toString();
    }

    protected String getRandomName() {
        String name, surname;
        switch ((int) (Math.random() * 4)) {
            case 0: name = "John"; break;
            case 1: name = "Rose"; break;
            case 2: name = "Dave"; break;
            case 3: name = "Jade"; break;
            default: name = "Lord"; break;
        }
        switch ((int) (Math.random() * 4)) {
            case 0: surname = "Egbert"; break;
            case 1: surname = "Lalonde"; break;
            case 2: surname = "Strider"; break;
            case 3: surname = "Harley"; break;
            default: surname = "English"; break;
        }
        return name + " " + surname;
    }

    protected void log(String message) {
        System.out.println(String.format("[%s #%d] %s", this.getClass().getTypeName(), id, message));
    }

}
