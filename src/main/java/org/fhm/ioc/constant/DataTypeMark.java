package org.fhm.ioc.constant;

import java.util.Locale;
import java.util.function.Function;

/**
 * @Classname DataTypeMark
 * @Description TODO
 * @Date 2024/1/8-11:12 AM
 * @Author tanbo
 */
public enum DataTypeMark {

    BOOLEAN(v -> {
        String lowerCase = v.toLowerCase(Locale.ROOT);
        return "true".equals(lowerCase) || "false".equals(lowerCase);
    }, Boolean::valueOf),
    INTEGER(v -> {
        for (char c : v.toCharArray()) {
            if ('0' > c || c > '9') {
                return false;
            }
        }
        return true;
    }, Integer::parseInt);

    private final Function<String, Boolean> judageFunction;

    private final Function<String, Object> castFunction;

    DataTypeMark(Function<String, Boolean> judageFunction, Function<String, Object> castFunction) {
        this.judageFunction = judageFunction;
        this.castFunction = castFunction;
    }

    public static Object obtainData(String value) {
        for (DataTypeMark dataTypeMark : DataTypeMark.values()) {
            if (dataTypeMark.judageFunction.apply(value)) {
                return dataTypeMark.castFunction.apply(value);
            }
        }
        return value;
    }

}
