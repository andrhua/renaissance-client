package perdonin.renaissance.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import perdonin.renaissance.Const;
import perdonin.renaissance.game.GameSession;
import perdonin.renaissance.MyGame;
import perdonin.renaissance.ui.Colors;
import perdonin.renaissance.ui.UIBuilder;
import perdonin.renaissance.i.IUpdate;

import static perdonin.renaissance.ui.Colors.MAIN_BG;

public class ScreenManager implements IUpdate {

    public enum ScreenType{
        SPLASH, MENU, GAME
    }
    private BaseScreen currScreen, menu, _game;
    public MyGame game;
    public Viewport viewport;
    public Stage stage;
    public UIBuilder uiBuilder;

    public ScreenManager(MyGame game){
        this.game = game;
        viewport = new ScreenViewport(new OrthographicCamera(Const.WIDTH, Const.HEIGHT));
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
    }

    public void setScreen(final ScreenType st){
        if (st != ScreenType.SPLASH) {
            stage.addAction(Actions.sequence(
                    Actions.fadeOut(.65f, Interpolation.exp10Out),
                    Actions.run(() -> {
                        stage.clear();
                        switch (st) {
                            case MENU: currScreen = menu; break;
                            case GAME: currScreen = _game; break;
                        }
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

    void postLoad(){
        uiBuilder = new UIBuilder(game.assets);
        menu = new MenuScreen(this);
        _game = new GameScreen(this, new GameSession());
    }

    public void update(float delta){
        Gdx.gl.glClearColor(MAIN_BG.r, MAIN_BG.g, MAIN_BG.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        currScreen.update(delta);
        stage.draw();
    }
}
