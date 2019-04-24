package perdonin.renaissance.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;
import perdonin.renaissance.core.Const;
import perdonin.renaissance.i.IReset;
import perdonin.renaissance.inference.InferenceHelper;
import perdonin.renaissance.ui.Canvas;

public class DrawingListener extends InputListener implements IReset {

    private Array<float[]> strokes = new Array<>();
    private Vector3 prevPoint;
    private Canvas canvas;
    private InferenceHelper helper;

    public DrawingListener(Canvas canvas, InferenceHelper helper){
        this.canvas = canvas;
        this.helper = helper;
    }

    private void ink(float x, float y, boolean endOfStroke){
        canvas.addStroke(new Vector2(x, y));
        if (prevPoint != null && (endOfStroke || (x != prevPoint.x && y != prevPoint.y ))) {
            strokes.add(new float[]{
                    (x - prevPoint.x) * Const.ACTUAL2SAMPLE,
                    (y - prevPoint.y) * Const.ACTUAL2SAMPLE,
                    endOfStroke ? 1 : 0
            });
        }
        prevPoint = new Vector3(x, y, 0);
        canvas.setStrokeFinished(endOfStroke);
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        ink(x, y, false);
        return true;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        ink(x, y, false);
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        ink(x, y, true);
        helper.requestPrediction(canvas.getScaledDrawing());
    }

    @Override
    public void reset() {
        strokes.clear();
        prevPoint = null;
    }
}
