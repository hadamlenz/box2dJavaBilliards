package com.hadamlenz.billiards;

import com.badlogic.gdx.math.Vector2;

/** Converts the original sketch's pixel coordinates into Box2D meters. */
public final class Units {

    public static final float PIXELS_PER_METER = 10f;
    public static final float TABLE_WIDTH_PX = 350f;
    public static final float TABLE_HEIGHT_PX = 600f;

    private Units() {
    }

    public static float toMeters(float pixels) {
        return pixels / PIXELS_PER_METER;
    }

    /** Processing's y grows downward and libGDX's grows upward, so y is flipped. */
    public static Vector2 toWorld(float px, float py) {
        return new Vector2(toMeters(px), toMeters(TABLE_HEIGHT_PX - py));
    }
}
