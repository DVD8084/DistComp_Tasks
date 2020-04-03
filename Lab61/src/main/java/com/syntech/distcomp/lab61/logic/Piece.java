package com.syntech.distcomp.lab61.logic;

import org.ice1000.jimgui.JImTextureID;

public abstract class Piece {
    int row, col;

    public abstract JImTextureID getTexture();

    public void setCoords(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
