package perdonin.renaissance.inferrence;

import com.badlogic.gdx.utils.Array;
import perdonin.renaissance.core.Utils;

import java.util.Map.Entry;

public class PredictionResponse {
    private Array<Entry<Integer, Float>> sortedScores;

    public PredictionResponse(float[] scores) {
        this.sortedScores = Utils.sort(scores);
    }

    public Entry<Integer, Float> getTop(int k) {
        return sortedScores.get(k);
    }

    public int getObjectivePosition(int objective) {
        for (int i = 0; i < sortedScores.size; i++) {
            if (sortedScores.get(i).getKey() == objective)
                return i;
        }
        return -1;
    }

    public Array<Entry<Integer, Float>> getSortedScores() {
        return sortedScores;
    }

    /**
     * Represents output format of json response from Tensorflow Serving.
     */
    public static class Wrap{
        Scores[] predictions;
    }

    /**
     * Output format of json result calculated by a prediction model.
     */
    public static class Scores {
        float[] out;
    }
}
