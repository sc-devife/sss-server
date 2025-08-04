package com.sss.app.util;

import java.util.Objects;

public class CompareUtil {
    public static <T> boolean hasChanged(T newValue, T currentValue) {
        return !Objects.equals(newValue, currentValue);
    }
}
