package cz.muni.fi.scheduler.utils;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import java.util.Objects;

public class Range<T extends Number & Comparable> {

    private final T min;
    private final T max;

    public Range(T min, T max) {
        this.min = requireNonNull(min, "min");
        this.max = requireNonNull(max, "max");

        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Invalid range boundaries.");
        }
    }

    public boolean contains(T value) {
        requireNonNull(value, "value");

        return ((min.compareTo(value) <= 0) && (value.compareTo(max) <= 0));
    }

    public boolean overlaps(Range<T> other) {
        requireNonNull(other, "other");

        return contains(other.min) || contains(other.max) || isSubsetOf(other);
    }

    public boolean isSupersetOf(Range<T> other) {
        requireNonNull(other, "other");

        return contains(other.min) && contains(other.max);
    }

    public boolean isSubsetOf(Range<T> other) {
        return requireNonNull(other, "other").isSupersetOf(this);
    }

    public Range<T> join(Range<T> other) {
        if (!overlaps(other)) {
            throw new IllegalArgumentException("Cannot join these ranges.");
        }

        T newMin = (min.compareTo(other.min) < 0) ? min : other.min;
        T newMax = (max.compareTo(other.max) > 0) ? max : other.max;

        return new Range<>(newMin, newMax);
    }

    public T getMin() { return min; }
    public T getMax() { return max; }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Range) || (obj.hashCode() != hashCode()))
            return false;

        final Range<?> other = (Range<?>) obj;

        return Objects.equals(min, other.min)
            && Objects.equals(max, other.max);
    }

    @Override
    public String toString() {
        return "[" + min + " -- " + max + "]";
    }
}
