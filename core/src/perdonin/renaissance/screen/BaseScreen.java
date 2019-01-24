package perdonin.renaissance.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;

import perdonin.renaissance.core.I18n;
import perdonin.renaissance.ui.UIBuilder;
import perdonin.renaissance.i.IUpdate;

public abstract class BaseScreen implements IUpdate {
    protected AssetManager assets;
    protected ScreenManager sm;
    protected UIBuilder uiBuilder;
    protected I18n i18n;

    BaseScreen(ScreenManager sm){
        this.sm = sm;
        this.assets = sm.game.assets;
        this.uiBuilder = sm.uiBuilder;
        this.i18n = sm.i18n;
        this.initUI();
    }

    protected abstract void initUI();

    protected void onShow(Stage stage){
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void update(float delta) {

    }
}
