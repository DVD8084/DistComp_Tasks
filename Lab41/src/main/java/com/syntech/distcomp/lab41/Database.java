package com.syntech.distcomp.lab41;

import java.io.*;

public class Database {
    private static class Entry {
        final String name;
        final String phone;

        private Entry(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }

        private Entry(String data) {
            this(data.substring(13), data.substring(0, 12));
        }
    }

    private File file;
    private BufferedReader reader;
    private BufferedWriter writer;

    public Database(String path) throws IOException {
        file = new File(path);
        if (!file.isFile()) {
            file.createNewFile();
        }
    }

    private Entry loadNextEntry() throws IOException {
        if (reader != null) {
            String entry = reader.readLine();
            if (entry != null) {
                return new Entry(entry);
            }
        }
        return null;
    }

    public synchronized void addEntry(String name, String phone) throws IOException {
        Entry entry = new Entry(name, phone);
        String path = file.getPath();
        File bak = new File(path + ".bak");
        file.renameTo(bak);
        reader = new BufferedReader(new FileReader(bak));
        writer = new BufferedWriter(new FileWriter(file));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            writer.write(line);
            writer.newLine();
        }
        writer.write(entry.phone + " " + entry.name);
        writer.newLine();
        reader.close();
        writer.close();
        bak.delete();
    }

    public synchronized void removeEntry(int id) throws IOException {
        String path = file.getPath();
        File bak = new File(path + ".bak");
        file.renameTo(bak);
        reader = new BufferedReader(new FileReader(bak));
        writer = new BufferedWriter(new FileWriter(file));
        int counter = 0;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (counter != id) {
                writer.write(line);
                writer.newLine();
            }
            counter++;
        }
        reader.close();
        writer.close();
        bak.delete();
    }

    public synchronized String findPhone(String name) throws IOException {
        reader = new BufferedReader(new FileReader(file));
        for (Entry entry = loadNextEntry(); entry != null; entry = loadNextEntry()) {
            if (entry.name.equals(name)) {
                reader.close();
                return entry.phone;
            }
        }
        reader.close();
        return null;
    }

    public synchronized String findName(String phone) throws IOException {
        reader = new BufferedReader(new FileReader(file));
        for (Entry entry = loadNextEntry(); entry != null; entry = loadNextEntry()) {
            if (entry.phone.equals(phone)) {
                reader.close();
                return entry.name;
            }
        }
        reader.close();
        return null;
    }
}
