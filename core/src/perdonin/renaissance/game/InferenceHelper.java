package perdonin.renaissance.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.discovery.Discovery;
import com.google.api.services.discovery.model.JsonSchema;
import com.google.api.services.discovery.model.RestDescription;
import com.google.api.services.discovery.model.RestMethod;
import com.google.common.collect.Lists;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import perdonin.renaissance.i.IReset;
import perdonin.renaissance.i.IUpdate;

public class InferenceHelper implements IUpdate, IReset {
    private Map<String, Object> inputWrap = new HashMap<>(), input = new HashMap<>();
    private final HttpTransport httpTransport = new NetHttpTransport();
    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private RestMethod method;
    private GenericUrl url;
    private final GoogleCredential credential = GoogleCredential.fromStream(Gdx.files.internal("inferrence/keras_keys.json").read())
            .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
    private final HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
    private Array<Future<HttpResponse>> futures = new Array<>();
    private Array<Integer> pasts = new Array<>();
    private Json json = new Json();
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public InferenceHelper() throws Exception {
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

    void makeInferenceImage(float[] pixels){
        input.put("in",  Collections.singletonList(pixels));
        makeRequest();
    }

    public void makeInferenceInk(Array<float[]>ink) {
        input.put("ink", ink);
        input.put("shape", new int[]{ink.size, 3});
        makeRequest();
    }

    private void makeRequest(){
        inputWrap.put("instances", Collections.singletonList(input));
        HttpContent content = new JsonHttpContent(jsonFactory, inputWrap);
        try {
            HttpRequest request = requestFactory.buildRequest(method.getHttpMethod(), url, content);
            futures.add(request.executeAsync());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(float delta) {
        for (int i = 0; i < futures.size; i++) {
            Future<HttpResponse> f = futures.get(i);
            if (f.isDone()){
                try {
                    String response = f.get().parseAsString();
                    float[] scores = json
                            .fromJson(InferenceHelper.Wrap.class, response)
                            .predictions[0]
                            .out;
                    support.firePropertyChange("scores", null, scores);
                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                pasts.add(i);
            }
        }
        for (Integer p: pasts){
            futures.removeIndex(p);
        }
        pasts.clear();
    }

    @Override
    public void reset() {
        futures.clear();
        pasts.clear();
    }

    public void addPropertyChangeListener(String prop, PropertyChangeListener pcl) {
        support.addPropertyChangeListener(prop, pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    public static class Wrap{
        Prediction[] predictions;
    }

    public static class Prediction{
        float[] out;
    }
}
