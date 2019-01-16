package perdonin.renaissance;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;

import perdonin.renaissance.screen.ScreenManager;

public class MyGame extends ApplicationAdapter {
	public ScreenManager sm;
	public AssetManager assets;

	@Override
	public void create () {
		sm = new ScreenManager(this);
		sm.setScreen(ScreenManager.ScreenType.SPLASH);
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void render () {
		sm.update(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		sm.viewport.update(width, height);
	}

	@Override
	public void dispose () {
	}

}
