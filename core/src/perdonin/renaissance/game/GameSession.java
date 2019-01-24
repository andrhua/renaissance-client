package perdonin.renaissance.game;

import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

import perdonin.renaissance.core.Const;

public class GameSession {
    public enum Result {SUCCESS, TIME_UP, USER_GAVE_UP}
    private Array<Integer> indices = new Array<>(Const.categories.size);
    private Iterator<Integer> iterator;
    private int i = 0, recognized, round;
    private boolean[] guesses = new boolean[6];

    public GameSession(){
        for (int i = 0; i < Const.categories.size; i++){
            indices.add(i);
        }
        indices.shuffle();
    }

    public int init(){
        recognized = 0;
        round = 0;
        if (i + Const.ROUNDS >= indices.size){
            indices.shuffle();
            i = 0;
        }
        Array<Integer> set = new Array<>();
        set.addAll(indices, i, Const.ROUNDS);
        i += Const.ROUNDS;
        iterator = set.iterator();
        return nextObjective();
    }

    public int setResult(Result result){
        switch (result){
            case SUCCESS:
                recognized++;
                guesses[round++] = true;
                break;
            case TIME_UP:
            case USER_GAVE_UP:
                guesses[round++] = false;
                break;
        }
        return nextObjective();
    }

    private int nextObjective(){
        if (iterator.hasNext())
            return iterator.next();
        else
            return -1;
    }

    public int getRecognized() {
        return recognized;
    }

    public boolean getGuess(int i){
        return this.guesses[i];
    }
}
