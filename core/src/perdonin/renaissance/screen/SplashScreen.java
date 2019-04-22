package perdonin.renaissance.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.google.api.client.http.HttpResponse;
import perdonin.renaissance.core.Assets;
import perdonin.renaissance.core.Const;
import perdonin.renaissance.inference.GoogleCloudPredictionBackend;
import perdonin.renaissance.ui.Colors;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SplashScreen extends BaseScreen {
    private AssetManager am;
    private GoogleCloudPredictionBackend gcpb;
    private Future<HttpResponse> instanceWakingRequest;
    private boolean connectionEstablished = false;
    private boolean assetsLoaded = false;
    // TODO: make fake bitmap font, create skin, substitute font with runtime generated, implement dialogs
    private Dialog noConnectionDialog;
    private Dialog noServerResponse;

    SplashScreen(ScreenManager sm) {
        super(sm);
        try {
            gcpb = new GoogleCloudPredictionBackend();
            instanceWakingRequest = gcpb.requestOnlinePrediction(new float[0]);
        } catch (IOException e) {
            System.out.println("Cannot init Google Cloud backend");
            e.printStackTrace();
        }
    }

    private BitmapFont getAppNameFont(FreeTypeFontGenerator.FreeTypeFontParameter parameter){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/classy.otf"));
        parameter.characters = "rensaic";
        parameter.size = Const.heightInt(0.067f);
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

    /*private BitmapFont getDialogFont(FreeTypeFontGenerator.FreeTypeFontParameter parameter) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/firacode.otf"));
        parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.";
        parameter.size = Const.heightInt(0.03f);
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }*/

    @Override
    protected void initUI() {
        /*Pixmap p = new Pixmap(2, 2, Pixmap.Format.RGB565);
        p.setColor(Colors.LOGO);
        p.fillRectangle(0, 0, 2, 2);
        NinePatchDrawable dialogBackground = new NinePatchDrawable(new NinePatch(new Texture(p)));
        p.dispose();*/

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.minFilter= Texture.TextureFilter.Linear;
        parameter.magFilter= Texture.TextureFilter.Linear;

        /*Window.WindowStyle dialogStyle = new Window.WindowStyle(getDialogFont(parameter), Color.WHITE, dialogBackground);
        noConnectionDialog = new Dialog("Check your internet connection and try again", dialogStyle);
        noConnectionDialog.button("OK");
        noServerResponse = new Dialog("Prediction server does not response", dialogStyle);
        noServerResponse.button("Reconnect", new Object());*/

        Table table = new Table();
        table.defaults().align(Align.center);
        table.setFillParent(true);
        table.add(new Label("renaissance", new Label.LabelStyle(getAppNameFont(parameter), Colors.LOGO))).center();
        table.setOrigin(Const.width(.5f), Const.height(.5f));
        table.setTransform(true);
        table.addAction(Actions.sequence(
                Actions.alpha(0),
                Actions.fadeIn(.5f),
                Actions.forever(
                        Actions.sequence(
                                Actions.delay(1.5f),
                                Actions.scaleTo(0.9f,0.9f, 0.25f, Interpolation.circleIn),
                                Actions.scaleTo(1, 1, 0.15f, Interpolation.circleOut)
                        )
                )
        ));
        sm.stage.addActor(table);
        am = new Assets().manager;
        sm.game.assets = am;
    }

    @Override
    protected void onShow(Stage stage) {

    }

    @Override
    public void update(float delta) {
        if (connectionEstablished && assetsLoaded) {
            sm.setScreen(ScreenManager.ScreenType.MENU);
        }
        if (!connectionEstablished && instanceWakingRequest.isDone()) {
            try {
                if (instanceWakingRequest.get().isSuccessStatusCode()) {
                    connectionEstablished = true;
                    Gdx.app.log("instance", "is online");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                if (e.getMessage().endsWith("handshake timed out")) {
                    // no connection
                    Gdx.app.log("no", "connection");
                } else {
                    // tf serving instance is offline
                    Gdx.app.log("instance", "is offline");
                }
            }
        }
        if (!assetsLoaded && am.update()) {
            sm.execAssetsDependentOps(gcpb);
            assetsLoaded = true;
        }
    }
}
