package perdonin.renaissance.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import perdonin.renaissance.MyGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "RENAISSANCE";
		config.width = 1920;
		config.height = 1080;
		config.useGL30 = true;
		config.fullscreen = true;
		new LwjglApplication(new MyGame(), config);
	}
}
