package perdonin.renaissance.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
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
    private Future<HttpResponse> wakingRequest;
    private boolean connectionEstablished = false;
    private boolean assetsLoaded = false;
    // TODO: make fake bitmap font, create skin, substitute font with runtime generated, implement dialogs
    private Dialog noConnectionDialog;
    private Dialog noServerResponse;
    private Label loadingFeedback;

    SplashScreen(ScreenManager sm) {
        super(sm);
        try {
            gcpb = new GoogleCloudPredictionBackend();
            wakingRequest = gcpb.requestOnlinePrediction(new float[0]);
            updateLoadingFeedback("connecting to server");
        } catch (IOException e) {
            updateLoadingFeedback("No internet connection.");
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

    private BitmapFont getDialogFont(FreeTypeFontGenerator.FreeTypeFontParameter parameter) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/firacode.otf"));
        parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.";
        parameter.size = Const.heightInt(0.025f);
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

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

        loadingFeedback = new Label("status", new Label.LabelStyle(getDialogFont(parameter), new Color(1, 1, 1, .5f)));
        // workaround for scaling animation
        Container<Label> appName = new Container<>(
                new Label("rennaissance", new Label.LabelStyle(getAppNameFont(parameter), Colors.LOGO))
        );
        appName.setTransform(true);
        appName.setOrigin(appName.getPrefWidth() / 2, appName.getPrefHeight() / 2);
        Table table = new Table();
        table.defaults().align(Align.center);
        table.setFillParent(true);
        table.add(appName).center().expandY().row();
        table.add(loadingFeedback).center();
        appName.addAction(Actions.sequence(
                Actions.alpha(0),
                Actions.fadeIn(.5f),
                Actions.forever(
                        Actions.sequence(
                                Actions.delay(1.7f),
                                Actions.scaleTo(0.95f,0.95f, 0.20f, Interpolation.circleIn),
                                Actions.scaleTo(1, 1, 0.10f, Interpolation.circleOut)
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
        if (!connectionEstablished && wakingRequest != null && wakingRequest.isDone()) {
            try {
                if (wakingRequest.get().isSuccessStatusCode()) {
                    connectionEstablished = true;
                    updateLoadingFeedback("");
                    Gdx.app.log("instance", "is online");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                if (e.getMessage().endsWith("handshake timed out")) {
                    // no connection
                    updateLoadingFeedback("Could not connect to server. Check your internet connection.");
                    Gdx.app.log("no", "connection");
                } else {
                    // tf serving instance is offline
                    updateLoadingFeedback("Prediction server does not respond. Try again later.");
                    Gdx.app.log("instance", "is offline");
                }
                Gdx.app.exit();
            }
        }
        if (!assetsLoaded && am.update()) {
            sm.execAssetsDependentOps(gcpb);
            assetsLoaded = true;
        }
    }

    private void updateLoadingFeedback(String feedback) {
        loadingFeedback.addAction(
                Actions.sequence(
                        Actions.fadeOut(0.3f),
                        Actions.run(() -> loadingFeedback.setText(feedback)),
                        Actions.fadeIn(0.25f)
                        )
        );
    }
}
