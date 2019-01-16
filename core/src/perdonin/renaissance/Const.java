package perdonin.renaissance;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class Const {
    public static final int WIDTH = Gdx.graphics.getWidth(), HEIGHT = Gdx.graphics.getHeight(),
            DOT_RADIUS = widthInt(.0155f),
            ROUND_TIME = 60, ROUNDS = 6,
            CANVAS_SIZE = widthInt(1);
    public static final float ACTUAL2SAMPLE = 255f / CANVAS_SIZE,
            WIN_PROB = .35f,
            RANDOM_CHOICE = 1f / 345, RECOGNIZABLE = .2f;
    public static final Array<String> categories = new Array<>(Gdx.files.internal("categories").readString().split("\n"));

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
