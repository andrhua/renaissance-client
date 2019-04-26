package perdonin.renaissance.game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import perdonin.renaissance.core.Const;
import perdonin.renaissance.inference.PredictionResponse;
import perdonin.renaissance.screen.GameScreen;

import java.util.Iterator;

public class GameSession {
    private final GameScreen gameScreen;
    private final Array<Integer> categories = new Array<>(Const.categories.size);
    private Iterator<Integer> sessionObjectives;
    private Timer.Task timer;
    private int latestObjectiveIndex = 0;
    private int round;
    private int time;
    private int objective;
    private int recognizedCount;
    private boolean isSleep;

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
        round = 0;
        recognizedCount = 0;
        isSleep = false;
        resetTimer();
        startNextRound();
    }

    public void startTimer() {
        Timer.schedule(timer, 0, 1, Const.ROUND_TIME);
    }

    public boolean isSuccessfulRecognition(PredictionResponse response) {
        if (!isSleep) {
            if (response.getTop(0).getValue() < Const.RECOGNIZABLE) {
                gameScreen.updateDrawingPrediction(null);
                return false;
            } else {
                int position = response.getObjectivePosition(objective);
                if (position <= Const.POSITION_TO_WIN) {
                    isSleep = true;
                    resetTimer();
                    gameScreen.updateDrawingPrediction(Array.with(response.getTop(position)));
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            finishRound(true);
                            startNextRound();
                            isSleep = false;
                        }
                    }, 1.75f);
                    return true;
                } else {
                    gameScreen.updateDrawingPrediction(response.getSortedScores());
                    return false;
                }
            }
        }
        return false;
    }

    private void finishRound(boolean isSuccessful) {
        if (isSuccessful) recognizedCount++;
        gameScreen.onRoundFinish(objective, round++, isSuccessful);
    }

    private void startNextRound() {
        objective = sessionObjectives.hasNext()
                ? sessionObjectives.next()
                : -1;
        gameScreen.onRoundStart(objective, round);
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        time = Const.ROUND_TIME;
        timer = new Timer.Task(){
            @Override
            public void run() {
                time--;
                gameScreen.updateTimer(time);
                if (!isSleep && time == 0) {
                    finishRound(false);
                    startNextRound();
                }
            }
        };
    }

    public void skipRound() {
        resetTimer();
        finishRound(false);
        startNextRound();
    }

    public int getRecognizedCount() {
        return recognizedCount;
    }

    public int getObjective() {
        return objective;
    }

}
