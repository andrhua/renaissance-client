package perdonin.renaissance.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import perdonin.renaissance.Const;
import perdonin.renaissance.Utils;
import perdonin.renaissance.game.GameSession;
import perdonin.renaissance.game.InferenceHelper;
import perdonin.renaissance.game.InkListener;
import perdonin.renaissance.ui.Canvas;
import perdonin.renaissance.ui.Colors;

public class GameScreen extends BaseScreen implements PropertyChangeListener {
    private GameSession session;
    private Table drawTable, taskTable, endTable;
    private enum State{TASK, DRAW, END}
    private State state;
    private Label timerLabel, roundLabel, summaryLabel;
    private int time, objective, round = 0;
    private Timer.Task timer;
    private Canvas canvas;
    private Label target, objectiveLabel, predictionLabel;
    private InferenceHelper helper;
    private InkListener inkListener;
    private Array<TextButton> drawings;
    private TextButton.TextButtonStyle resultStyle;
    private boolean alreadyProcessed = false;
    private StringBuilder response;

    GameScreen(ScreenManager sm, GameSession session) {
        super(sm);
        this.session = session;
        response = new StringBuilder();
        try {
            helper = new InferenceHelper();
            helper.addPropertyChangeListener("scores", this);
            inkListener = new InkListener(canvas, helper);
            canvas.addListener(inkListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initUI() {
        TextureAtlas icons = assets.get("icons.atlas", TextureAtlas.class);
        taskTable = new Table();
        taskTable.setFillParent(true);
        taskTable.setSize(Const.WIDTH, Const.HEIGHT);
        Label.LabelStyle taskLS = new Label.LabelStyle(assets.get("task.ttf"), Color.BLACK);
        Label.LabelStyle targetLS = new Label.LabelStyle(assets.get("regular.ttf"), Color.WHITE);
        Label draw = new Label("draw", taskLS);
        target = new Label("banana", targetLS);
        Label timeBounds = new Label("in under " + Const.ROUND_TIME + " seconds", taskLS);
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = assets.get("timer.ttf");
        tbs.fontColor = Color.WHITE;
        TextButton startButton = uiBuilder.getTextButton("Got it!", tbs, ()-> setState(State.DRAW), Color.WHITE);
        startButton.addAction(Actions.forever(
                Actions.sequence(
                        Actions.alpha(1, 1),
                        Actions.delay(1),
                        Actions.alpha(.25f, .8f)
                )
        ));
        taskTable.add(draw).padTop(Const.height(.1f)).row();
        taskTable.add(target).row();
        taskTable.add(timeBounds).row();
        taskTable.add(startButton).padTop(Const.height(.15f)).bottom().row();

        Label.LabelStyle statusbarLS = new Label.LabelStyle(assets.get("timer.ttf"), Color.WHITE);
        Label.LabelStyle classyLS = new Label.LabelStyle(assets.get("task.ttf"), Color.WHITE);
        objectiveLabel = new Label("1+\n2", classyLS);
        objectiveLabel.setWrap(true);
        objectiveLabel.setAlignment(Align.center);
        roundLabel = new Label("1 / 5", classyLS);
        roundLabel.setAlignment(Align.left);
        timerLabel = new Label("00:70", statusbarLS);
        timerLabel.setColor(1, 1, 1, .45f);
        timerLabel.setAlignment(Align.right);
        ImageButton eraser = uiBuilder.getImageButton(icons.findRegion("eraser"), ()-> {
            canvas.reset();
            inkListener.reset();
            predictionLabel.setText("...");
        }, Color.WHITE);
        ImageButton skip = uiBuilder.getImageButton(icons.findRegion("skip"), () -> nextRound(session.setResult(GameSession.Result.USER_GAVE_UP)), Color.WHITE);
        ImageButton exit = uiBuilder.getImageButton(icons.findRegion("exit"), () -> sm.setScreen(ScreenManager.ScreenType.MENU), Color.RED);
        canvas = new Canvas();
        predictionLabel = new Label("...", statusbarLS);
        predictionLabel.setColor(1, 1, 1, .6f);
        drawTable = new Table();
        drawTable.setFillParent(true);
        drawTable.setDebug(false);
        Table buttons = new Table();
        buttons.defaults().expandX().size(Const.CANVAS_SIZE * .15f).pad(0, Const.widthInt(0.05f), 0, Const.widthInt(0.05f));
        buttons.add(eraser, skip, exit);
        drawTable.defaults().width(Const.width(.5f));
        drawTable.top().add(roundLabel);
        drawTable.top().add(timerLabel).row();
        drawTable.defaults().reset();
        drawTable.defaults().colspan(2);
        drawTable.add(objectiveLabel).width(Const.WIDTH).center().row();
        drawTable.add(canvas).padTop(Const.height(.05f)).row();
        drawTable.add(buttons).center().padTop(Const.height(.05f)).row();
        drawTable.add(predictionLabel).padTop(Const.height(0.035f)).center();

        endTable = new Table();
        endTable.setFillParent(true);
        //endTable.setBackground(npd);
        endTable.setDebug(true);
        endTable.setSize(Const.WIDTH, Const.HEIGHT);
        Label resultLabel = new Label("You suck!", targetLS);
        summaryLabel = new Label("", classyLS);
        summaryLabel.setWrap(true);
        endTable.add(resultLabel).align(Align.center).expandX().colspan(2).padBottom(Const.height(.1f)).row();
        endTable.center().add(summaryLabel).align(Align.center).colspan(2).padBottom(Const.height(.05f)).row();
        endTable.defaults().center().padBottom(Const.height(.05f));
        TextButton.TextButtonStyle captionStyle = new TextButton.TextButtonStyle();
        captionStyle.font = assets.get("caption.ttf");
        captionStyle.fontColor = Color.RED;
        drawings = new Array<>(6);
        for (int i = 0; i < 6; i++) {
            TextButton tb = new TextButton("1", captionStyle);
            drawings.add(tb);
            endTable.add(tb).size(Const.height(.15f)).center();
            if (i % 2 != 0){
                endTable.row();
            }
        }
        resultStyle = new TextButton.TextButtonStyle();
        resultStyle.font = assets.get("caption.ttf");
        resultStyle.fontColor = Color.BLACK;
        ImageButton exit_ = uiBuilder.getImageButton(icons.findRegion("exit"), ()->sm.setScreen(ScreenManager.ScreenType.MENU), Color.RED);
        endTable.add(exit_).colspan(2).center().size(Const.CANVAS_SIZE * .15f);
    }

    @Override
    protected void onShow(Stage stage) {
        taskTable.setBackground(uiBuilder.getBackground(Colors.LOGO));
        canvas.initGL();
        Gdx.input.setInputProcessor(stage);
        stage.addActor(drawTable);
        stage.addActor(taskTable);
        stage.addActor(endTable);
        drawTable.setVisible(false);
        endTable.setVisible(false);
        nextRound(session.init());
    }

    @Override
    public void update(float delta) {
        helper.update(delta);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        processScores((float[]) e.getNewValue());
    }

    private void setState(State state){
        switch (state){
            case TASK:
                target.setText(Const.categories.get(objective));
                time = Const.ROUND_TIME;
                if (timer != null) timer.cancel();
                taskTable.addAction(Actions.sequence(
                        Actions.visible(true),
                        Actions.alpha(0),
                        Actions.moveTo(0, - Const.HEIGHT),
                        Actions.parallel(
                                Actions.moveBy(0, Const.HEIGHT, .7f, Interpolation.exp10Out),
                                Actions.fadeIn(.6f, Interpolation.exp10In)
                        ),
                        Actions.run(()->drawTable.setVisible(false))
                ));
                break;
            case DRAW:
                drawTable.setVisible(true);
                drawTable.setTouchable(Touchable.enabled);
                canvas.reset();
                roundLabel.setText(round + "/" + Const.ROUNDS);
                timerLabel.setText("00:".concat(String.valueOf(Const.ROUND_TIME)));
                objectiveLabel.setText("Draw:\n" + Const.categories.get(objective));
                predictionLabel.setText("...");
                taskTable.addAction(Actions.sequence(
                        Actions.parallel(
                                Actions.moveBy(0, -Const.HEIGHT, .5f, Interpolation.exp5In),
                                Actions.fadeOut(.4f, Interpolation.exp5Out)
                        ),
                        Actions.visible(false)
                ));
                timer = new Timer.Task(){
                    @Override
                    public void run() {
                        time--;
                        timerLabel.setText("00:".concat((time + 1 < 10 ? "0" : "").concat(String.valueOf(time + 1))));
                        if (time < 0){
                            //nextRound(session.setResult(GameSession.Result.TIME_UP));
                        }
                    }
                };
                Timer.schedule(timer, 0, 1, Const.ROUND_TIME);
                break;
            case END:
                if (timer != null) timer.cancel();
                summaryLabel.setText("Neural net recognized " + session.getRecognized() + " your drawings");
                summaryLabel.invalidate();
                endTable.setVisible(true);
                endTable.addAction(Actions.sequence(
                        Actions.alpha(0),
                        Actions.fadeIn(.65f, Interpolation.exp10In),
                        Actions.run(()-> drawTable.setVisible(false))
                ));
                break;
        }
        this.state = state;
    }

    private void nextRound(int objective){
        drawTable.setTouchable(Touchable.disabled);
        alreadyProcessed = false;
        if (1 <= round && round <= 6){
            TextButton tb = drawings.get(round - 1);
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(resultStyle);
            style.up = canvas.getRawDrawable();
            style.fontColor = session.getGuess(round - 1) ? Colors.RECOGNIZED : Colors.UNRECOGNIZED;
            tb.setStyle(style);
            tb.setText(Const.categories.get(this.objective));
            tb.getLabelCell().padTop(Const.height(.12f));
        }
        this.objective = objective;
        round++;
        if (objective >= 0){
            setState(State.TASK);
        } else {
            alreadyProcessed = true;
            setState(State.END);
        }
    }

    private void processScores(float[] scores){
        if (!alreadyProcessed){
            response.setLength(0);
            List<Map.Entry<Integer, Float>> sorted = Utils.sort(scores);
            if (sorted.get(0).getValue() < Const.RECOGNIZABLE){
                response.append("This is a mess!");
            } else {
                if (sorted.get(0).getKey() == objective || scores[objective] > Const.WIN_PROB) {
                    response.append("Oh, I see ").append(Const.categories.get(objective)).append("!");
                    drawTable.setTouchable(Touchable.disabled);
                    alreadyProcessed = true;
                    timer.cancel();
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            nextRound(session.setResult(GameSession.Result.SUCCESS));
                        }
                    }, 1.75f);

                } else {
                    response.append("I see");
                    for (int i = 0; i < 4; i++){
                        Map.Entry<Integer, Float> entry = sorted.get(i);
                        if (entry.getValue() >= Const.RECOGNIZABLE){
                            response.append(i == 0 ? " " : ", ")
                                    .append(Const.categories.get(entry.getKey()));
                        } else break;
                    }
                }
            }
            predictionLabel.setText(response.toString());
        }

    }
}
