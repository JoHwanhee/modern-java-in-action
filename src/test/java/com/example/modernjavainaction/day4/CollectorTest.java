package com.example.modernjavainaction.day4;

import com.example.modernjavainaction.models.Dish;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class CollectorTest {
    public enum CaloricLevel { DIET, NORMAL, FAT}

    @Test
    void groupingByTest() {
        Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = Stream.of(new Dish(), new Dish())
                .collect(groupingBy(dish -> {
                    if (dish.getCalories() > 0) return CaloricLevel.DIET;
                    return CaloricLevel.FAT;
                })) ;

        System.out.println(dishesByCaloricLevel);

    }

    @Test
    void filtering() {
        Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = Stream.of(new Dish(), new Dish())
                .collect(groupingBy(Dish::getLevel, Collectors.filtering(dish -> dish.getCalories() > 100, toList()))) ;

        System.out.println(dishesByCaloricLevel);
    }

    @Test
    void myToList() {
         List<Integer> dishes = Stream.of(new Dish(), new Dish())
                 .map(Dish::getCalories)
                 .filter(calories -> calories > 0)
                 .collect(new MyToListCollector<>());

        System.out.println(dishes);
    }
}

// T 는 수집될 항목쓰
// A 는 주적자
// R는 결과 R -> 대개 컬렉션 형식으로 함~
class MyToListCollector<T> implements Collector<T, List<T>, List<T>> {

    // 새로운 결과 컨테이너 만들기
    // 빈 결과로 이뤄진 서플라이어를 반환해야함!!!!!!
    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new; // 수집 연산의 시발점
    }

    // supplier -> accumulator (중간연산) -> ?? ->  finisher

    // 리듀싱 연산을 수행하는 함수~
    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add; // 탐색항목을 누적한다
    }

    // 누적자 객체를 최종 결과로 반환하면서 누적 과정을 끝낼 때 호출할 함수를 반환한다.
    // 때로는 ToList처럼 누적하는 과정이 결과인 경우가 있다.
    // 이때는 변환과정이 노필요하니까ㅏ, 항등함수를 반환한다.
    @Override
    public Function<List<T>, List<T>> finisher() {
        return Function.identity(); // 항등 함수 (t->t)
    }


    // 병렬ㄹ로 처리할때 필요한 것
    @Override
    public BinaryOperator<List<T>> combiner() {
        return (list1, list2) -> {
            list1.addAll(list2);
            return list1; // 두개를 그냥 합쳐서 첫번째껄 반환한당
        };
    }

    // 이 컬렉터를 처리할 때 힌트를준다. (병렬로 할지말지, 병렬로한다면 어케 최적화할지)
    // Characteristics.CONCURRENT
    //    다중 스레드에서 accumulator 함수를 동시에 호출할 수 있으며, 이 컬렉터는 스트림의 병렬 리듀싱을 수행할 수 있다!
    // Characteristics.UNORDERED
    //    리듀싱 결과는 방문 순서나 누적 순서에 영향을 받지 않는다.
    // Characteristics.IDENTITY_FINISH
    //      뭐라는지 모르겠다 ㅎ
    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(
                EnumSet.of(Characteristics.IDENTITY_FINISH, Characteristics.CONCURRENT, Characteristics.UNORDERED)
        );
    }
}