package perdonin.renaissance.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import perdonin.renaissance.core.Assets;
import perdonin.renaissance.core.Const;
import perdonin.renaissance.ui.Colors;

public class SplashScreen extends BaseScreen {
    private BitmapFont classy;
    private AssetManager am;

    SplashScreen(ScreenManager sm) {
        super(sm);
    }

    private void initAssets(){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/classy.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.minFilter= Texture.TextureFilter.Linear;
        parameter.magFilter= Texture.TextureFilter.Linear;
        parameter.characters = "rensaic";
        parameter.size = Const.heightInt(0.067f);
        classy = generator.generateFont(parameter);
        classy.setUseIntegerPositions(false);
    }

    @Override
    protected void initUI() {
        initAssets();
        Label.LabelStyle style = new Label.LabelStyle(classy, Colors.LOGO);
        Table table = new Table();
        table.defaults().align(Align.center);
        table.setFillParent(true);
        table.add(new Label("renaissance", style)).center();

        sm.stage.addActor(table);
        sm.stage.addAction(Actions.sequence(
                Actions.alpha(0),
                Actions.fadeIn(.5f)
        ));

        am = new Assets().manager;
        sm.game.assets = am;
    }

    @Override
    protected void onShow(Stage stage) {

    }

    @Override
    public void update(float delta) {
        if (am.update()){
            sm.postLoad();
            sm.setScreen(ScreenManager.ScreenType.MENU);
        }
    }
}
