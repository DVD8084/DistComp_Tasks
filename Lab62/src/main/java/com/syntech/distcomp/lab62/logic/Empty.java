package com.syntech.distcomp.lab62.logic;

import com.syntech.distcomp.lab62.graphics.Graphics;
import org.ice1000.jimgui.JImTextureID;

public class Empty extends Piece {
    @Override
    public JImTextureID getTexture() {
        return Graphics.getTexture("blank");
    }
}
