package com.example.modernjavainaction.day9;

import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;


@ExtensionMethod({
        MainNull.class
})
public class OptionalTest2 {

    // before
    public int readDuration(Properties pros, String name) {
        String value = pros.getProperty(name);
        if (value != null) {
            try {
                int i = Integer.parseInt(value);
                if (i > 0) {
                    return i;
                }
            } catch (NumberFormatException nfe) {
                
            }
        }
        
        return 0;
    }
    
    // after 
    public int readDuration2(Properties pros, String name) {
        return Optional.ofNullable(pros.getProperty(name))
                .map(Integer::parseInt)
                .filter(greaterThan(0))
                .orElse(0);
    }

    private Predicate<Integer> greaterThan(int num) {
        return i -> i > num;
    }

    @Test
    void name() {
//        String s = "";
//        assertThat(s.isNull()).isFalse();
    }
}


class MainNull {
    public static < T > T or(T obj, T defValue) {
        return (obj != null) ? obj : defValue;
    }
    public static boolean isNull(Object obj) {
        return (obj == null) ||
                String.valueOf(obj).equals("null");
    }
    public static < T > Optional < T > whenNotNull(T obj) {
        return Optional.ofNullable(obj);
    }

}