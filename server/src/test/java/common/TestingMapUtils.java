package common;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TestingMapUtils {

    public static <K, V1, V2> V1 merge(Map<K, V1> initialMap, K key, V2 value,
                                Function<? super V2,? extends V1> mapValues,
                                BiFunction<? super V1,? super V2,? extends V1> combineValues) {
        V1 mergedVal;
        if(!initialMap.containsKey(key))
            mergedVal = mapValues.apply(value);
        else
            mergedVal = combineValues.apply(initialMap.get(key), value);
        initialMap.put(key, mergedVal);

        return mergedVal;
    }

    public static <K1, K2, V1, V2> void MergeNested(Map<K1, Map<K2, V1>> sourceMap, Map<K1, Map<K2, V2>> destinationMap,
                                            BiFunction<? super K2,? super V1,? extends V2> mapValue,
                                              BiFunction<? super V1,? super V2,? extends V2> combineValues) {

        sourceMap.forEach((srcKey1, srcKey1Dict) -> {
                    Map<K2, V2> destKey1Dict;
                    if (!destinationMap.containsKey(srcKey1)) {
                        destKey1Dict = new HashMap<>();
                        srcKey1Dict.forEach((k, v) -> destKey1Dict.put(k, mapValue.apply(k, v)));
                    } else {
                        destKey1Dict = destinationMap.get(srcKey1);
                        destKey1Dict.replaceAll((k, v) -> combineValues.apply(srcKey1Dict.get(k), v));
                    }
                    destinationMap.put(srcKey1, destKey1Dict);
                });
    }

}
