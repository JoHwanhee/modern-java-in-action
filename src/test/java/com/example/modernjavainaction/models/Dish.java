package com.example.modernjavainaction.models;

import com.example.modernjavainaction.day4.CollectorTest;
import lombok.Value;

public class Dish {
    CollectorTest.CaloricLevel level;

    public CollectorTest.CaloricLevel getLevel() {
        return CollectorTest.CaloricLevel.FAT;
    }

    public int getCalories() {
        return 10;
    }


}
