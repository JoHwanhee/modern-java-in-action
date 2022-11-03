package com.example.modernjavainaction.day8;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class OptionalTest {
    class Insurance {
        String name;

        public String getName() {
            return name;
        }

        public Optional<String> optName() {
            return Optional.ofNullable(name);
        }
    }

    class Car {
        Insurance insurance;

        public Insurance getInsurance() {
            return insurance;
        }

        public Optional<Insurance> optInsurance() {
            return Optional.ofNullable(insurance);
        }
    }

    class Person {
        Car car;

        public Car getCar() {
            return car;
        }

        public Optional<Car> optCar() {
            return Optional.ofNullable(car);
        }
    }



    @Test
    void name() {
        // before
        Insurance insurance = new Insurance();
        String name = null;
        if (insurance != null) {
            name = insurance.getName();
        }

        // after
        var optInsurance = Optional.ofNullable(insurance);
        var optName = optInsurance.map(Insurance::getName);
    }

    private String getCarInsuranceName(Optional<Person> person) {
        return person.map(Person::getCar)
                .map(Car::getInsurance)
                .map(Insurance::getName)
                .orElse("unknown");
    }

    @Test
    void name2() {
        Person p = new Person();


        String name = getCarInsuranceName(Optional.of(p));


        assertThat(name).isEqualTo("unknown");
    }

    // 근데 만약에 함수 내부에서 Optional<>을 반환한다면?

    private String getCarInsuranceName2(Optional<Person> person) {
        return person.flatMap(Person::optCar)
                .flatMap(Car::optInsurance)
                .flatMap(Insurance::optName)
                .orElse("unknown");
    }

    @Test
    void name3() {
        Person p = new Person();


        String name = getCarInsuranceName2(Optional.of(p));


        assertThat(name).isEqualTo("unknown");
    }


    // 재밌는 사실
    // Optional은 직렬화가 안 됨~
    // 그래서 프로퍼티는 원본 값을 쓰고
    // get할때 Optional<> 을 하는 방식 추천
    class Person22 {
        Car car;
        Optional<Car> optCar;

        public Car getCar() {
            return car;
        }

        public Optional<Car> optCar() {
            return Optional.ofNullable(car);
        }
    }


    @Test
    void name4() {
        var p1 = new Person();
        var p2 = new Person();


        var names = getCarInsuranceNames(p1, p2);


        assertThat(names.stream().allMatch("unknown"::equals)).isTrue();
    }

    private Set<String> getCarInsuranceNames(Person ... people) {
        return Stream.of(people)
                .map(Person::optCar)
                .map(optCar -> optCar.flatMap(Car::optInsurance))
                .map(optIns -> optIns.flatMap(Insurance::optName))
                .map(optStr -> optStr.orElse("unknown"))
                .collect(toSet());
    }
}
