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
import perdonin.renaissance.core.Const;
import perdonin.renaissance.game.DrawingListener;
import perdonin.renaissance.game.GameSession;
import perdonin.renaissance.inference.GoogleCloudPredictionBackend;
import perdonin.renaissance.inference.InferenceHelper;
import perdonin.renaissance.ui.Canvas;
import perdonin.renaissance.ui.Colors;

import java.util.Map;

public class GameScreen extends BaseScreen{
    private final GameSession gameSession;
    private final InferenceHelper inferenceHelper;
    private final DrawingListener drawListener;
    private enum State {TASK, DRAW, END}
    private Table drawTable;
    private Table taskTable;
    private Table endTable;
    private Canvas canvas;
    private Label objectiveTaskLabel;
    private Label objectiveDrawLabel;
    private Label predictionLabel;
    private Label timerLabel;
    private Label roundLabel;
    private Label summaryLabel;
    private Array<TextButton> drawings;
    private TextButton.TextButtonStyle resultStyle;

    GameScreen(ScreenManager sm, GoogleCloudPredictionBackend gcpb) {
        super(sm);
        gameSession = new GameSession(this);
        inferenceHelper = new InferenceHelper(gameSession, gcpb);
        drawListener = new DrawingListener(canvas, inferenceHelper);
        canvas.addListener(drawListener);
    }

    @Override
    protected void initUI() {
        TextureAtlas icons = assets.get("gfx/icons.atlas", TextureAtlas.class);
        Label.LabelStyle taskTextLS = new Label.LabelStyle(assets.get("task.ttf"), Color.WHITE);
        initTaskTable();
        initDrawTable(taskTextLS, icons);
        initResultTable(taskTextLS, icons);
    }

    private void initTaskTable() {
        taskTable = new Table();
        taskTable.setFillParent(true);
        taskTable.setSize(Const.WIDTH, Const.HEIGHT);
        Label.LabelStyle taskLS = new Label.LabelStyle(assets.get("task.ttf"), Color.BLACK);
        Label.LabelStyle targetLS = new Label.LabelStyle(assets.get("main.ttf"), Color.WHITE);
        Label draw = new Label(i18n.get("draw"), taskLS);
        objectiveTaskLabel = new Label("banana", targetLS);
        Label timeLimit = new Label(i18n.format("timeLimit", Const.ROUND_TIME), taskLS);
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = assets.get("timer.ttf");
        tbs.fontColor = Color.WHITE;
        TextButton startButton = uiBuilder.getTextButton(i18n.get("confirmation"), tbs, () -> setState(State.DRAW), Color.WHITE);
        startButton.addAction(Actions.forever(
                Actions.sequence(
                        Actions.alpha(1, 1),
                        Actions.delay(1),
                        Actions.alpha(.25f, .8f)
                )
        ));
        taskTable.add(draw).padTop(Const.height(.1f)).row();
        taskTable.add(objectiveTaskLabel).row();
        taskTable.add(timeLimit).row();
        taskTable.add(startButton).padTop(Const.height(.15f)).bottom().row();
    }

    private void initDrawTable(Label.LabelStyle taskTextLS, TextureAtlas icons) {
        Label.LabelStyle statusbarLS = new Label.LabelStyle(assets.get("timer.ttf"), Color.WHITE);
        objectiveDrawLabel = new Label("", taskTextLS);
        objectiveDrawLabel.setWrap(true);
        objectiveDrawLabel.setAlignment(Align.center);
        roundLabel = new Label("", taskTextLS);
        roundLabel.setAlignment(Align.left);
        timerLabel = new Label("", statusbarLS);
        timerLabel.setColor(1, 1, 1, .45f);
        timerLabel.setAlignment(Align.right);
        ImageButton eraser = uiBuilder.getImageButton(icons.findRegion("eraser"), this::clearCanvas, Color.WHITE);
        ImageButton skip = uiBuilder.getImageButton(icons.findRegion("skip"), this::skipRound, Color.WHITE);
        ImageButton exit = uiBuilder.getImageButton(icons.findRegion("exit"), this::exitGameSession, Color.RED);
        canvas = new Canvas();
        predictionLabel = new Label("", statusbarLS);
        predictionLabel.setColor(1, 1, 1, .6f);

        Table buttons = new Table();
        buttons.defaults().expandX().size(Const.CANVAS_SIZE * .15f).pad(0, Const.canvasInt(0.05f), 0, Const.canvasInt(0.05f));
        buttons.add(eraser, skip, exit);

        drawTable = new Table();
        drawTable.setFillParent(true);
        drawTable.setDebug(false);
        drawTable.defaults().width(Const.width(.5f));
        drawTable.top().add(roundLabel);
        drawTable.top().add(timerLabel).row();
        drawTable.defaults().reset();
        drawTable.defaults().colspan(2);
        drawTable.add(objectiveDrawLabel).width(Const.WIDTH).center().row();
        drawTable.add(canvas).padTop(Const.canvas(.05f)).row();
        drawTable.add(buttons).center().padTop(Const.canvas(.05f)).row();
        drawTable.add(predictionLabel).padTop(Const.canvas(0.1f)).center();
    }

    private void clearCanvas() {
        canvas.reset();
        drawListener.reset();
        predictionLabel.setText(i18n.get("emptyCanvasGuess"));
    }

    private void skipRound() {
        gameSession.skipRound();
    }

    private void exitGameSession() {
        sm.setScreen(ScreenManager.ScreenType.MENU);
    }

