package com.example.modernjavainaction.day5;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@BenchmarkMode(Mode.AverageTime)
@Fork(1)
@Threads(1)
@Measurement(iterations = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ParallelStreamTest {
    public static int N = 10_000;


    /**
     Benchmark                             Mode  Cnt  Score   Error  Units
     ParallelStreamTest.forLoopSum         avgt       0.003          ms/op
     ParallelStreamTest.parallelRangedSum  avgt       0.064          ms/op
     ParallelStreamTest.parallelSum        avgt       0.255          ms/op
     ParallelStreamTest.rangedSum          avgt       0.010          ms/op
     ParallelStreamTest.sequentialSum      avgt       0.122          ms/op

     * 박싱을 주의하자. (되도록 특화된 스트림을 사용하자.)
     * 순차 스트림보다 병렬스트림에서 성능이 떨어지는 연산이있다.
     *  (limit 이나 findFirst 처럼 요소의 순서에 의존하는 연산을 병렬 스트림에서 수행하려면 비싼 비용을 치뤄야한다.)
     * 스트림에서 수행하는 전체 파이프라인 연산 비용을 고려하라.
     * 소량의 데이터에서는 병렬 스트림이 도움 되지 않는다.
     * **/

    @TearDown(Level.Invocation)
    public void name() {
        System.gc();
    }

    // 반복 결과로 박싱된 객체가 만들어지므로 숫자를 더하려면 언 박싱을 해야한다.
    // 반복 작업은 병렬로 수행할 수 있는 있는 독립 단위로 나누기가 어렵다.
    @Benchmark
    public long parallelSum(){
        return Stream.iterate(1L, i -> i + 1)
                .limit(N)
                .parallel()
                .reduce(0L, Long::sum);
    }

    @Benchmark
    public long sequentialSum(){
        return Stream.iterate(1L, i -> i + 1)
                .limit(N)
                .reduce(0L, Long::sum);
    }

    @Benchmark
    public long forLoopSum(){
        long res = 0L;

        for (long i = 0L; i <= N; i++) {
            res += i;
        }

        return res;
    }

    @Benchmark
    public long rangedSum(){
        return LongStream.rangeClosed(0, N)
                .reduce(0L, Long::sum);
    }

    @Benchmark
    public long parallelRangedSum(){
        return LongStream.rangeClosed(0, N)
                .parallel()
                .reduce(0L, Long::sum);
    }

}
