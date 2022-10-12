package com.example.modernjavainaction.day1;

import org.junit.jupiter.api.Test;


class DefaultMethod {
    @Test
    void test() {
        MyClass myClass = new MyClass();
        myClass.testV2();

        // FunctionalInterfaceTest.java 파일을 좀 더 보자!
    }
}

interface MyInterface {
    void test();

    default void testV2() {
        test();
        //
        System.out.println("testv2");
    }
}

class MyClass implements MyInterface {
    @Override
    public void test() {

    }
}

