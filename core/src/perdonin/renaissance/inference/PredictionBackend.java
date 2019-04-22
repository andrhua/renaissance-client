package perdonin.renaissance.inference;

public interface PredictionBackend<T> {

    T requestOnlinePrediction(float[] pixels);
}
