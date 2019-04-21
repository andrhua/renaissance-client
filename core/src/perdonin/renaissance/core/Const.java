package perdonin.renaissance.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public class Const {
    public static final int WIDTH = Gdx.graphics.getWidth();
    public static final int HEIGHT = Gdx.graphics.getHeight();
    public static final int DOT_RADIUS = widthInt(.0155f);
    public static final int ROUND_TIME = 30;
    public static final int ROUNDS = 6;
    public static final int CANVAS_SIZE = widthInt(1);
    public static final int POSITION_TO_WIN = 5;
    public static final float ACTUAL2SAMPLE = 255f / CANVAS_SIZE;
    public static final float RECOGNIZABLE = .15f;
    public static final Array<String> categories =
            new Array<>(Gdx.files.internal("inference/categories").readString().split("\n"));

    public static float width(float percent){
        return WIDTH * percent;
    }

    public static int widthInt(float percent){
        return (int)width(percent);
    }

    public static float height(float percent){
        return HEIGHT * percent;
    }

    public static int heightInt(float percent){
        return (int)height(percent);
    }
}
