package com.example.modernjavainaction.day1;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.example.modernjavainaction.day1.Colors.GREEN;

public class PredicateTest {
    List<Apple> apples = new ArrayList<>();

    /// before
    public static List<Apple> filterGreenApples(List<Apple> inventory) {
        List<Apple> res = new ArrayList<>();

        for (Apple apple : inventory) {
            if (GREEN.equals(apple.getColor())) {
                res.add(apple);
            }
        }

        return res;
    }

    public static List<Apple> filterHeavyApples(List<Apple> inventory) {
        List<Apple> res = new ArrayList<>();

        for (Apple apple : inventory) {
            if (apple.getWeight() > 150) {
                res.add(apple);
            }
        }

        return res;
    }

    /// after
    // Predicate<T>가 Function<T, Boolean> 보다 더 효율적이다!
    static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p) {
        return inventory.stream()
                .filter(p)
                .collect(Collectors.toList());
    }

    void test() {
        filterApples(apples, Apple::isGreenApple);
        filterApples(apples, Apple::isHeavyApple);
        filterApples(apples, apple -> apple.getWeight() > 150);
        filterApples(apples, apple -> GREEN.equals(apple.getColor()));
    }

    // after 2
    // 값을 파라미터화 하는 것 보다, 동작을 파라미터화 하는게 더 간결하고 유연하다
    void test222() {
        // 값 파라미터화
        filterApples(apples, 150, GREEN);
        // 동작 파라미터화 v1
        filterApples(apples, apple -> apple.getWeight() > 150);
        // 동작 파라미터화 v2
        filterApples(apples, Apple::isHeavyApple);
        // 동작 파라미터화 v3
        filterApples(apples, new HevayAppleFinder(150));
    }
}

class HevayAppleFinder implements Predicate<Apple> {
    private int value;

    public HevayAppleFinder(int value) {
        this.value = value;
    }

    @Override
    public boolean test(Apple apple) {
        return apple.getWeight() > this.value;
    }
}