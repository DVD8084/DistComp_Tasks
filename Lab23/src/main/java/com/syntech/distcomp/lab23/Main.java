package com.syntech.distcomp.lab23;

import org.ice1000.jimgui.*;
import org.ice1000.jimgui.flag.JImWindowFlags;
import org.ice1000.jimgui.util.JniLoaderEx;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;


public class Main {

    private JImGui imGui;

    private static class Monk {
        private String name;
        private int energy;
        private int team;
        public final int TEAM_YIN = 0;
        public final int TEAM_YANG = 1;

        public Monk(int team) {
            this.team = team;
            this.name = getRandomName();
            this.energy = (int) (Math.random() * 75 + 25);
        }

        public int getStrike() {
            return Math.max((int) (Math.random() * energy + 1), (int) (Math.random() * energy + 1));
        }

        public void display(JImGui imGui) {
            Color buttonColor = Color.GRAY;
            switch (team) {
                case TEAM_YIN: buttonColor = Color.BLACK; break;
                case TEAM_YANG: buttonColor = Color.GRAY; break;
            }
            imGui.pushStyleColor(JImStyleColors.Button, JImVec4.fromAWT(buttonColor));
            imGui.pushStyleColor(JImStyleColors.ButtonHovered, JImVec4.fromAWT(buttonColor));
            imGui.pushStyleColor(JImStyleColors.ButtonActive, JImVec4.fromAWT(buttonColor));
            StringBuilder text = new StringBuilder(String.format("%s (%d)", name, energy));
            while (text.length() < 16) {
                text.insert(0, ' ');
            }
            imGui.button(text.toString());
            JImGuiGen.popStyleColor(3);
        }

        private String getRandomName() {
            return getRandomNamePart() + ' ' + getRandomNamePart();
        }

        private String getRandomNamePart() {
            switch ((int) (Math.random() * 30)) {
                case 0: return "Ань";
                case 1: return "Бань";
                case 2: return "Вэнь";
                case 3: return "Гуань";
                case 4: return "Дань";
                case 5: return "Ень";
                case 6: return "Жань";
                case 7: return "Зень";
                case 8: return "Инь";
                case 9: return "Кань";
                case 10: return "Лань";
                case 11: return "Мэнь";
                case 12: return "Нань";
                case 13: return "Онь";
                case 14: return "Пьянь";
                case 15: return "Ронь";
                case 16: return "Сань";
                case 17: return "Тань";
                case 18: return "Унь";
                case 19: return "Фэнь";
                case 20: return "Хуань";
                case 21: return "Цинь";
                case 22: return "Чень";
                case 23: return "Шань";
                case 24: return "Щань";
                case 25: return "Ынь";
                case 26: return "Энь";
                case 27: return "Юнь";
                case 28: return "Янь";
                case 29: return "Ёнь";
            }
            return "Хрень"; // хотел бы я увидеть, как выполнится эта строчка, муахаха
        }
    }

    private static class TournamentTree {
        private Monk[] competitors;
        private int[] results;
        private TournamentTree[] children;

        private TournamentTree() {
            competitors = new Monk[]{
                    new Monk((int) (Math.random() * 2)),
                    new Monk((int) (Math.random() * 2))
            };
            results = new int[2];
            Arrays.fill(results, -1);
            children = new TournamentTree[]{};
        }

        private TournamentTree(TournamentTree left, TournamentTree right) {
            competitors = new Monk[2];
            results = new int[2];
            Arrays.fill(results, -1);
            children = new TournamentTree[]{left, right};
        }

