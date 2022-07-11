package com.pig.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> List<T> merge(List<T> firstList, List<T> secondList) {
        List<T> mergedList = new ArrayList<>();
        if (firstList != null) {
            mergedList.addAll(firstList);
        }
        if (secondList != null) {
            mergedList.addAll(secondList);
        }
        return mergedList;
    }
}
