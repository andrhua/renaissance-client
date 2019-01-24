package perdonin.renaissance.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import perdonin.renaissance.core.Const;
import perdonin.renaissance.ui.Colors;

public class MenuScreen extends BaseScreen {
    private Table table;

    MenuScreen(ScreenManager sm) {
        super(sm);
    }

    @Override
    protected void initUI() {
        TextButton.TextButtonStyle tbs1 = new TextButton.TextButtonStyle();
        tbs1.font = assets.get("menu.ttf");
        tbs1.fontColor = Colors.LOGO;
        TextButton start = uiBuilder.getTextButton(i18n.get("buttonDraw"), tbs1, ()-> sm.setScreen(ScreenManager.ScreenType.GAME), Colors.LOGO);
        Label motto = new Label(i18n.get("greeting"), new Label.LabelStyle(assets.get("regular.ttf"), Color.WHITE));
        motto.setAlignment(Align.center);
        table = new Table();
        table.setFillParent(true);
        table.defaults().expandY();
        table.add(motto).width(Const.WIDTH * .9f).bottom().padBottom(Const.HEIGHT / 16).row();
        table.add(start).top().row();
        motto.setWrap(true);
    }

    @Override
    protected void onShow(Stage stage) {
        super.onShow(stage);
        stage.addActor(table);
    }

}
