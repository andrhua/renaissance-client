package perdonin.renaissance.inference;

import com.badlogic.gdx.Gdx;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.discovery.Discovery;
import com.google.api.services.discovery.model.JsonSchema;
import com.google.api.services.discovery.model.RestDescription;
import com.google.api.services.discovery.model.RestMethod;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class GoogleCloudPredictionBackend implements PredictionBackend<Future<HttpResponse>> {
    private final Map<String, Object> inputWrap = new HashMap<>();
    private final Map<String, Object> input = new HashMap<>();
    private final HttpTransport httpTransport = new NetHttpTransport();
    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private final GoogleCredential credential = getCredential();
    private final HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
    private final RestMethod method;
    private final GenericUrl url;
    private final ExponentialBackOff backoff = new ExponentialBackOff.Builder()
            .setInitialIntervalMillis(5*1000)
            .setMaxElapsedTimeMillis(30*1000)
            .setMaxIntervalMillis(6*1000)
            .setMultiplier(1.5)
            .setRandomizationFactor(0.25)
            .build();

    public GoogleCloudPredictionBackend() throws IOException {
        Discovery discovery = new Discovery.Builder(httpTransport, jsonFactory, null)
                .setApplicationName("Renaissance")
                .build();
        RestDescription api = discovery.apis().getRest("ml", "v1").execute();
        method = api.getResources().get("projects").getMethods().get("predict");
        JsonSchema param = new JsonSchema();
        String projectId = "renaissance-226221";
        String modelId = "gamma";
        String versionId = "rat";
        param.set("name", String.format("projects/%s/models/%s/versions/%s", projectId, modelId, versionId));
        url = new GenericUrl(UriTemplate.expand(api.getBaseUrl() + method.getPath(), param, true));
    }

    /**
     * Storing credentials on client-side is the worst decision ever, but who cares :)
     */
    private GoogleCredential getCredential() {
        try {
            return GoogleCredential
                    .fromStream(Gdx.files.internal("inference/keras_keys.json").read())
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        } catch (IOException e) {
            System.out.println("Could not read service keys from disk");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Future<HttpResponse> requestOnlinePrediction(float[] pixels) {
        input.put("in",  Collections.singletonList(pixels));
        inputWrap.put("instances", Collections.singletonList(input));
        HttpContent content = new JsonHttpContent(jsonFactory, inputWrap);
        try {
            HttpRequest request = requestFactory
                    .buildRequest(method.getHttpMethod(), url, content)
                    .setConnectTimeout(10 * 1000)
                    .setReadTimeout(60 * 1000);
            return request.executeAsync();
        } catch (IOException e) {
            System.out.println("Cannot build request");
            e.printStackTrace();
        }
        return null;
    }

}
