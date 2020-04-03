package com.syntech.distcomp.lab61;

import com.syntech.distcomp.lab61.graphics.Color;
import com.syntech.distcomp.lab61.graphics.Graphics;
import com.syntech.distcomp.lab61.logic.Board;
import com.syntech.distcomp.lab61.logic.LifeThread;
import org.ice1000.jimgui.*;
import org.ice1000.jimgui.flag.JImWindowFlags;
import org.ice1000.jimgui.util.JniLoaderEx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;


public class Main {

    public static void main(String... args) {
        JniLoaderEx.loadGlfw();
        int width = 1280, height = 800;
        try (JImGui imGui = new JImGui(width, height, "Lab61")) {

            Graphics.initialize();

            String fontName = "LCALLIG.TTF";
            File fontFile = new File(fontName);
            if (!fontFile.exists()) {
                String fontPath = "fonts/" + fontName;
                InputStream fontInput = Main.class.getClassLoader().getResourceAsStream(fontPath);
                assert fontInput != null;
                Files.copy(fontInput, fontFile.getAbsoluteFile().toPath());
            }

            NativeShort glyphRange = imGui.getIO().getFonts().getGlyphRangesForCyrillic();

            JImFont font = imGui.getIO().getFonts().addFontFromFile(fontName, 24, glyphRange);

            width /= 10;
            height /= 10;

            Board board = new Board(height - 12 - height % 2, width - 6 - width % 2);

            imGui.setBackground(Color.WINDOW_BG.getColor());

            imGui.initBeforeMainLoop();

            boolean started = false;

            while (!imGui.windowShouldClose()) {

                imGui.initNewFrame();

                imGui.setWindowPos("boardMain", 20 + width % 2 * 5, 20 + height % 2 * 5);
                board.display(imGui, "boardMain", 10);

                if (!started) {
                    imGui.setWindowPos("start", width * 5 - 35, height * 10 - 70);
                    imGui.begin("start", new NativeBool(), JImWindowFlags.NoMove | JImWindowFlags.NoTitleBar | JImWindowFlags.AlwaysAutoResize);
                    if (imGui.button("Start")) {
                        started = true;
                        for (int i = 0; i < 4; i++) {
                            new LifeThread(board, i).start();
                        }
                    }
                    JImGuiGen.end();
                }

                imGui.render();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
