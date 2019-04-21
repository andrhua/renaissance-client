package perdonin.renaissance.inferrence;

public interface PredictionBackend<T> {

    T requestOnlinePrediction(float[] pixels);
}
