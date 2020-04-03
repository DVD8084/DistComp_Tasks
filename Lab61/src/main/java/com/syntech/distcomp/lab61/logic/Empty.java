package com.syntech.distcomp.lab61.logic;

import com.syntech.distcomp.lab61.graphics.Graphics;
import org.ice1000.jimgui.JImTextureID;

public class Empty extends Piece {
    @Override
    public JImTextureID getTexture() {
        return Graphics.getTexture("blank");
    }
}
