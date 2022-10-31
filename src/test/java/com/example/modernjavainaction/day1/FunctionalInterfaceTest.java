package com.example.modernjavainaction.day1;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class FunctionalInterfaceTest {
    // 추상 인터페이스가 하나만 있는 것을 함수형 인터페이스라고 한다.
    // Predicate 나 Comparer같은것이 전형적인 예
    // 추상을 하나 선언해두고, 새로운 것이 추가되면, 하위호환을 위해 default method로 만든다.
    @FunctionalInterface
    public interface MyPredicate<T> {
        boolean test(T t);

        default boolean not(T t) {
            return !test(t);
        }
    }

    List<Apple> apples = new ArrayList<>();
    void myfilter(MyPredicate<Apple> p) {
        apples.stream().filter(a -> p.not(a));
        apples.stream().filter(a -> p.test(a));

        // MyPredicate<Apple> aa = (a) -> { s};
    }





    void test() {
//        Consumer<Apple> appleConsumer; // T 받아서 void 반환
//        Predicate<Apple> appleConsumer; // T 받아서 bool 반환
//        Function<Apple, Apple> appleConsumer; // T 받아서 R 반환
    }
}
