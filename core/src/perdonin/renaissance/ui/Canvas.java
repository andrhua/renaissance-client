package perdonin.renaissance.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import perdonin.renaissance.core.Const;
import perdonin.renaissance.i.IReset;


public class Canvas extends Widget implements IReset {
    private FrameBuffer buffer;
    private TextureRegion textureRegion;
    private ShapeRenderer shapeRenderer;
    private Array<Vector2> points = new Array<>();
    private boolean isStrokeFinished;
    private Vector2 topRight, bottomLeft;
    private Pixmap raw;

    public void initGL(){
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setColor(Color.BLACK);
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, Const.WIDTH, Const.HEIGHT, false);
        this.reset();
        textureRegion = new TextureRegion(buffer.getColorBufferTexture(), 0, 0, Const.CANVAS_SIZE, Const.CANVAS_SIZE);
        textureRegion.flip(false, true);
    }

    public void addInk(Vector2 v){
        points.add(v);
        if (v.x > topRight.x) topRight.x = v.x;
        if (v.y > topRight.y) topRight.y = v.y;
        if (v.x < bottomLeft.x) bottomLeft.x = v.x;
        if (v.y < bottomLeft.y) bottomLeft.y = v.y;
    }

    public TextureRegionDrawable getRawDrawable(){
        TextureRegion tr = new TextureRegion(new Texture(raw));
        tr.flip(false, true);
        return new TextureRegionDrawable(tr);
    }

    private void updateRawPixmap(){
        if (raw != null) raw.dispose();
        Gdx.gl30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.getFramebufferHandle());
        Gdx.gl30.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        raw = ScreenUtils.getFrameBufferPixmap(0, 0, Const.CANVAS_SIZE, Const.CANVAS_SIZE);
        Gdx.gl30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
        Gdx.gl30.glReadBuffer(GL30.GL_BACK);
    }

    public float[] getScaledDrawing(){
        float ax = Math.max(bottomLeft.x - Const.DOT_RADIUS, 0),
                ay = Math.max(bottomLeft.y - Const.DOT_RADIUS, 0),
                bx = Math.min(topRight.x + Const.DOT_RADIUS, Const.CANVAS_SIZE),
                by = Math.min(topRight.y + Const.DOT_RADIUS, Const.CANVAS_SIZE);
        updateRawPixmap();
        Pixmap scaled = new Pixmap(28, 28, Pixmap.Format.RGBA8888);
        scaled.setFilter(Pixmap.Filter.BiLinear);
        scaled.drawPixmap(raw,
                (int)ax, (int)ay, (int)(bx - ax), (int)(by - ay),
                0, 0, scaled.getWidth(), scaled.getHeight());

        float[] pixels = new float[scaled.getHeight() * scaled.getWidth()];
        for (int i = 0; i < 28; i++){
            for (int j = 0; j < 28; j++){
                int red = (scaled.getPixel(j, 27 - i) & 0xff000000) >>> 24;
                pixels[i * 28 + j] = (255 - red) / 255f;
            }
        }
        scaled.dispose();
        return pixels;
    }

    private void drawNewPoints(){
        buffer.begin();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Vector2 a, b;
        a = points.get(0);
        shapeRenderer.circle(a.x, a.y, Const.DOT_RADIUS);
        for (int i = 0; i < points.size - 1; i++){
            a = points.get(i);
            b = points.get(i + 1);
            shapeRenderer.rectLine(a, b, Const.DOT_RADIUS * 2);
            shapeRenderer.circle(b.x, b.y, Const.DOT_RADIUS);
        }
        shapeRenderer.end();
        buffer.end();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(textureRegion, getX(), getY());
    }

    @Override
    public void act(float delta) {
        if (points.size > 0) drawNewPoints();
        if (isStrokeFinished) points.clear();
    }

    @Override
    public void reset() {
        topRight = new Vector2(0, 0);
        bottomLeft = new Vector2(Const.CANVAS_SIZE, Const.CANVAS_SIZE);
        buffer.begin();
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        buffer.end();
        updateRawPixmap();
    }

    @Override
    public float getMinWidth() {
        return Const.CANVAS_SIZE;
    }

    @Override
    public float getMinHeight() {
        return Const.CANVAS_SIZE;
    }

    public void setStrokeFinished(boolean strokeFinished) {
        isStrokeFinished = strokeFinished;
    }
}
