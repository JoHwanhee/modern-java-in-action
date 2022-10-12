package com.example.modernjavainaction.day1;

public class Category {
    public static Category of(String value) {
        return new Category(value);
    }

    public Category(String value) {
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }
}
