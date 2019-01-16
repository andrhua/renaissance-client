package perdonin.renaissance.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import perdonin.renaissance.Const;

public class UIBuilder {
    private NinePatch border;

    public UIBuilder(AssetManager assets){
        TextureAtlas atlas = assets.get("icons.atlas", TextureAtlas.class);
        border = atlas.createPatch("border");
    }

    public TextButton getTextButton(String text, TextButton.TextButtonStyle tbs, Runnable onTouchUp, Color borderColor){
        tbs.up = new NinePatchDrawable(border).tint(borderColor);
        TextButton tb = new TextButton(text, tbs);
        tb.setTransform(true);
        tb.pad(Const.width(.02f));
        tb.addListener(new InputListener(){
            private boolean is_touched;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                is_touched = true;
                tb.setOrigin(Align.center);
                tb.addAction(Actions.scaleTo(.95f, .95f, .2f, Interpolation.exp10Out));
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                is_touched = false;
                tb.addAction(Actions.scaleTo(1, 1, .15f, Interpolation.exp10In));
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                tb.addAction(Actions.scaleTo(1, 1, .15f, Interpolation.exp10In));
                if (is_touched) onTouchUp.run();
            }
        });
        return tb;
    }

    public ImageButton getImageButton(TextureAtlas.AtlasRegion ar, Runnable onTouchUp, Color borderColor){
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new NinePatchDrawable(border).tint(borderColor);
        style.imageUp = new TextureRegionDrawable(ar);
        ImageButton ib = new ImageButton(style);
        ib.setTransform(true);
        ib.addListener(new InputListener(){
            private boolean is_touched;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                is_touched = true;
                ib.setOrigin(Align.center);
                ib.addAction(Actions.scaleTo(.95f, .95f, .2f, Interpolation.exp10Out));
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                is_touched = false;
                ib.addAction(Actions.scaleTo(1, 1, .15f, Interpolation.exp10In));
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (is_touched) onTouchUp.run();
            }
        });
        return ib;
    }

    public TextureRegionDrawable getBackground(Color color){
        Pixmap pixmap = new Pixmap(Const.WIDTH, Const.HEIGHT, Pixmap.Format.RGB888);
        pixmap.setColor(color);
        pixmap.fill();
        return new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
    }
}
