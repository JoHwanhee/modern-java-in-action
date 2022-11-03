package com.example.modernjavainaction.day11;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 최상은 좋은 것의 적이다.
 **/


public class CompletableFutureTest2 {

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
            Thread.sleep(1000);
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


    @Test
    void name2() {
        long start = System.nanoTime();

        System.out.println(findPrices("asdf"));

        long invokeTime = getInvokeTime(start);

        System.out.println(invokeTime + " msecs");

        doSomethingElse();

        long retriedTime = getInvokeTime(start);
        System.out.println(retriedTime + " msecs");

    }

    public List<String> findPrices(String product) {
        var shops = Arrays.asList(
                new Shop("bestPrice", 100),
                new Shop("lests", 202),
                new Shop("fav", 130),
                new Shop("buy", 140)
        );

        return shops.stream()
                .map(shop -> String.format("%s price is %.2f", shop.getName(), shop.getPrice()))
                .collect(Collectors.toList());
    }


    @Test
    void name3() {
        long start = System.nanoTime();

        System.out.println(findPricesP("asdf"));

        long invokeTime = getInvokeTime(start);

        System.out.println(invokeTime + " msecs");

        doSomethingElse();

        long retriedTime = getInvokeTime(start);
        System.out.println(retriedTime + " msecs");

    }

    public List<String> findPricesP(String product) {
        var shops = Arrays.asList(
                new Shop("bestPrice", 100),
                new Shop("lests", 202),
                new Shop("fav", 130),
                new Shop("buy", 140)
        );

        return shops.stream().parallel()
                .map(shop -> String.format("%s price is %.2f", shop.getName(), shop.getPrice()))
                .collect(Collectors.toList());
    }


    @Test
    void name4() {
        long start = System.nanoTime();

        System.out.println(findPricesC("asdf"));

        long invokeTime = getInvokeTime(start);

        System.out.println(invokeTime + " msecs");

        doSomethingElse();

        long retriedTime = getInvokeTime(start);
        System.out.println(retriedTime + " msecs");
    }

    public List<String> findPricesC(String product) {
        var shops = Arrays.asList(
                new Shop("bestPrice", 100),
                new Shop("lests", 202),
                new Shop("fav", 130),
                new Shop("buy", 140)
        );

        var res = shops.stream()
                .map(shop ->
                        CompletableFuture.supplyAsync(() ->
                                String.format("%s price is %.2f", shop.getName(), shop.getPrice())
                        )
                )
                .collect(Collectors.toList());

        return res.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }
}
