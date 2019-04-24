package perdonin.renaissance.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import perdonin.renaissance.MyGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "RENAISSANCE";
		config.width = 1280;
		config.height = 720;
		config.useGL30 = true;
		new LwjglApplication(new MyGame(), config);
	}
}
