package com.example.modernjavainaction.day1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class GroupingBy {
    List<Apple> apples = new ArrayList<>();

    // before
    Map<Category, List<Apple>> test_before() {
        Map<Category, List<Apple>> applesByCategories = new HashMap<>();
        for (Apple apple : apples) {
            if ("전주".equals(apple.getCategory().getValue())) {
                Category category = apple.getCategory();
                List<Apple> appleList = applesByCategories.get(category);
                if (appleList == null) {
                    appleList = new ArrayList<>();
                    applesByCategories.put(category, appleList);
                }
                appleList.add(apple);
            }
        }

        return applesByCategories;
    }

    // after
    Map<Category, List<Apple>> test_after() {
        return apples.stream()
                .filter(a -> a.getCategory().equals(Category.of("전주")))
                .collect(groupingBy(Apple::getCategory));
    }
}

