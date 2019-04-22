package perdonin.renaissance.game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import perdonin.renaissance.core.Const;
import perdonin.renaissance.inference.PredictionResponse;
import perdonin.renaissance.screen.GameScreen;

import java.util.Iterator;

public class GameSession {
    private final GameScreen gameScreen;
    private Array<Integer> categories = new Array<>(Const.categories.size);
    private Iterator<Integer> sessionObjectives;
    private int latestObjectiveIndex = 0;
    private int round = 0;
    private boolean[] predictions = new boolean[6];
    private Timer.Task timer;
    private int time;
    private int objective;

    public GameSession(GameScreen gameScreen){
        this.gameScreen = gameScreen;
        for (int i = 0; i < Const.categories.size; i++){
            categories.add(i);
        }
        categories.shuffle();
    }

    public void init(){
        if (latestObjectiveIndex + Const.ROUNDS >= categories.size){
            categories.shuffle();
            latestObjectiveIndex = 0;
        }
        Array<Integer> array = new Array<>();
        array.addAll(categories, latestObjectiveIndex, Const.ROUNDS);
        latestObjectiveIndex += Const.ROUNDS;
        sessionObjectives = array.iterator();
        resetTimer();
        startNextRound(false);
    }

    public void startTimer() {
        Timer.schedule(timer, 0, 1, Const.ROUND_TIME);
    }

    public boolean isSuccessfulRecognition(PredictionResponse response) {
        if (response.getTop(0).getValue() < Const.RECOGNIZABLE){
            gameScreen.updateDrawingPrediction(null);
            return false;
        } else {
            int position = response.getObjectivePosition(objective);
            if (position <= Const.POSITION_TO_WIN) {
                resetTimer();
                gameScreen.updateDrawingPrediction(Array.with(response.getTop(position)));
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        startNextRound(true);
                    }
                }, 1.75f);
                return true;
            } else {
                gameScreen.updateDrawingPrediction(response.getSortedScores());
                return false;
            }
        }
    }

    private void startNextRound(boolean isSuccessful) {
        if (round > 0) predictions[round - 1] = isSuccessful;
        objective = sessionObjectives.hasNext()
                ? sessionObjectives.next()
                : -1;
        gameScreen.onRoundStart(objective, round++);
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        time = Const.ROUND_TIME;
        timer = new Timer.Task(){
            @Override
            public void run() {
                time--;
                gameScreen.updateTimer(time);
                if (time == 0) {
                    startNextRound(false);
                }
            }
        };
    }

    public void skipRound() {
        resetTimer();
        startNextRound(false);
    }

    public int getRecognized() {
        int recognized = 0;
        for (boolean prediction: predictions)
            if (prediction) recognized += 1;
        return recognized;
    }

    public boolean getGuess(int i){
        return this.predictions[i];
    }

    public int getObjective() {
        return objective;
    }

}
