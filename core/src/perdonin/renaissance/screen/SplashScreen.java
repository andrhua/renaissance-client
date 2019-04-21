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
import com.google.api.client.http.HttpResponse;
import perdonin.renaissance.core.Assets;
import perdonin.renaissance.core.Const;
import perdonin.renaissance.inferrence.GoogleCloudPredictionBackend;
import perdonin.renaissance.ui.Colors;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SplashScreen extends BaseScreen {
    private BitmapFont classy;
    private AssetManager am;
    private GoogleCloudPredictionBackend gcpb;
    private Future<HttpResponse> instanceWakingRequest;
    private boolean connectionEstabilished = false;
    private boolean assetsLoaded = false;

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
        if (connectionEstabilished && assetsLoaded) {
            sm.setScreen(ScreenManager.ScreenType.MENU);
        }
        if (!connectionEstabilished && instanceWakingRequest.isDone()) {
            try {
                if (instanceWakingRequest.get().isSuccessStatusCode()) {
                    connectionEstabilished = true;
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

    private Future<HttpResponse> createInstanceWakingRequest() {
        return gcpb.requestOnlinePrediction(new float[1]);
    }
}
