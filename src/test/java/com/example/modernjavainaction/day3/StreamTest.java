package com.example.modernjavainaction.day3;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Dish {
    int value;

    public Dish(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    boolean isVegetarian() {
        return true;
    }
}

public class StreamTest {
    @Test
    void filter() {
        List<Dish> dishes = new ArrayList<>();
        List<Dish> vegetarians = dishes.stream()
                .filter(Dish::isVegetarian)
                .collect(Collectors.toList());
    }

    @Test
    void distinct() {
        List<Integer> integers = Arrays.asList(1, 2, 2, 2, 43, 4, 5, 6);
        integers.stream()
                .filter(i -> i % 2 == 0)
                .distinct()
                .forEach(System.out::println);
    }

    @Test
    void takewhile() {
        // takeWhile 은 predicated가 일치하게 되면 멈춘다.
        List<Integer> integers = Arrays.asList(1, 2, 2, 2, 43, 4, 5, 6);
        integers.stream()
                .takeWhile(i -> i < 10)
                .forEach(System.out::println);
    }

    @Test
    void dropWhile() {
        // dropWhile 은 predicated가 처음으로 일치하지 않는 요소까지 버린다..
        List<Integer> integers = Arrays.asList(1, 2, 2, 2, 43, 4, 5, 6);
        integers.stream()
                .dropWhile(i -> i < 10)
                .forEach(System.out::println);
    }

    @Test
    void limit() {
        List<Integer> integers = Arrays.asList(1, 2, 2, 2, 43, 4, 5, 6);
        integers.stream()
                .dropWhile(i -> i < 10)
                .limit(1)
                .forEach(System.out::println);
    }

    @Test
    void skip() {
        List<Integer> integers = Arrays.asList(1, 2, 2, 2, 43, 4, 5, 6);
        integers.stream()
                .dropWhile(i -> i < 10)
                .skip(1)
                .forEach(System.out::println);
    }

    @Test
    void flatMap() {
        // 평면화된 새로운 스트림에 컨텐츠를 매핑한다.
        List<String> words = Arrays.asList("hello", "world!");
        List<String> characters = words.stream()
                .map(word -> word.split(""))
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        assertThat(characters.size()).isEqualTo(5 + 6);
    }

    @Test
    void flatMap2() {
        List<Integer> arr1 = Arrays.asList(1, 2, 3);
        List<Integer> arr2 = Arrays.asList(3, 4);

        List<int[]> res = arr1.stream()
                .flatMap(i -> arr2.stream().map(j -> new int[]{i, j}))
                .collect(Collectors.toList());
    }

    @Test
    void anyMatch() {
        // 적어도 한 요소와 일치하는지?, 쇼트 서킷 기법이다
        List<Integer> arr1 = Arrays.asList(1, 2, 3);
        boolean res = arr1.stream()
                .anyMatch(i -> i == 3);

        assertThat(res).isTrue();
    }

    @Test
    void allMatch() {
        // 모든 요소와 일치하는지?, 쇼트 서킷 기법이다
        List<Integer> arr1 = Arrays.asList(1, 2, 3);
        boolean res = arr1.stream()
                .allMatch(i -> i == 3);

        assertThat(res).isFalse();
    }

    @Test
    void noneMatch() {
        // 모든 요소와 일치하지 않는지, 쇼트 서킷 기법이다
        List<Integer> arr1 = Arrays.asList(1, 2, 3);
        boolean res = arr1.stream()
                .noneMatch(i -> i == 100);

        assertThat(res).isTrue();
    }

    @Test
    void findAny() {
        // 일치하는 아무거나 준다, 쇼트 서킷 기법이다
        // 다음 연산은 병렬 연산을 돌리게 되면, 1이 안 나올수도 있다.
        // 병렬연산일때 순서가 중요하지 않으면 findAny로 하자. (더 빠름)
        int res = IntStream.range(0, 99999)
                .parallel()
                .filter(i -> i > 0)
                .findAny()
                .getAsInt();

        assertThat(res).isNotEqualTo(1);
    }

    @Test
    void findFirst() {
        // 일치하는 것 중 첫번째거를 보장한다., 쇼트 서킷 기법이다
        int res = IntStream.range(0, 99999)
                .parallel()
                .filter(i -> i > 0)
                .findFirst()
                .getAsInt();

        assertThat(res).isEqualTo(1);
    }

    @Test
    void reduce() {
        int res = IntStream.range(0, 10)
                .parallel()
                .reduce(1, (p, a) -> p + a);
        assertThat(res).isEqualTo(55);
    }

    @Test
    void reduce2() {
        // 초기값이 없으면, Optional을 반환
        int res = IntStream.range(0, 10)
                .parallel()
                .reduce(Integer::max)
                .getAsInt();

        assertThat(res).isEqualTo(9);
    }

    @Test
    void mapToInt() {
        List<Dish> dishes = new ArrayList<>();
        dishes.add(new Dish(10));
        dishes.add(new Dish(9));
        dishes.add(new Dish(88));
        dishes.add(new Dish(1));
        dishes.add(new Dish(1));
        dishes.add(new Dish(1));

        // value 가 2보다 작은 것들을 intstream 으로 변환하여 intstream의 sum을 사용
        int res = dishes.stream()
                .parallel()
                .filter(dish -> dish.value < 2)
                .mapToInt(Dish::getValue)
                .sum();

        assertThat(res).isEqualTo(3);
    }

    @Test
    void boxed() {
        List<Dish> dishes = new ArrayList<>();
        dishes.add(new Dish(10));
        dishes.add(new Dish(9));
        dishes.add(new Dish(88));
        dishes.add(new Dish(1));
        dishes.add(new Dish(1));
        dishes.add(new Dish(1));

        // 다시 boxed 하여 Stream의 함수를 사용ㅇ할 수 있다.
        List<Integer> res = dishes.stream()
                .parallel()
                .filter(dish -> dish.value < 2)
                .mapToInt(Dish::getValue)
                .boxed()
                .collect(Collectors.toList());

        assertThat(res.size()).isEqualTo(3);
    }

    @Test
    void streamOf() {
        // 그냥 바로 stream 만들기~ 이거 좋은듯
        List<Integer> res = Stream.of(1, 2, 3, 4, 5)
                .parallel()
                .filter(i -> i < 2)
                .collect(Collectors.toList());

        assertThat(res.size()).isEqualTo(1);
    }

    @Test
    void iterate() {
        // 무한 스트림 만들기
        // 이거 진짜 무한이다. 안 끝남~
        List<Integer> res = Stream.iterate(0, (n) -> n + 1)
                .parallel()
                .filter(i -> i < 2)
                .collect(Collectors.toList());

        assertThat(res.size()).isEqualTo(1);
    }

    @Test
    void iterateLimit() {
        // 무한 스트림 만들기
        // 그래서 Limit을 걸어준다.
        int res = Stream.iterate(0, (n) -> n + 1)
                .parallel()
                .limit(10)
                .mapToInt(i -> i)
                .sum();

        // 0 ~ 9 까지 sum 하면 45다
        assertThat(res).isEqualTo(45);
    }


    int getZero() {
        return 0;
    }

    void test() {
        int result = getZero();
    }
}
