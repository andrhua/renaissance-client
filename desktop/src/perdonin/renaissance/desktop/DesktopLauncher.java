package perdonin.renaissance.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import perdonin.renaissance.MyGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "RENAISSANCE";
		config.width = 540;
		config.height = 960;
		new LwjglApplication(new MyGame(), config);
	}
}