    private void initResultTable(Label.LabelStyle taskTextLS, TextureAtlas icons) {
        endTable = new Table();
        endTable.setFillParent(true);
        endTable.setSize(Const.WIDTH, Const.HEIGHT);
        summaryLabel = new Label("", taskTextLS);
        summaryLabel.setAlignment(Align.center);
        endTable.add(summaryLabel).align(Align.center).width(Const.WIDTH).colspan(2)
                .padTop(Const.height(.05f)).padBottom(Const.height(.05f)).row();
        endTable.defaults().center().padBottom(Const.height(.05f));
        TextButton.TextButtonStyle captionStyle = new TextButton.TextButtonStyle();
        captionStyle.font = assets.get("caption.ttf");
        captionStyle.fontColor = Color.RED;
        drawings = new Array<>(6);
        for (int i = 0; i < 6; i++) {
            TextButton tb = new TextButton("1", captionStyle);
            drawings.add(tb);
            endTable.add(tb).size(Const.height(.15f)).center();
            if (i % 2 != 0) {
                endTable.row();
            }
        }
        resultStyle = new TextButton.TextButtonStyle();
        resultStyle.font = assets.get("caption.ttf");
        resultStyle.fontColor = Color.BLACK;
        ImageButton exit_ = uiBuilder.getImageButton(icons.findRegion("exit"), () -> sm.setScreen(ScreenManager.ScreenType.MENU), Color.RED);
        endTable.add(exit_).colspan(2).center().size(Const.CANVAS_SIZE * .15f);
    }

    @Override
    protected void onShow(Stage stage) {
        // stage.setDebugAll(true);
        taskTable.setBackground(uiBuilder.getBackground(Colors.LOGO));
        canvas.initGraphicalOps();
        Gdx.input.setInputProcessor(stage);
        stage.addActor(drawTable);
        stage.addActor(taskTable);
        stage.addActor(endTable);
        drawTable.setVisible(false);
        endTable.setVisible(false);
        gameSession.init();
    }

    @Override
    public void update(float delta) {
        inferenceHelper.update(delta);
    }

    private void setState(State state) {
        switch (state) {
            case TASK:
                drawTable.setTouchable(Touchable.disabled);
                taskTable.addAction(Actions.sequence(
                        Actions.visible(true),
                        Actions.alpha(0),
                        Actions.moveTo(0, -Const.HEIGHT),
                        Actions.parallel(
                                Actions.moveBy(0, Const.HEIGHT, .7f, Interpolation.exp10Out),
                                Actions.fadeIn(.6f, Interpolation.exp10In)
                        ),
                        Actions.run(() -> drawTable.setVisible(false))
                ));
                break;
            case DRAW:
                drawTable.setVisible(true);
                drawTable.setTouchable(Touchable.enabled);
                canvas.reset();
                predictionLabel.setText(i18n.get("emptyCanvasGuess"));
                taskTable.addAction(Actions.sequence(
                        Actions.parallel(
                                Actions.moveBy(0, -Const.HEIGHT, .5f, Interpolation.exp5In),
                                Actions.fadeOut(.4f, Interpolation.exp5Out)
                        ),
                        Actions.visible(false),
                        Actions.run(gameSession::startTimer)
                ));
                break;
            case END:
                summaryLabel.setText(i18n.format("summary", gameSession.getRecognizedCount()));
                endTable.setVisible(true);
                endTable.addAction(Actions.sequence(
                        Actions.alpha(0),
                        Actions.fadeIn(.65f, Interpolation.exp10In),
                        Actions.run(() -> drawTable.setVisible(false))
                ));
                summaryLabel.setWrap(true);
                break;
        }
    }

    public void updateDrawingPrediction(Array<Map.Entry<Integer, Float>> predictions) {
        StringBuilder response = new StringBuilder();
        if (predictions == null) {
            response.append(i18n.random("mess"));
        } else {
            if (predictions.size == 1) {
                inferenceHelper.reset();
                response.append(i18n.format("success", Const.categories.get(gameSession.getObjective())));
            } else {
                response.append(i18n.get("guesses"));
                for (int i = 0; i < 4; i++) {
                    Map.Entry<Integer, Float> entry = predictions.get(i);
                    if (entry.getValue() < Const.RECOGNIZABLE) {
                        break;
                    } else {
                        response.append(i == 0 ? " " : ", ")
                                .append(Const.categories.get(entry.getKey()));
                    }
                }
            }
        }
        predictionLabel.setText(response.toString());
    }

    public void updateTimer(int time) {
        timerLabel.setText(i18n.format("time", time));
    }

    public void onRoundStart(int objective, int round) {
        if (objective >= 0) {
            setState(State.TASK);
            objectiveTaskLabel.setText(Const.categories.get(objective));
            taskTable.addAction(Actions.sequence(
                    Actions.delay(.7f),
                    Actions.run(()-> {
                        objectiveDrawLabel.setText(i18n.format("objective", Const.categories.get(objective)));
                        roundLabel.setText(i18n.format("rounds", round + 1, Const.ROUNDS));
                        updateTimer(Const.ROUND_TIME);
                    })
            ));
        }
        else {
            setState(State.END);
        }
    }

    public void onRoundFinish(int objective, int round, boolean isSuccessful) {
        TextButton tb = drawings.get(round);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(resultStyle);
        style.up = canvas.getRawDrawable();
        style.fontColor = isSuccessful ? Colors.RECOGNIZED : Colors.UNRECOGNIZED;
        tb.setStyle(style);
        tb.setText(Const.categories.get(objective));
        tb.getLabelCell().padTop(Const.height(.12f));
    }

}
