package cz.muni.fi.scheduler.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class PairNGTest {

    @Test
    public void testOf() {
        String       ll = "Left-most node";
        Integer      ml = 42;
        List<Double> mr = Arrays.asList(2.71, 3.14);
        Object       rr = this;

        Pair<
                Pair<String,Integer>,
                Pair<List<Double>,Object>
            > pair = Pair.of(Pair.of(ll, ml), Pair.of(mr, rr));

        assertEquals(pair.first().first(),   ll);
        assertEquals(pair.first().second(),  ml);
        assertEquals(pair.second().first(),  mr);
        assertEquals(pair.second().second(), rr);
    }

    /**
     * Test of hashCode method, of class Pair.
     */
    @Test
    public void testHashCode() {
        long hashes =
            Stream.of(Pair.of(0, 0), Pair.of(0, 1), Pair.of(1, 0), Pair.of(1, 1))
                .mapToInt(Pair::hashCode)
                .distinct()
                .count();

        assertEquals(hashes, 4L);
    }

    /**
     * Test of equals method, of class Pair.
     */
    @Test
    public void testEquals() {
        List<Pair<Integer,Integer>> pairs = new ArrayList<>(10);

        for (int l = -1; l <= 1; ++l) {
            for (int r = -1; r <= 1; ++r) {
                pairs.add(Pair.of(l, r));
            }
        }

        for (int a = 0; a < pairs.size(); ++a) {
            for (int b = 0; b < pairs.size(); ++b) {
                Pair x = pairs.get(a);
                Pair y = pairs.get(b);

                boolean expected = (Objects.equals(x.first(), y.first()) && Objects.equals(x.second(), y.second()));

                if (expected) {
                    assertEquals(x, y);
                } else {
                    assertNotEquals(x, y);
                }
            }
        }
    }

}
