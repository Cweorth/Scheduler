package cz.muni.fi.scheduler.utils;

import java.io.IOException;

@FunctionalInterface
public interface IOFunction<T,R> {
    R apply(T value) throws IOException;
}
