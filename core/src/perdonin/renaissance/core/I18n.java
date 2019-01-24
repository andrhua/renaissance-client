package perdonin.renaissance.core;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.I18NBundle;

public class I18n {
    private I18NBundle bundle;

    public I18n(AssetManager am){
        bundle = am.get("i18n/strings", I18NBundle.class);
    }

    public String get(String key){
        return bundle.get(key);
    }

    public String format(String key, Object... args){
        return bundle.format(key, args);
    }

    public String random(String key){
        String[] values = bundle.format(key).split("#");
        return values[(int)(System.currentTimeMillis() % values.length)];
    }
}
