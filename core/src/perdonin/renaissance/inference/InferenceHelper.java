package perdonin.renaissance.inference;

import com.badlogic.gdx.utils.Json;
import com.google.api.client.http.HttpResponse;
import perdonin.renaissance.game.GameSession;
import perdonin.renaissance.i.IReset;
import perdonin.renaissance.i.IUpdate;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Handles all routine related to making inference requests to GCP instance.
 */
public class InferenceHelper implements IUpdate, IReset {
    private final GameSession gameSession;
    private final GoogleCloudPredictionBackend gcpb;
    private final HashSet<Future<HttpResponse>> responses = new HashSet<>();
    private final Json json = new Json();

    public InferenceHelper(GameSession gameSession, GoogleCloudPredictionBackend gcpb) {
        this.gameSession = gameSession;
        this.gcpb = gcpb;
    }

    /**
     * Creates request to classification model from user's current drawing.
     * @param pixels array of length 784, containing float red channel values,
     *               from left -> right, top -> bottom
     */
    public void requestPrediction(float[] pixels){
        responses.add(gcpb.requestOnlinePrediction(pixels));
    }

    /**
     * Waits for responses to be returned from GCP, processes it, triggers update
     * in GameScreen, and removes response from list of unfinished requests.
     * @param delta is not used here.
     */
    @Override
    public void update(float delta) {
        boolean flag = false;
        for (Iterator<Future<HttpResponse>> i = responses.iterator(); i.hasNext(); ) {
            Future<HttpResponse> response = i.next();
            if (response.isDone()){
                try {
                    PredictionResponse predictionResponse = new PredictionResponse(
                            json.fromJson(PredictionResponse.Wrap.class, response.get().parseAsString())
                                    .predictions[0].out);
                    flag = gameSession.isSuccessfulRecognition(predictionResponse);
                    if (flag) break;
                } catch (IOException | InterruptedException | ExecutionException e) {
                    System.out.println("Server be sleepin");
                    e.printStackTrace();
                }
                i.remove(); // safe deletion to prevent ConcurrentModificationException
            }
        }
        if (flag) reset();
    }

    /**
     * Forget all unsatisfied requests, if new game session started.
     */
    @Override
    public void reset() {
        responses.clear();
    }
}
