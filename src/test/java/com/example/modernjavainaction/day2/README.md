# 스트림이란 정확히 뭘까?
### 컬렉션은 소모된다!!!!

컬렉션과 스트림의 차이
- 일단 둘다 연속된 요소의 데이터를 저장 및 처리하는 자료구조임
  - 컬렉션 : compile 시점과 비슷한 개념임, 한번에 모든 데이터를 메모리에 올린다.
  - 스트림 : jit 시점과 비슷한 개념임, 요청시에 필요한 데이터만 메모리에 올린다.
```
스트림은 시간적으로 흩어진 값의 집합으로 간주할 수 있다.
컬렉션은 특정시간에 모든 것이 존재하는 공간에 흩어진 값으로 비유할 수 있다.
```

책에 스트림은 비싼 연산이다.라는 말이 나온다. 왜 그럴까? 스트림은 어떻게 만들어질까? 궁금해서 java를 찾아봤다.

```
Collection -> StreamSupport.stream() 
                -> IteratorSpliterator : 병렬이면, 여기서 병렬 스플릿터를 쓴다.
                -> ReferencePipeline.Head() -> 각 연산마다 StreamSupport.stream()를 생성한다. 
```
```java
interface Collection {
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, 0);
    }

    static <T> Spliterator<T> spliterator(Collection<? extends T> c,
                                                 int characteristics) {
        return new IteratorSpliterator<>(Objects.requireNonNull(c),
                characteristics);
    }
}

public final class StreamSupport {
    public static <T> Stream<T> stream(Spliterator<T> spliterator, boolean parallel) {
        Objects.requireNonNull(spliterator); // << null 체크는 이렇게 하쟈 이제
        return new ReferencePipeline.Head<>(spliterator,
                StreamOpFlag.fromCharacteristics(spliterator),
                parallel);
    }
    
}

abstract class ReferencePipeline<P_IN, P_OUT>
        extends AbstractPipeline<P_IN, P_OUT, Stream<P_OUT>>
        implements Stream<P_OUT>  {

    static class Head<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
    }
    
  public final Stream<P_OUT> filter(Predicate<? super P_OUT> predicate) {
    Objects.requireNonNull(predicate);
    return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE,
            StreamOpFlag.NOT_SIZED) {
      @Override
      Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
        return new Sink.ChainedReference<P_OUT, P_OUT>(sink) {
          @Override
          public void begin(long size) {
            downstream.begin(-1);
          }

          @Override
          public void accept(P_OUT u) {
            if (predicate.test(u))
              downstream.accept(u);
          }
        };
      }
    };
  }


  @Override
  @SuppressWarnings("unchecked")
  public final <R> Stream<R> map(Function<? super P_OUT, ? extends R> mapper) {
    Objects.requireNonNull(mapper);
    return new StatelessOp<P_OUT, R>(this, StreamShape.REFERENCE,
            StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
      @Override
      Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
        return new Sink.ChainedReference<P_OUT, R>(sink) {
          @Override
          public void accept(P_OUT u) {
            downstream.accept(mapper.apply(u));
          }
        };
      }
    };
  }
  
  @SuppressWarnings("unchecked")
  public final <R, A> R collect(Collector<? super P_OUT, A, R> collector) {
    A container;
    if (isParallel()
            && (collector.characteristics().contains(Collector.Characteristics.CONCURRENT))
            && (!isOrdered() || collector.characteristics().contains(Collector.Characteristics.UNORDERED))) {
      container = collector.supplier().get();
      BiConsumer<A, ? super P_OUT> accumulator = collector.accumulator();
      forEach(u -> accumulator.accept(container, u));
    }
    else {
      container = evaluate(ReduceOps.makeRef(collector));
    }
    return collector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)
            ? (R) container
            : collector.finisher().apply(container);
  }
  
  @Override
  public final <R> R reduce(R identity, BiFunction<R, ? super P_OUT, R> accumulator, BinaryOperator<R> combiner) {
    return evaluate(ReduceOps.makeRef(identity, accumulator, combiner));
  }


  @Override
  public final boolean anyMatch(Predicate<? super P_OUT> predicate) {
    return evaluate(MatchOps.makeRef(predicate, MatchOps.MatchKind.ANY));
  }
    

  final <R> R evaluate(TerminalOp<E_OUT, R> terminalOp) {
    assert getOutputShape() == terminalOp.inputShape();
    if (linkedOrConsumed)
      throw new IllegalStateException(MSG_STREAM_LINKED);
    linkedOrConsumed = true;

    return isParallel()
            ? terminalOp.evaluateParallel(this, sourceSpliterator(terminalOp.getOpFlags()))
            : terminalOp.evaluateSequential(this, sourceSpliterator(terminalOp.getOpFlags()));
  }
}



public interface Stream<T> extends BaseStream<T, Stream<T>> {
    Stream<T> filter(Predicate<? super T> predicate);
    <R> Stream<R> map(Function<? super T, ? extends R> mapper);
    IntStream mapToInt(ToIntFunction<? super T> mapper);
    LongStream mapToLong(ToLongFunction<? super T> mapper);
    DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);
    <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);
    IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper);
    LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper);
    DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper);
    default <R> Stream<R> mapMulti(BiConsumer<? super T, ? super Consumer<R>> mapper) {
        Objects.requireNonNull(mapper);
        return flatMap(e -> {
            SpinedBuffer<R> buffer = new SpinedBuffer<>();
            mapper.accept(e, buffer);
            return StreamSupport.stream(buffer.spliterator(), false);
        });
    }
    default IntStream mapMultiToInt(BiConsumer<? super T, ? super IntConsumer> mapper) {
        Objects.requireNonNull(mapper);
        return flatMapToInt(e -> {
            SpinedBuffer.OfInt buffer = new SpinedBuffer.OfInt();
            mapper.accept(e, buffer);
            return StreamSupport.intStream(buffer.spliterator(), false);
        });
    }
    default LongStream mapMultiToLong(BiConsumer<? super T, ? super LongConsumer> mapper) {
        Objects.requireNonNull(mapper);
        return flatMapToLong(e -> {
            SpinedBuffer.OfLong buffer = new SpinedBuffer.OfLong();
            mapper.accept(e, buffer);
            return StreamSupport.longStream(buffer.spliterator(), false);
        });
    }
    default DoubleStream mapMultiToDouble(BiConsumer<? super T, ? super DoubleConsumer> mapper) {
        Objects.requireNonNull(mapper);
        return flatMapToDouble(e -> {
            SpinedBuffer.OfDouble buffer = new SpinedBuffer.OfDouble();
            mapper.accept(e, buffer);
            return StreamSupport.doubleStream(buffer.spliterator(), false);
        });
    }
    Stream<T> distinct();
    Stream<T> sorted();
    Stream<T> sorted(Comparator<? super T> comparator);
    Stream<T> peek(Consumer<? super T> action);
    Stream<T> limit(long maxSize);
    Stream<T> skip(long n);
    default Stream<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        // Reuses the unordered spliterator, which, when encounter is present,
        // is safe to use as long as it configured not to split
        return StreamSupport.stream(
                new WhileOps.UnorderedWhileSpliterator.OfRef.Taking<>(spliterator(), true, predicate),
                isParallel()).onClose(this::close);
    }
    default Stream<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        // Reuses the unordered spliterator, which, when encounter is present,
        // is safe to use as long as it configured not to split
        return StreamSupport.stream(
                new WhileOps.UnorderedWhileSpliterator.OfRef.Dropping<>(spliterator(), true, predicate),
                isParallel()).onClose(this::close);
    }
    void forEach(Consumer<? super T> action);
    void forEachOrdered(Consumer<? super T> action);
    Object[] toArray();
    <A> A[] toArray(IntFunction<A[]> generator);
    T reduce(T identity, BinaryOperator<T> accumulator);
    Optional<T> reduce(BinaryOperator<T> accumulator);
    <U> U reduce(U identity,
                 BiFunction<U, ? super T, U> accumulator,
                 BinaryOperator<U> combiner);
    <R> R collect(Supplier<R> supplier,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner);
    <R, A> R collect(Collector<? super T, A, R> collector);
    @SuppressWarnings("unchecked")
    default List<T> toList() {
        return (List<T>) Collections.unmodifiableList(new ArrayList<>(Arrays.asList(this.toArray())));
    }
    Optional<T> min(Comparator<? super T> comparator);
    Optional<T> max(Comparator<? super T> comparator);
    long count();
    boolean anyMatch(Predicate<? super T> predicate);
    boolean allMatch(Predicate<? super T> predicate);
    boolean noneMatch(Predicate<? super T> predicate);
    Optional<T> findFirst();
    Optional<T> findAny();
    public static<T> Builder<T> builder() {
        return new Streams.StreamBuilderImpl<>();
    }
    public static<T> Stream<T> empty() {
        return StreamSupport.stream(Spliterators.<T>emptySpliterator(), false);
    }
    public static<T> Stream<T> of(T t) {
        return StreamSupport.stream(new Streams.StreamBuilderImpl<>(t), false);
    }
    public static<T> Stream<T> ofNullable(T t) {
        return t == null ? Stream.empty()
                : StreamSupport.stream(new Streams.StreamBuilderImpl<>(t), false);
    }
    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a stream from an array is safe
    public static<T> Stream<T> of(T... values) {
        return Arrays.stream(values);
    }
    public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) {
        Objects.requireNonNull(f);
        Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE,
                Spliterator.ORDERED | Spliterator.IMMUTABLE) {
            T prev;
            boolean started;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                T t;
                if (started)
                    t = f.apply(prev);
                else {
                    t = seed;
                    started = true;
                }
                action.accept(prev = t);
                return true;
            }
        };
        return StreamSupport.stream(spliterator, false);
    }
    public static<T> Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE,
                Spliterator.ORDERED | Spliterator.IMMUTABLE) {
            T prev;
            boolean started, finished;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished)
                    return false;
                T t;
                if (started)
                    t = next.apply(prev);
                else {
                    t = seed;
                    started = true;
                }
                if (!hasNext.test(t)) {
                    prev = null;
                    finished = true;
                    return false;
                }
                action.accept(prev = t);
                return true;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished)
                    return;
                finished = true;
                T t = started ? next.apply(prev) : seed;
                prev = null;
                while (hasNext.test(t)) {
                    action.accept(t);
                    t = next.apply(t);
                }
            }
        };
        return StreamSupport.stream(spliterator, false);
    }

    public static<T> Stream<T> generate(Supplier<? extends T> s) {
        Objects.requireNonNull(s);
        return StreamSupport.stream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfRef<>(Long.MAX_VALUE, s), false);
    }
    public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        @SuppressWarnings("unchecked")
        Spliterator<T> split = new Streams.ConcatSpliterator.OfRef<>(
                (Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator());
        Stream<T> stream = StreamSupport.stream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }

    public interface Builder<T> extends Consumer<T> {
        @Override
        void accept(T t);
        default Builder<T> add(T t) {
            accept(t);
            return this;
        }
        Stream<T> build();

    }
    
}

public interface BaseStream<T, S extends BaseStream<T, S>>
        extends AutoCloseable {
    Iterator<T> iterator();
    Spliterator<T> spliterator();
    boolean isParallel();
    S sequential();
    S parallel();
    S unordered();
    @Override
    void close();
}
```