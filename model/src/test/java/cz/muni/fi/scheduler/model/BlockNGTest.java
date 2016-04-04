package cz.muni.fi.scheduler.model;

import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BlockNGTest {

    private static List<TimeSlot> timeSlots;
    private static List<Block>    blocks;
    private static EntryRow       row;

    @BeforeClass
    public static void setUp() {
        timeSlots = new ArrayList<>();
        row = mock(EntryRow.class);
        when(row.getStart()).thenReturn(0);
        when(row.getEnd()).thenReturn(5);

        for (int i = 0; i < 5; ++i) {
            timeSlots.add(new TimeSlot(i, 1, row));
        }

        blocks = timeSlots.stream().map(Block::new).collect(Collectors.toList());
        when(row.streamTimeSlots()).thenReturn(timeSlots.stream());
    }

    @Test
    public void testCanJoinSameDay() {
        for (int i = 0; i + 1 < blocks.size(); ++i) {
            assertTrue(blocks.get(i).canJoin(blocks.get(i + 1)));
        }

        for (int i = 0; i + 2 < blocks.size(); ++i) {
            for (int j = i + 2; j < blocks.size(); ++j) {
                assertFalse(blocks.get(i).canJoin(blocks.get(j)));
            }
        }
    }

    @Test
    public void testCanJoinOtherDay() {
        EntryRow row1 = mock(EntryRow.class);
        when(row1.getDay()).thenReturn(1);

        EntryRow row2 = mock(EntryRow.class);
        when(row2.getDay()).thenReturn(2);

        Block b1 = new Block(new TimeSlot(10, 2, row1));
        Block b2 = new Block(new TimeSlot(12, 2, row1));
        Block b3 = new Block(new TimeSlot(12, 2, row2));

        assertTrue(b1.canJoin(b2));
        assertFalse(b1.canJoin(b3));
        assertFalse(b2.canJoin(b3));
    }

    /**
     * Test of join method, of class Block.
     */
    @Test
    public void testJoin() {
        // B0  | B1  | B2  | B3  | B4  |
        // B00       | B01       | B02 |

        Block b00 = blocks.get(0).join(blocks.get(1));
        Block b01 = blocks.get(2).join(blocks.get(3));
        Block b02 = blocks.get(4);

        assertEquals(b00.getFirst(), timeSlots.get(0));
        assertEquals(b00.getLast(),  timeSlots.get(1));
        assertEquals(b01.getFirst(), timeSlots.get(2));
        assertEquals(b01.getLast(),  timeSlots.get(3));

        // B00       | B01       | B02 |
        // B10       | B11             |

        Block b10 = b00;
        Block b11 = b01.join(b02);

        assertEquals(b11.getFirst(), timeSlots.get(2));
        assertEquals(b11.getLast(),  timeSlots.get(4));

        // B20

        Block b20 = b10.join(b11);

        assertEquals(b20.getFirst(), timeSlots.get(0));
        assertEquals(b20.getLast(),  timeSlots.get(4));
    }

    /**
     * Test of split method, of class Block.
     */
    @Test
    public void testSplitSimple() {
        Block superblock = blocks.stream().reduce((a, b) -> a.join(b)).get();

        List<Block> split = superblock.split(timeSlots.get(2));

        assertEquals(split.size(), 2);

        Block lo = split.get(0);
        Block hi = split.get(1);

        assertEquals(lo.getFirst(), timeSlots.get(0));
        assertEquals(lo.getLast(),  timeSlots.get(1));
        assertEquals(hi.getFirst(), timeSlots.get(3));
        assertEquals(hi.getLast(),  timeSlots.get(4));
    }

    @Test
    public void testSplitComplex() {
        // | B1      | B3      |
        // | B0 | B2      | B4 |

        List<Block> bxs = Arrays.asList(
                new Block(new TimeSlot(0, 1, row)),
                new Block(new TimeSlot(0, 2, row)),
                new Block(new TimeSlot(1, 2, row)),
                new Block(new TimeSlot(2, 2, row)),
                new Block(new TimeSlot(3, 1, row))
        );

        Block superblock = bxs.stream().reduce((a, b) -> a.join(b)).get();
        List<Block> split = superblock.split(new TimeSlot(1, 2, row));

        assertEquals(split.size(), 1);
        Block nb = split.get(0);
        assertEquals(nb.getFirst().getStart(), 0);
        assertEquals(nb.getLast().getEnd(),    4);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSplitInvalid() {
        Block superblock = blocks.stream().reduce((a,b) -> a.join(b)).get();

        superblock.split(new TimeSlot(3, 8, row));
    }
}
