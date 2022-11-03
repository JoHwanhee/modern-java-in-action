package com.example.modernjavainaction.day11;

import lombok.Getter;
import lombok.Value;

import static com.example.modernjavainaction.day11.CompletableFutureTest2.delay;


public class Shop {
    String name = "hello world";

    float price;

    public Shop(String name, float price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        delay();
        return price;
    }
}
