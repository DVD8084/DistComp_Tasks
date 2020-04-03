package com.syntech.distcomp.lab61.graphics;

import org.ice1000.jimgui.JImVec4;
import org.jetbrains.annotations.NotNull;

public enum Color {
    NONE,
    WINDOW_BG,
    BORDER,
    BUTTON,
    BOARD_BG;

    @NotNull
    public JImVec4 getColor() {
        switch (this) {
            case WINDOW_BG:
                return new JImVec4(0.47f, 0.86f, 0.4f, 1.0f);
            case BORDER:
                return new JImVec4(0.27f, 0.66f, 0.2f, 1.0f);
            case BUTTON:
                return new JImVec4(0.07f, 0.27f, 0.07f, 1.0f);
            case BOARD_BG:
                return new JImVec4(0.8f, 0.8f, 0.8f, 1.0f);
            default:
                return new JImVec4(0.5f, 0.5f, 0.5f, 1.0f);
        }
    }

    @NotNull
    public JImVec4 getHoveredColor() {
        switch (this) {
            case BUTTON:
                return new JImVec4(0.17f, 0.37f, 0.17f, 1.0f);
            case BOARD_BG:
                return new JImVec4(0.9f, 0.9f, 0.9f, 1.0f);
            default:
                return new JImVec4(0.5f, 0.5f, 0.5f, 1.0f);
        }
    }

    @NotNull
    public JImVec4 getActiveColor() {
        switch (this) {
            case BUTTON:
                return new JImVec4(0.27f, 0.47f, 0.27f, 1.0f);
            case BOARD_BG:
                return new JImVec4(0.88f, 0.88f, 0.88f, 1.0f);
            default:
                return new JImVec4(0.5f, 0.5f, 0.5f, 1.0f);
        }
    }
}
