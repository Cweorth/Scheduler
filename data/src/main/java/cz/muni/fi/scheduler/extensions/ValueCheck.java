package cz.muni.fi.scheduler.extensions;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Provides value assertions similar to {@link Objects#requireNonNull}.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class ValueCheck {

    /**
     * Plain Old Data entity that holds conditions for {@code compareTo} method result.
     */
    private static class CmpZeroPOD {
        public final Function<Integer, Boolean> fun; // comparare with zero
        public final String name;                    // comparison condition name

        public CmpZeroPOD(Function<Integer, Boolean> fun, String name) {
            this.fun  = fun;
            this.name = name;
        }
    }

    private static final CmpZeroPOD[] cmps = {
    /*0*/   new CmpZeroPOD(x -> x <  0, "negative"    ),
    /*1*/   new CmpZeroPOD(x -> x <= 0, "non-positive"),
    /*2*/   new CmpZeroPOD(x -> x == 0, "zero"        ),
    /*3*/   new CmpZeroPOD(x -> x != 0, "non-zero"    ),
    /*4*/   new CmpZeroPOD(x -> x >= 0, "non-negative"),
    /*5*/   new CmpZeroPOD(x -> x >  0, "positive"    )
    };

    private static <T extends Number & Comparable<T>> T checkNumber(T n, T z, CmpZeroPOD cmp, String attrName) {
        if (!cmp.fun.apply(n.compareTo(z)))
            throw new IllegalArgumentException(attrName + " must be " + cmp.name + '.');

        return n;
    }

    public static long requireNonNegative(long n, String attrName) {
        return checkNumber(n, 0L, cmps[4], attrName);
    }

    public static int requireNonNegative(int n, String attrName) {
        return checkNumber(n, 0,  cmps[4], attrName);
    }

    public static long requirePositive(long n, String attrName) {
        return checkNumber(n, 0L, cmps[5], attrName);
    }

    public static int requirePositive(int n, String attrName) {
        return checkNumber(n, 0,  cmps[5], attrName);
    }

    public static <T> T requireNonNull(T obj, String attrName) {
        return Objects.requireNonNull(obj, attrName + " is null.");
    }

    public static String requireMatch(Pattern pattern, String value, String attrName) {
        if ((pattern != null) && !pattern.matcher(value).matches())
            throw new IllegalArgumentException(attrName + " with value \"" + value + "\" does not match " + pattern);

        return value;
    }

    public static String requireMatch(String pattern, String value, String attrName) {
        return requireMatch(Pattern.compile(pattern), value, attrName);
    }
}
