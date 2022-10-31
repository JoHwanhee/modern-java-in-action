package com.example.modernjavainaction.day7;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionTest {


}


// 런 타임에 알고리즘을 변경~
interface ValidateStrategy {
    boolean execute(String s);
}

class IsLowerCase implements ValidateStrategy {

    @Override
    public boolean execute(String s) {
        return false;
    }
}

class IsNumeric implements  ValidateStrategy {

    @Override
    public boolean execute(String s) {
        return false;
    }
}

class Validator {
    private final ValidateStrategy strategy;

    public Validator(ValidateStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean validate(String s) {
        return strategy.execute(s);
    }
}

class StrategyTest {
    @Test
    void name() {
        new Validator(new IsLowerCase());

        // 이렇게 리팩터링 된다~
        new Validator(s -> false);
    }
}


class Customer {

}

class Db {
    public Customer getCustomer(int id) {
        return new Customer();
    }
}

// 템플릿 메서드
// 알고리즘의 개요를 소개한 후 다음 알고리즘의 일부를 고칠 수 있는 유연함을 제공할때~
abstract class OnlineBanking {
    public void processCustomer(int id) {
        Customer customer = new Db().getCustomer(id);
        makeCustomerHappy(customer);
    }

    abstract void makeCustomerHappy(Customer customer);
}


// 리팩터링
class OnlineBankingLambda {
    public void processCustomer(int id, Consumer<Customer> makeCustomerHappy) {
        var customer = new Db().getCustomer(id);
        makeCustomerHappy.accept(customer);
    }
}

class TemplateMethodTest {

    @Test
    void name() {

        new OnlineBankingLambda().processCustomer(1, customer -> {

        });
    }
}


// 옵저버 패턴
// UI에 주로 쓰임
interface Observer {
    void notify(String tweet);
}

class NYTimes implements Observer {

    @Override
    public void notify(String tweet) {
        System.out.println(tweet);
    }
}

interface Subject {
    void register(Observer observer);

    void notify(String tweet);
}

class Feed implements Subject {
    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void register(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void notify(String tweet) {
        observers.forEach(o -> o.notify(tweet));
    }
}

class ObserverTest {
    @Test
    void name() {
        Feed f = new Feed();

        f.register(new NYTimes());
        f.register(tweet -> { });
        f.notify("hello~");
    }
}


// 의무체인
// 작업 처리 객체의 체인을 만들 때 의무 체인을 많이 씀, 한 객체가 뭐 한다음에 다른 개ㅔㄱ체로 전달 전달 전달~

abstract class ProcessingObject<T> {
    protected ProcessingObject<T> successor;

    public void setSuccessor(ProcessingObject<T> successor) {
        this.successor = successor;
    }

    public T handle(T input) {
        T r = handleWork(input);
        if (!Objects.isNull(successor)) {
            return successor.handle(r);
        }

        return r;
    }

    abstract protected T handleWork(T input);
}

class HeaderTextProcessing extends ProcessingObject<String> {

    @Override
    protected String handleWork(String input) {
        return "hello" + input;
    }
}

class ProcessingObjectTest {
    @Test
    void name() {
        ProcessingObject<String> p1 = new HeaderTextProcessing();
        ProcessingObject<String> p2 = new HeaderTextProcessing();
        p1.setSuccessor(p2);
        System.out.println(p1.handle("world"));

        // 리팩터링
        UnaryOperator<String> header = (s) -> "hello" + s;
        UnaryOperator<String> header2 = (s) -> "hello" + s;
        Function<String, String> pipe = header.andThen(header2);
        System.out.println(pipe.apply("world"));


        assertThat(p1.handle("world")).isEqualTo(pipe.apply("world"));
    }
}

class Product {

}

// 팩토리
class ProductFactory {
    public static Product createProduct(String name) {
        switch (name) {
            case "hello":
                return new Product();
        }

        return new Product();
    }
}

class FactoryTest {


    @Test
    void name() {
        ProductFactory.createProduct("aa");

        // 리팩터링
        Supplier<Product> loan = Product::new;
        loan.get();

        map.get("hello");
    }

    final static Map<String, Supplier<Product>> map = new HashMap<>();
    static {
        map.put("helo", Product::new);
        map.put("world", Product::new);
    }

}