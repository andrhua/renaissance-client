package perdonin.renaissance.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import perdonin.renaissance.MyGame;
import perdonin.renaissance.core.Const;
import perdonin.renaissance.core.I18n;
import perdonin.renaissance.i.IUpdate;
import perdonin.renaissance.inference.GoogleCloudPredictionBackend;
import perdonin.renaissance.ui.UIBuilder;

import java.util.HashMap;

import static perdonin.renaissance.ui.Colors.MAIN_BG;

public class ScreenManager implements IUpdate {
    public enum ScreenType {
        SPLASH, MENU, GAME
    }
    private GoogleCloudPredictionBackend gcpb;
    private final HashMap<ScreenType, BaseScreen> screens = new HashMap<>(3);
    public final Viewport viewport = new ScreenViewport(new OrthographicCamera(Const.WIDTH, Const.HEIGHT));
    public final Stage stage = new Stage(viewport);
    public final MyGame game;
    public BaseScreen currScreen;
    public UIBuilder uiBuilder;
    public I18n i18n;

    public ScreenManager(MyGame game) {
        this.game = game;
        Gdx.input.setInputProcessor(stage);
    }

    public void setScreen(final ScreenType st) {
        if (st != ScreenType.SPLASH) {
            stage.addAction(Actions.sequence(
                    Actions.fadeOut(.65f, Interpolation.exp10Out),
                    Actions.run(() -> {
                        stage.clear();
                        currScreen = getScreen(st);
                        currScreen.onShow(stage);
                        stage.addAction(Actions.sequence(
                                Actions.alpha(0),
                                Actions.fadeIn(.5f, Interpolation.exp10In)
                        ));
                    })
            ));
        } else {
            currScreen = new SplashScreen(this);
        }
    }

    private BaseScreen getScreen(ScreenType st) {
        BaseScreen screen = screens.get(st);
        if (screen == null) {
            switch (st) {
                case MENU:
                    screen = new MenuScreen(this);
                    break;
                case GAME:
                    screen = new GameScreen(this, gcpb);
                    break;
            }
            screens.put(st, screen);
        }
        return screen;
    }

    void execAssetsDependentOps(GoogleCloudPredictionBackend gcpb) {
        this.gcpb = gcpb;
        uiBuilder = new UIBuilder(game.assets);
        i18n = new I18n(game.assets);
    }

    public void update(float delta) {
        Gdx.gl.glClearColor(MAIN_BG.r, MAIN_BG.g, MAIN_BG.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        currScreen.update(delta);
        stage.draw();
    }
}
