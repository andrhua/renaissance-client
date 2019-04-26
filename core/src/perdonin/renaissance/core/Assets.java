package perdonin.renaissance.core;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.utils.I18NBundle;

public class Assets {
    private interface IRun {
        void run(String file, int size, String assetsName);
    }
    public AssetManager manager;

    public Assets(){
        manager = new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
        manager.setLoader(TextureAtlas.class, new TextureAtlasLoader(resolver));
        manager.load("gfx/icons.atlas", TextureAtlas.class);
        manager.load("i18n/strings", I18NBundle.class);

        IRun createFTF = (String file, int size, String assetsName)->{
            FreetypeFontLoader.FreeTypeFontLoaderParameter ftfp = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
            ftfp.fontParameters.characters="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!'.,:/-";
            ftfp.fontFileName = file;
            ftfp.fontParameters.size = size;
            ftfp.fontParameters.minFilter = Texture.TextureFilter.Linear;
            ftfp.fontParameters.magFilter= Texture.TextureFilter.Linear;
            manager.load(assetsName, BitmapFont.class, ftfp);
        };
        createFTF.run("fonts/classy.otf", Const.heightInt(.0625f), "main.ttf");
        createFTF.run("fonts/classy.otf", Const.heightInt(.091f), "button.ttf");
        createFTF.run("fonts/classy.otf", Const.heightInt(.048f), "task.ttf");
        createFTF.run("fonts/firacode.otf", Const.heightInt(.031f), "timer.ttf");
        createFTF.run("fonts/firacode.otf", Const.heightInt(.016f), "caption.ttf");
    }
}
