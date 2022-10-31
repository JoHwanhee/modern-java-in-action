package com.example.modernjavainaction.day6;

import org.junit.jupiter.api.Test;

import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class RecursiveTest {
    /**
     * 대부분의 분할정복은 다음과 같은 구조를 지닌다.
     * if ( 태스크가 충분히 작거나 더 이상 분할할 수 없으면  )
     *    순차적으로 태스크 계산
     * else
     *    태스크를 두 서브 태스트코 분할
     *    태스크가 다시 서브 태스크로 분할되도록 이 메서드를 재귀적으로 호출함
     *    모든 서브 태스크의 연산이 완료될 때까지 기다림
     *    각 서브태스크의 결과를 합침
     * **/

    /**
     *         [     |    ]
     *              fork
     *     [     |    ]    [     |    ]
     *        fork            fork
     * [  ]   [  ]     [  ]   [  ]
     * <p>
     * (  )   (  )     (  )   (  )
     * join           join
     * (   )          (   )
     * join
     * (   )
     */
    public static class ForkJoinSumCalculator extends RecursiveTask<Long> {
        private final long[] numbers;
        private final int start;
        private final int end;
        public static final long THRESHOLD = 10_000;


        public ForkJoinSumCalculator(long[] numbers) {
            this(numbers, 0, numbers.length);
        }

        private ForkJoinSumCalculator(long[] numbers, int start, int end) {
            this.numbers = numbers;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                return computeSequentially();
            }

            ForkJoinSumCalculator left = new ForkJoinSumCalculator(numbers, start, start + length / 2);
            left.fork();

            ForkJoinSumCalculator right = new ForkJoinSumCalculator(numbers, start + length / 2, end);

            Long rightRes = right.compute();
            Long leftRes = left.join();

            return rightRes + leftRes;
        }

        private Long computeSequentially() {
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += numbers[i];
            }
            return sum;
        }
    }

    @Test
    void forkJoinSum() {
        long[] numbers = LongStream.rangeClosed(1, 1000000).toArray();
        ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
        Long res = new ForkJoinPool().invoke(task);

        assertThat(res).isEqualTo(500000500000L);
    }


    // 이것은 코어 개수와 상관없이 분할을 하는데,
    // ex) core 4개 / 쓰레드 1000개
    // 많은 사람들은 이게 문제라고 말한다.
    // 하지만 이것은 문제가 아니다.
    // 이론적으로는 코어 개수만큼 병렬화된 태스크로 분할하면 모든 코어cpu에서 태스크를 실행할 것이고 크기가 같은 각각의 태스크는 같은 시간에 종료될것이라
    // 생각될 수 있다.

    // 하지만 실뭉서는 각각의 서브태스크 작업 완료 시간이 달라질 수 있다.
    // 포크/조인 프레임워크에서는 '작업훔치기' 기법을 사용한다.
    // 스레드의 작업은 작업큐에 주구장창 쌓아두고
    // 한 쓰레드가 작업 끝나면, 그 스레드는 유휴로 돌리는게 아니라, 다른 쓰레드의 tail을 훔쳐온다.
    //
    // 스레드 풀에 있는 작업자 스레드의 태스크를 재분배하고 균형을 맞출 때 작업 훔치기 알고리즘을 사용한다.

    /**근데 이거보다 더 우아한 기법이 spliterator를 사용하는거시다..**/
    public interface Spliterator<T> {
        boolean tryAdvance(Consumer<? super T> action); // 요소를 하나씩 순차적으로 소비하면서 탐색할 요소가 있으면 참 반환
        Spliterator<T> trySplit(); // 분할!
        long estimateSize(); // 탐색해야할 요소의 정보를 제공
        int characteristics();
    }

    // 1. trySplit을 호출하면 두번째 spliterator가 생성됨
    // 2. 두개의 spliterator에서 trySplit을 호출하면 네개의 spliterator이 생성됨
    // 3. 이걸 trySplit 결과가 null 될때까지 반복

    public static class WorkCounterSpliterator implements Spliterator<Character> {
        private final String string;

        public WorkCounterSpliterator(String string) {
            this.string = string;
        }

        private int currentChar = 0;
        @Override
        public boolean tryAdvance(Consumer<? super Character> action) {
            return false;
        }

        @Override
        public Spliterator<Character> trySplit() {
            int currentSize = string.length() - currentChar;
            if (currentSize < 10) {
                return null;
            }

            for (int splitPos = currentSize / 2 + currentChar; splitPos < string.length(); splitPos++) {
                if (Character.isWhitespace(string.charAt(splitPos))) {
                    Spliterator<Character> spliterator =
                            new WorkCounterSpliterator(string.substring(currentChar, splitPos));
                    currentChar = splitPos;
                    return spliterator;
                }
            }

            return null;
        }

        @Override
        public long estimateSize() {
            return string.length() - currentChar;
        }

        @Override
        public int characteristics() {
            return 0;
        }
    }

    private int countWord(Stream<Character> stream) {
        WorkCounter workCounter = stream.reduce(
                new WorkCounter(0, true),
                WorkCounter::accumulate,
                WorkCounter::combine);
        return workCounter.getCounter();
    }

    @Test
    void testCount() {
        String word = "hello world!!";
        Stream<Character> characterStream = IntStream.range(0, word.length())
                .mapToObj(word::charAt);
        int cnt =  countWord(characterStream);

        assertThat(cnt).isEqualTo(2);
    }

    public static class WorkCounter {
        private final int counter;
        private final boolean lastSpace;

        public WorkCounter(int counter, boolean lastSpace) {
            this.counter = counter;
            this.lastSpace = lastSpace;
        }

        public WorkCounter accumulate(Character c) {
            if (Character.isWhitespace(c)) {
                return lastSpace ?
                        this :
                        new WorkCounter(counter, true);
            }else {
                return lastSpace ?
                        new WorkCounter(counter + 1, false) :
                        this;
            }
        }

        public WorkCounter combine(WorkCounter workCounter) {
            return new WorkCounter(counter + workCounter.counter, workCounter.lastSpace);
        }

        public int getCounter() {
            return counter;
        }
    }

}

