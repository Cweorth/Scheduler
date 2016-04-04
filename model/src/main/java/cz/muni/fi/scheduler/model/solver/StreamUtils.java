package cz.muni.fi.scheduler.model.solver;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class StreamUtils {
    public static <Q,LT,RT> Stream<Q> zipWith(Stream<LT> left, Stream<RT> right, BiFunction<LT, RT, Q> function) {
        Iterable<Q> iterable = () -> new Iterator<Q>() {
            private final Iterator<LT> li = left.iterator();
            private final Iterator<RT> ri = right.iterator();

            @Override
            public boolean hasNext() { return li.hasNext() && ri.hasNext(); }
            @Override
            public Q       next()    { return function.apply(li.next(), ri.next()); }
        };

        return StreamSupport.stream(iterable.spliterator(), true);
    }
}
