package com.hadamlenz.billiards;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;


public class Boundary {

    private final Body body;
    private final float width;
    private final float height;

    public Boundary(World world, float x, float y, float w, float h, float angle) {
        width = Units.toMeters(w);
        height = Units.toMeters(h);
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(Units.toWorld(x, y));
        bodyDef.angle = angle;
        body = world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape box = new PolygonShape();
        box.setAsBox(width / 2, height / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 1f;
        body.createFixture(fixtureDef);
        box.dispose();
    }

    public void display(ShapeRenderer shapes) {
    }
}
