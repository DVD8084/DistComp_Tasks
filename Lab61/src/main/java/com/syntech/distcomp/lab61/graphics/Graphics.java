package com.syntech.distcomp.lab61.graphics;

import com.syntech.distcomp.lab61.Main;
import com.syntech.distcomp.lab61.logic.Piece;
import org.apache.commons.io.IOUtils;
import org.ice1000.jimgui.JImGui;
import org.ice1000.jimgui.JImGuiGen;
import org.ice1000.jimgui.JImStyleColors;
import org.ice1000.jimgui.JImTextureID;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Graphics {
    private static ArrayList<JImTextureID> textures;
    private static ArrayList<String> names;

    public static void initialize() throws IOException {
        textures = new ArrayList<>();
        names = new ArrayList<>();

        //This one should always be first. Just in case.
        loadTexture("missingno", "ui");

        loadTexture("blank", "ui");
        loadUITextures();
        loadCellTextures();
    }

    private static void loadUITextures() throws IOException {
        loadTexture("cross", "ui");
        loadTexture("double_left", "ui");
        loadTexture("double_right", "ui");
        loadTexture("down", "ui");
        loadTexture("info", "ui");
        loadTexture("left", "ui");
        loadTexture("load", "ui");
        loadTexture("log_closed", "ui");
        loadTexture("log_opened", "ui");
        loadTexture("qmark", "ui");
        loadTexture("restart", "ui");
        loadTexture("right", "ui");
        loadTexture("save", "ui");
        loadTexture("up", "ui");
    }

    private static void loadCellTextures() throws IOException {
        for (int i = 0; i < 6; i++) {
            loadTexture(String.format("%d", i), "cells");
        }
    }

    private static void loadTexture(String name, String folder) throws IOException {
        String texturePath = String.format("textures/%s/%s.png", folder, name);
        InputStream textureInput = Main.class.getClassLoader().getResourceAsStream(texturePath);
        if (textureInput != null) {
            textures.add(JImTextureID.fromBytes(IOUtils.toByteArray(textureInput)));
            names.add(name);
        }
    }

    @NotNull
    public static JImTextureID getTexture(String name) {
        int index = names.indexOf(name);
        if (index >= 0) {
            return textures.get(index);
        }
        return textures.get(0);
    }

    private static boolean display(@NotNull JImGui imGui, JImTextureID image, float size, @NotNull com.syntech.distcomp.lab61.graphics.Color color, int id) {
        imGui.pushStyleColor(JImStyleColors.Button, color.getColor());
        imGui.pushStyleColor(JImStyleColors.ButtonHovered, color.getHoveredColor());
        imGui.pushStyleColor(JImStyleColors.ButtonActive, color.getActiveColor());
        JImGui.pushID(id);
        boolean result = imGui.imageButton(image, size, size);
        JImGuiGen.popID();
        JImGuiGen.popStyleColor(3);
        return result;
    }

    public static boolean display(@NotNull JImGui imGui, String name, float size, @NotNull com.syntech.distcomp.lab61.graphics.Color color, int id) {
        return display(imGui, getTexture(name), size, color, id);
    }

    public static boolean display(@NotNull JImGui imGui, @NotNull Piece piece, float size, Color color, int id) {
        return display(imGui, piece.getTexture(), size, color, id);
    }

    public static boolean display(@NotNull JImGui imGui, float size, Color color, int id) {
        return display(imGui, getTexture("blank"), size, color, id);
    }

}
