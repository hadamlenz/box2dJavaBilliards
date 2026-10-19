package com.hadamlenz.billiards;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.graphics.Color;

/**
 * A fixed circle at a pocket location. The sinking logic lives in
 * Main.beginContact(), which fires when a ball touches a pocket. Port of
 * Pocket.pde.
 */
public class Pocket {

    private static final float RADIUS_PX = 15;

    private Body body;

    public Pocket(World world, float x, float y) {
        makeBody(world, Units.toWorld(x, y));
    }

    public void display(ShapeRenderer shapes) {
        Vector2 position = body.getPosition();
        shapes.setColor(Color.BLACK);
        shapes.circle(
                position.x,
                position.y - Units.toMeters(RADIUS_PX),
                Units.toMeters(RADIUS_PX),
                32);
    }


    /** Creates the pocket's static physics body and circle shape. */
    protected void makeBody(World world, Vector2 center) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(center);
        body = world.createBody(bodyDef);
        body.setUserData(this);

        // The original drew and placed the circle one radius below the
        // body's position. Screen-down is negative y in our world.
        CircleShape circle = new CircleShape();
        circle.setRadius(Units.toMeters(RADIUS_PX));
        circle.setPosition(new Vector2(0, -Units.toMeters(RADIUS_PX)));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        body.createFixture(fixtureDef);
        circle.dispose();
    }
}