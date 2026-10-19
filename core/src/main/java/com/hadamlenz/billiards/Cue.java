package com.hadamlenz.billiards;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.math.Vector2;

/**
 * The cue ball: a white Ball that also draws an aiming line while the mouse
 * is held, and turns the drag into a shot on release. Port of Cue.pde.
 */
public class Cue extends Ball {

    public Cue(World world, float x, float y) {
        super(world, x, y, Color.WHITE);
    }

    /** Draws the ball, plus the aiming line while the mouse is held. */
    public void update(ShapeRenderer shapes) {
    }

    /**
     * Shoots the ball away from the target point. The farther the target is
     * from the ball, the harder the shot.
     */
    public void shoot(Vector2 target) {
        Vector2 center = body.getWorldCenter();
        Vector2 impulse = center.cpy().sub(target).scl(Units.PIXELS_PER_METER);
        body.applyLinearImpulse(impulse, center, true);
    }

}
