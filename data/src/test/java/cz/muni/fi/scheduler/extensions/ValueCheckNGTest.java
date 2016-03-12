package cz.muni.fi.scheduler.extensions;

import cz.muni.fi.scheduler.extensions.ValueCheck;
import static org.testng.Assert.*;
import org.testng.annotations.*;

/**
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class ValueCheckNGTest {

    @Test
    public void requireNonNegativeIntTest() {
        for (int i = -10; i <= 10; ++i) {
            try {
                assertEquals(ValueCheck.requireNonNegative(i, String.valueOf(i)), i);
                if (i <  0) fail("Should have thrown IAE for " + i + '.');
            } catch (IllegalArgumentException ex) {
                if (i >= 0) fail("Exception thrown for " + i + '.');
                assertEquals(ex.getMessage(), String.valueOf(i) + " must be non-negative.");
            }
        }
    }

    @Test
    public void testRequireNonNegativeLongTest() {
        for (long i = -10L; i <= 10L; ++i) {
            try {
                assertEquals(ValueCheck.requireNonNegative(i, String.valueOf(i)), i);
                if (i <  0L) fail("Should have thrown IAE for " + i + '.');
            } catch (IllegalArgumentException ex) {
                if (i >= 0L) fail("Exception thrown for " + i + '.');
                assertEquals(ex.getMessage(), String.valueOf(i) + " must be non-negative.");
            }
        }
    }

    @Test
    public void requirePositiveIntTest() {
        for (int i = -10; i <= 10; ++i) {
            try {
                assertEquals(ValueCheck.requirePositive(i, String.valueOf(i)), i);
                if (i <= 0) fail("Should have thrown IAE for " + i + '.');
            } catch (IllegalArgumentException ex) {
                if (i >  0) fail("Exception thrown for " + i + '.');
                assertEquals(ex.getMessage(), String.valueOf(i) + " must be positive.");
            }
        }
    }

    @Test
    public void testRequirePositiveLongTest() {
        for (long i = -10L; i <= 10L; ++i) {
            try {
                assertEquals(ValueCheck.requirePositive(i, String.valueOf(i)), i);
                if (i <= 0L) fail("Should have thrown IAE for " + i + '.');
            } catch (IllegalArgumentException ex) {
                if (i >  0L) fail("Exception thrown for " + i + '.');
                assertEquals(ex.getMessage(), String.valueOf(i) + " must be positive.");
            }
        }
    }

    @Test
    public void testRequireNonNull() {
        String foo = "foo bar";
        assertEquals(ValueCheck.requireNonNull(foo, "foo"), foo);

        try {
            ValueCheck.requireNonNull(null, "null");
            fail("Should have thrown NPE for null.");
        } catch (NullPointerException ex) {
            assertEquals(ex.getMessage(), "null is null.");
        }
    }

}
