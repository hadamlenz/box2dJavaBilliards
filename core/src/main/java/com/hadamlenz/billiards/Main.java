package com.hadamlenz.billiards;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
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
    private static final float BALL_DIAMETER = Ball.RADIUS_PX * 2;
    private static final float RACK_ROW_SPACING = BALL_DIAMETER * 0.8660254f;

    //ball colors
    private static final Color[] BALL_COLORS = {
            Color.valueOf("FDD835FF"),
            Color.valueOf("1565C0FF"),
            Color.valueOf("C62828FF"),
            Color.valueOf("6A1B9AFF"),
            Color.valueOf("EF6C00FF"),
            Color.valueOf("2E7D32FF"),
            Color.valueOf("7F0000FF"),
            Color.BLACK
    };

    //gotta have a proper rack
    //it's inverted
    private static final int[] RACK_ORDER = {
            1,
            9, 2,
            13, 8, 10,
            14, 4, 11, 7,
            3, 15, 5, 12, 6
    };

    private final List<Boundary> boundaries = new ArrayList<>();
    private final List<Pocket> pockets = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>();
    private final List<Ball> sunkBalls = new ArrayList<>();
    private boolean resetRequested;
    private Cue cue;

    private World world;
    private OrthographicCamera camera;
    private ShapeRenderer shapes;

    /** runs once when the window opens. */
    @Override
    public void create() {
        Box2D.init();
        camera = new OrthographicCamera();
        // camera.setToOrtho(false, TABLE_WIDTH, TABLE_HEIGHT);
        camera.setToOrtho(false,
                Units.toMeters(Units.TABLE_WIDTH_PX),
                Units.toMeters(Units.TABLE_HEIGHT_PX));
        shapes = new ShapeRenderer();
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
        //apple pie
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(this);
        sunkBalls.clear();
        resetRequested = false;
        float w = Units.TABLE_WIDTH_PX;
        float h = Units.TABLE_HEIGHT_PX;
        //the bounds arond the outside of the table
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
        int rackIndex = 0;
        //build the rack from the RACK_ORDER and ball color
        for (int row = 0; row < 5; row++) {
            float y = FOOTLINE - row * RACK_ROW_SPACING;
            for (int column = 0; column <= row; column++) {
                int number = RACK_ORDER[rackIndex++];
                float x = w / 2 + (column - row / 2f) * BALL_DIAMETER;
                Color color = BALL_COLORS[(number - 1) % BALL_COLORS.length];
                balls.add(new Ball(world, x, y, color, number > 8));
            }
        }


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
        resolveSunkBalls();
        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeType.Filled);

        for (Boundary boundary : boundaries) {
            boundary.display(shapes);
        }

        for (Pocket pocket : pockets) {
            pocket.display(shapes);
        }

        for (Ball ball : balls) {
            ball.display(shapes);
        }

        cue.display(shapes);
        shapes.end();

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            Vector3 mouse = camera.unproject(
                    new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            shapes.begin(ShapeType.Line);
            cue.update(shapes, new Vector2(mouse.x, mouse.y));
            shapes.end();
        }
    }


    /** Frees native resources (the Box2D world, renderers). Processing did this for us. */
    @Override
    public void dispose() {
        world.dispose();
        shapes.dispose();
    }


    /** Port of beginContact(): two bodies started touching. */
    @Override
    public void beginContact(Contact contact) {
        Object first = contact.getFixtureA().getBody().getUserData();
        Object second = contact.getFixtureB().getBody().getUserData();
        if (first instanceof Pocket && second instanceof Ball) {
            queueSunkBall((Ball) second);
        } else if (second instanceof Pocket && first instanceof Ball) {
            queueSunkBall((Ball) first);
        }
    }

    private void queueSunkBall(Ball ball) {
        if (ball == cue) {
            resetRequested = true;
        } else if (!sunkBalls.contains(ball)) {
            sunkBalls.add(ball);
        }
    }

    private void resolveSunkBalls() {
        if (resetRequested) {
            resetTable();
            return;
        }

        for (Ball ball : sunkBalls) {
            ball.killBody();
        }

        balls.removeAll(sunkBalls);
        sunkBalls.clear();
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
