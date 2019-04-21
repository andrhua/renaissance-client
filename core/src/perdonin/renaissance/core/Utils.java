package perdonin.renaissance.core;

import com.badlogic.gdx.utils.Array;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;

public class Utils {
    public static Array<Map.Entry<Integer, Float>> sort(float[] arr){
        Array<Map.Entry<Integer, Float>> list = new Array<>();
        for (int i = 0; i < arr.length; i++){
            list.add(new AbstractMap.SimpleEntry<>(i, arr[i]));
        }
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        return list;
    }
}
