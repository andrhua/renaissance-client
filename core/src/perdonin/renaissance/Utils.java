package perdonin.renaissance;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class Utils {
    public static int argmax(float[] arr){
        int idx = 0;
        float max = arr[0];
        for (int i = 1; i < arr.length; i++){
            if (arr[i] > max) {
                max = arr[i];
                idx = i;
            }
        }
        return idx;
    }

    public static ArrayList<Map.Entry<Integer, Float>> sort(float[] arr){
        ArrayList<Map.Entry<Integer, Float>> list = new ArrayList<>();
        for (int i = 0; i < arr.length; i++){
            list.add(new AbstractMap.SimpleEntry<>(i, arr[i]));
        }
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        return list;
    }
}
