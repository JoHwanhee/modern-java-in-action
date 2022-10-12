package com.example.modernjavainaction.day1;

import static com.example.modernjavainaction.day1.Colors.GREEN;

public class Apple {
    private Integer color = 0;
    private Integer weight = 0;
    private Category category = Category.of("전주");

    public Integer getColor() {
        return color;
    }

    public Integer getWeight() {
        return weight;
    }

    public static boolean isHeavyApple(Apple apple) {
        return apple.getWeight() > 150;
    }

    public static boolean isGreenApple(Apple apple) {
        return GREEN.equals(apple.getColor());
    }

    public Category getCategory() {
        return category;
    }
}