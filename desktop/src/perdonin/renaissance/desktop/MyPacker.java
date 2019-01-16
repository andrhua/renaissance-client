package perdonin.renaissance.desktop;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class MyPacker {
    public static void main (String[] args) throws Exception {
        String inputDir = "texture_in", outputDir = "android/assets", packFileName = "icons";
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth = 2048;
        settings.maxHeight = 2048;
        settings.filterMin = Texture.TextureFilter.MipMapLinearLinear;
        settings.filterMag = Texture.TextureFilter.MipMap;
        TexturePacker.process(settings, inputDir, outputDir, packFileName);
    }
}