        public TournamentTree(int tourAmount) {
            int matchAmount = (int) Math.pow(2, tourAmount - 1);
            LinkedList<TournamentTree> lastLayer = new LinkedList<>();
            for (int i = 0; i < matchAmount; i++) {
                lastLayer.add(new TournamentTree());
            }
            for (int i = 0; i < tourAmount - 2; i++) {
                matchAmount /= 2;
                LinkedList<TournamentTree> newLayer = new LinkedList<>();
                for (int j = 0; j < matchAmount; j++) {
                    newLayer.add(new TournamentTree(lastLayer.get(j * 2), lastLayer.get(j * 2 + 1)));
                }
                lastLayer = newLayer;
            }
            competitors = new Monk[2];
            results = new int[2];
            Arrays.fill(results, -1);
            children = new TournamentTree[]{lastLayer.get(0), lastLayer.get(1)};
        }

        private void display(JImGui imGui, int layer) {
            imGui.begin(String.format("%d", layer), new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
            for (int i = 0; i < competitors.length; i++) {
                if (competitors[i] != null) {
                    competitors[i].display(imGui);
                    if (results[i] != -1) {
                        imGui.sameLine();
                        imGui.text(String.valueOf(results[i]));
                    }
                }
            }
            imGui.text("\n");
            JImGuiGen.end();
            for (TournamentTree child : children) {
                child.display(imGui, layer + 1);
            }
        }

        public void display(JImGui imGui) {
            display(imGui, 0);
        }

        public TournamentTree child(int i) {
            return children[i];
        }

        public int childAmount() {
            return children.length;
        }

        public void addCompetitor(Monk monk, int i) {
            competitors[i] = monk;
        }

        public Monk match() {
            int winner = -1;
            int bestStrike = -1;
            boolean stillGoing = true;
            while (stillGoing) {
                winner = -1;
                bestStrike = -1;
                stillGoing = false;
                for (int i = 0; i < competitors.length; i++) {
                    results[i] = competitors[i].getStrike();
                    if (results[i] == bestStrike) {
                        stillGoing = true;
                    }
                    if (results[i] > bestStrike) {
                        winner = i;
                        bestStrike = results[i];
                    }
                }
            }
            return competitors[winner];
        }
    }

    private static class Match extends RecursiveTask<Monk> {
        private TournamentTree tree;

        @Override
        protected synchronized Monk compute() {
            LinkedList<Match> matches = new LinkedList<>();
            for (int i = 0; i < tree.childAmount(); i++) {
                Match m = new Match(tree.child(i));
                m.fork();
                matches.add(m);
            }
            for (int i = 0; i < matches.size(); i++) {
                tree.addCompetitor(matches.get(i).join(), i);
            }
            return tree.match();
        }

        public Match(TournamentTree tree) {
            this.tree = tree;
        }
    }

    public Main() {

        boolean started = false, done = false;

        TournamentTree tree = new TournamentTree(4);

        Monk winner = null;

        JniLoaderEx.loadGlfw();
        int width = 1080, height = 720;
        try (JImGui gui = new JImGui(width, height, "Lab22")) {

            imGui = gui;

            String fontName = "consola.ttf";
            File fontFile = new File(fontName);
            if (!fontFile.exists()) {
                String fontPath = "fonts/" + fontName;
                InputStream fontInput = Main.class.getClassLoader().getResourceAsStream(fontPath);
                assert fontInput != null;
                Files.copy(fontInput, fontFile.getAbsoluteFile().toPath());
            }

            NativeShort glyphRange = imGui.getIO().getFonts().getGlyphRangesForCyrillic();

            JImFont font = imGui.getIO().getFonts().addFontFromFile(fontName, 16, glyphRange);

            imGui.initBeforeMainLoop();
            while (!imGui.windowShouldClose()) {

                imGui.initNewFrame();

                tree.display(imGui);

                if (!started) {
                    imGui.begin("Start", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                    if (imGui.button("Начать Турнир!")) {
                        started = true;
                    }
                    JImGuiGen.end();
                } else if (!done) {
                    winner = new ForkJoinPool().invoke(new Match(tree));
                    done = true;
                } else if (winner != null) {
                    imGui.begin("Start", new NativeBool(), JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                    imGui.text("Победитель: ");
                    imGui.sameLine();
                    winner.display(imGui);
                    JImGuiGen.end();
                }

                imGui.render();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        new Main();
    }
}
