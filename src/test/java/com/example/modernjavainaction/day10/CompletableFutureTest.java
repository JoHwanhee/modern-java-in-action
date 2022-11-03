package com.example.modernjavainaction.day10;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** 최상은 좋은 것의 적이다. **/


public class CompletableFutureTest {

    @Test
    void name() {
        long start = System.nanoTime();
        Future<Double> futurePrice = getPriceAsync2("12");
        long invokeTime = getInvokeTime(start);

        System.out.println(invokeTime + " msecs sdasfdasd");

        doSomethingElse();

        try {
            double price = futurePrice.get();

            System.out.printf("price : %.2f%n", price);

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        long retriedTime = getInvokeTime(start);
        System.out.println(retriedTime + " msecs");
    }

    private long getInvokeTime(long start) {
        return ((System.nanoTime()) - start) / 1_000_000;
    }

    private void doSomethingElse() {

    }

    public Future<Double> getPriceAsync(String product) {
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        new Thread(() -> {

            try {
                double price = calculatePrice(product);
                futurePrice.complete(price);
            } catch (Exception e) {
                futurePrice.completeExceptionally(e);
            }

        }).start();

        return futurePrice;
    }

    public static void delay() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private double calculatePrice(String product) {
        delay();
        return new Random().nextDouble() * product.charAt(0) + product.charAt(1);
    }

    ///after

    public Future<Double> getPriceAsync2(String product) {
        return CompletableFuture.supplyAsync(() -> calculatePrice(product));
    }
}
