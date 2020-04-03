package com.syntech.distcomp.lab62.logic;

import com.syntech.distcomp.lab62.graphics.Graphics;
import org.ice1000.jimgui.JImTextureID;

public class Cell extends Piece {

    int color;

    public Cell(int color) {
        this.color = color;
    }

    @Override
    public JImTextureID getTexture() {
        return Graphics.getTexture(String.format("%d", color));
    }

    public int getColor() {
        return color;
    }
}
