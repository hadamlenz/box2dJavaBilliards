package com.hadamlenz.billiards;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

/** A ball on the table. Also the base class for the cue ball. */
public class Ball {

    protected static final float RADIUS_PX = 10;

    protected Body body;
    //solid balls and the cue
    protected final Color fill;
    //striped balls
    protected final boolean striped;
    protected boolean visible = true;

    public Ball(World world, float x, float y, Color fill) {
        this(world, x, y, fill, false);
    }

    public Ball(World world, float x, float y, Color fill, boolean striped) {
        this.fill = fill;
        this.striped = striped;
        makeBody(world, Units.toWorld(x, y));
    }


    /** Draws the ball, or parks it off the table once sunk. */
    public void display(ShapeRenderer shapes) {
        if (!visible) {
            return;
        }
        Vector2 center = body.getWorldCenter();
        float radius = Units.toMeters(RADIUS_PX);

        shapes.setColor(striped ? Color.WHITE : fill);
        shapes.circle(center.x, center.y, radius, 32);

        if (striped) {
            float halfStripeHeight = radius * 0.4f;
            float halfStripeWidth = (float) Math.sqrt(
                    radius * radius - halfStripeHeight * halfStripeHeight);

            shapes.setColor(fill);
            shapes.rect(
                    center.x - halfStripeWidth,
                    center.y - halfStripeHeight,
                    halfStripeWidth * 2,
                    halfStripeHeight * 2);
        }
    }


    /** Removes the ball's body from the world. */
    public void killBody() {
        body.getWorld().destroyBody(body);
    }

    /** Creates the ball's  body and circle shape. */
    protected void makeBody(World world, Vector2 center) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Slows the ball over time, standing in for the table's friction.
        bodyDef.linearDamping = 1.5f;
        bodyDef.position.set(center);
        body = world.createBody(bodyDef);
        body.setUserData(this);

        // The circle sits one radius below the body's position.
        CircleShape circle = new CircleShape();
        circle.setRadius(Units.toMeters(RADIUS_PX));
        circle.setPosition(new Vector2(0, -Units.toMeters(RADIUS_PX)));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 1f;
        body.createFixture(fixtureDef);
        circle.dispose();
    }

    /** Placeholder for per-frame movement logic. */
    public void move() {
    }
}