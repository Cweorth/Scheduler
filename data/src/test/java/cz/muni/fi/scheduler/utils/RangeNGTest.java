package cz.muni.fi.scheduler.utils;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class RangeNGTest {
    @Test
    public void testContains() {
        final int LO = -5;
        final int HI =  5;
        assert(LO <= HI);

        for (int l = LO; l <= HI; ++l) {
            for (int u = l; u <= HI; ++u) {
                Range r = new Range(l, u);

                for (int i = LO; i <= HI; ++i) {
                    boolean expected = (l <= i && i <= u);

                    assertEquals(r.contains(i), expected);
                }
            }
        }
    }

    @Test
    public void testOverlaps() {
        final int LO = -10;
        final int MI =   0;
        final int HI =  10;
        assert((LO <= MI) && (MI <= HI));

        Range rmid = new Range(MI, HI);
        for (int i = LO; i <= HI; ++i) {
            Range test = new Range(LO, i);

            boolean expected = (i >= MI);
            assertEquals(test.overlaps(rmid), expected);
            assertEquals(rmid.overlaps(test), expected);
        }
    }

    @Test
    public void testIsSupersetOf() {
        final int LO = -20;
        final int HI =  20;
        final int SZ =   6;
        final int RF =   5;
        assert(LO <= HI);
        assert(RF <= SZ);

        for (int i = 0; i < RF; ++i) {
            Range rnil = new Range(-i, i);

            for (int l = LO; l + SZ <= HI; ++l) {
                Range test = new Range(l, l + SZ);

                boolean expected = (l <= -i && i <= l + SZ);
                assertEquals(test.isSupersetOf(rnil), expected);
            }
        }
    }

    @Test
    public void testIsSubsetOf() {
        final int LO = -20;
        final int HI =  20;
        final int SZ =   2;
        final int RF =   5;
        assert(LO <= HI);
        assert(RF >= SZ);

        for (int i = 0; i < RF; ++i) {
            Range rnil = new Range(-i, i);

            for (int l = LO; l + SZ <= HI; ++l) {
                Range test = new Range(l, l + SZ);

                boolean expected = (-i <= l && l + SZ <= i);
                assertEquals(test.isSubsetOf(rnil), expected);
            }
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJoinInvalid() {
        Range r1 = new Range(0, 1);
        Range r2 = new Range(2, 3);

        r1.join(r2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJoinInvalid2() {
        Range r1 = new Range(0, 1);
        Range r2 = new Range(2, 3);

        r2.join(r1);
    }

    @Test
    public void testJoin() {
        final int LO = -15;
        final int HI =  15;
        final int S1 =   5;
        final int S2 =   7;

        for (int l1 = LO; l1 + S1 <= HI; ++l1) {
            Range r1 = new Range(l1, l1 + S1);

            for (int l2 = l1 - S2; l2 <= l1 + S1; ++l2) {
                Range r2 = new Range(l2, l2 + S2);
                Range rr;
                try {
                    rr = r1.join(r2);
                } catch (IllegalArgumentException ex) {
                    System.out.printf("crashed for l1 = %d and l2 = %d\n", l1, l2);
                    throw ex;
                }

                int explo = Math.min(l1, l2);
                int exphi = Math.max(l1 + S1, l2 + S2);

                assertEquals(rr.getMin(), explo);
                assertEquals(rr.getMax(), exphi);
            }
        }
    }
}
