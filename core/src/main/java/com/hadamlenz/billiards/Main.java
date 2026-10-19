package com.hadamlenz.billiards;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * The game itself. Port of BilliardsPBox2d.pde.
 *
 * Processing calls the sketch's setup() once and draw() every frame. libGDX
 * does the same with create() and render(). The sketch also received
 * collision callbacks directly, so this class implements ContactListener to
 * play that role.
 */
public class Main extends ApplicationAdapter implements ContactListener {

    //size and default scale.
    private static final float RAIL_WIDTH = 20;
    private static final float FOOTLINE = 150;
    private static final float BALL_R = 17;
    private static final float X_OFFSET = 3;
    private static final float Y_OFFSET = 5;
    private final List<Boundary> boundaries = new ArrayList<>();
    private final List<Pocket> pockets = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>();
    private Cue cue;

    private World world;
    private OrthographicCamera camera;
    private Box2DDebugRenderer debugRenderer;

    /** runs once when the window opens. */
    @Override
    public void create() {
        Box2D.init();
        camera = new OrthographicCamera();
        // camera.setToOrtho(false, TABLE_WIDTH, TABLE_HEIGHT);
        camera.setToOrtho(false,
                Units.toMeters(Units.TABLE_WIDTH_PX),
                Units.toMeters(Units.TABLE_HEIGHT_PX));
        debugRenderer = new Box2DDebugRenderer();
        resetTable();
    }


    /**
     * Builds the world, rails, pockets, rack, and cue ball. 
     * reset the table (R key, or a scratch), 
     */
    private void resetTable() {
        if (world != null) {
            world.dispose();
        }
        world = new World(new Vector2(0, 0), true);
        float w = Units.TABLE_WIDTH_PX;
        float h = Units.TABLE_HEIGHT_PX;
        boundaries.clear();
        boundaries.add(new Boundary(world, 0, 0, w * 2, RAIL_WIDTH, 0));
        boundaries.add(new Boundary(world, 0, 0, RAIL_WIDTH, h * 2, 0));
        boundaries.add(new Boundary(world, w - 2, 0, RAIL_WIDTH, h * 2, 0));
        boundaries.add(new Boundary(world, w - 2, h - 2, w * 2, RAIL_WIDTH, 0));


        pockets.clear();
        pockets.add(new Pocket(world, 15, 0));
        pockets.add(new Pocket(world, w - 15, 0));
        pockets.add(new Pocket(world, 15, h / 2 - 15));
        pockets.add(new Pocket(world, w - 15, h / 2 - 15));
        pockets.add(new Pocket(world, 15, h - 30));
        pockets.add(new Pocket(world, w - 15, h - 30));

        balls.clear();
        balls.add(new Ball(world, w / 2, FOOTLINE, Color.BLACK));
        balls.add(new Ball(world, w / 2 - BALL_R + X_OFFSET, FOOTLINE - BALL_R - Y_OFFSET, Color.BLACK));
        balls.add(new Ball(world, w / 2 + BALL_R - X_OFFSET, FOOTLINE - BALL_R - Y_OFFSET, Color.BLACK));

        cue = new Cue(world, w / 2, 450);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                onMouseReleased(screenX, screenY);
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                onKeyReleased(keycode);
                return true;
            }
        });

    }


    /** Port of draw(): runs every frame. */
    @Override
    public void render() {
        ScreenUtils.clear(0f, 0.4f, 0f, 1f);
        world.step(1 / 60f, 10, 8);
        camera.update();
        debugRenderer.render(world, camera.combined);
    }


    /** Frees native resources (the Box2D world, renderers). Processing did this for us. */
    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
    }


    /** Port of beginContact(): two bodies started touching. */
    @Override
    public void beginContact(Contact contact) {
    }

    /** Port of endContact(): empty in the original too. */
    @Override
    public void endContact(Contact contact) {
    }

    /** Required by ContactListener; the original never used it. */
    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    /** Required by ContactListener; the original never used it. */
    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    /** Shoots the cue ball away from where the mouse was released. */
    private void onMouseReleased(int screenX, int screenY) {
        Vector3 touch = camera.unproject(new Vector3(screenX, screenY, 0));
        cue.shoot(new Vector2(touch.x, touch.y));
    }

    /** R resets the table. */
    private void onKeyReleased(int keycode) {
        if (keycode == Input.Keys.R) {
            resetTable();
        }
    }
}
