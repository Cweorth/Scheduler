package cz.muni.fi.scheduler.model;

import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import java.time.LocalDate;
import java.time.Month;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AgendaNGTest {

    private static Configuration config;

    private static EntryRow row1;
    private static EntryRow row2;

    @BeforeClass
    public static void setUpClass() throws Exception {
        config = new Configuration.Builder()
                .addDate(LocalDate.of(2016, Month.MARCH, 17))
                .addDate(LocalDate.of(2016, Month.MARCH, 18))
                .value();

        row1 = mock(EntryRow.class);
        when(row1.getDay()).thenReturn(0);

        row2 = mock(EntryRow.class);
        when(row2.getDay()).thenReturn(1);
    }

    @Test
    public void testMarkTimeSlot() {
        Agenda   agenda  = new Agenda();
        Teacher  teacher = new Teacher(1, "X", "Y");

        TimeSlot slot1   = new TimeSlot(0, 2, row1);
        TimeSlot slot2   = new TimeSlot(2, 2, row1);
        TimeSlot slot3   = new TimeSlot(6, 2, row1);
        TimeSlot slot4   = new TimeSlot(8, 2, row2);

        assertEquals(agenda.blockCount(teacher), 0);
        assertEquals(agenda.markTimeSlot(teacher, slot1), 1);

        assertEquals(agenda.blockCount(teacher), 1);
        assertEquals(agenda.markTimeSlot(teacher, slot2), 0);

        assertEquals(agenda.blockCount(teacher), 1);
        assertEquals(agenda.markTimeSlot(teacher, slot3), 1);

        assertEquals(agenda.blockCount(teacher), 2);
        assertEquals(agenda.markTimeSlot(teacher, slot4), 1);

        assertEquals(agenda.blockCount(teacher), 3);
    }

    @Test
    public void testMarkTimeSlotJoining() {
        Agenda   agenda  = new Agenda();
        Teacher  teacher = new Teacher(1, "X", "Y");

        TimeSlot slot1   = new TimeSlot(0, 1, row1);
        TimeSlot slot2   = new TimeSlot(1, 1, row1);
        TimeSlot slot3   = new TimeSlot(2, 1, row1);
        TimeSlot slot4   = new TimeSlot(3, 1, row1);

        assertEquals(agenda.blockCount(teacher), 0);
        assertEquals(agenda.markTimeSlot(teacher, slot1), 1);

        assertEquals(agenda.blockCount(teacher), 1);
        assertEquals(agenda.markTimeSlot(teacher, slot3), 1);

        assertEquals(agenda.blockCount(teacher), 2);
        assertEquals(agenda.markTimeSlot(teacher, slot4), 0);

        assertEquals(agenda.blockCount(teacher), 2);
        assertEquals(agenda.markTimeSlot(teacher, slot2), -1);

        assertEquals(agenda.blockCount(teacher), 1);
    }

    @Test
    public void testUnmarkTimeSlot() {
        Agenda   agenda  = new Agenda();
        Teacher  teacher = new Teacher(1, "X", "Y");

        TimeSlot slot1   = new TimeSlot(0, 1, row1);
        TimeSlot slot2   = new TimeSlot(1, 1, row1);
        TimeSlot slot3   = new TimeSlot(2, 1, row1);
        TimeSlot slot4   = new TimeSlot(3, 1, row1);

        agenda.markTimeSlot(teacher, slot1);
        agenda.markTimeSlot(teacher, slot2);
        agenda.markTimeSlot(teacher, slot3);
        agenda.markTimeSlot(teacher, slot4);

        assertEquals(agenda.blockCount(teacher), 1);
        assertEquals(agenda.unmarkTimeSlot(teacher, slot3), 1);

        assertEquals(agenda.blockCount(teacher), 2);
        assertEquals(agenda.unmarkTimeSlot(teacher, slot2), 0);

        assertEquals(agenda.blockCount(teacher), 2);
        assertEquals(agenda.unmarkTimeSlot(teacher, slot1), -1);

        assertEquals(agenda.blockCount(teacher), 1);
        assertEquals(agenda.unmarkTimeSlot(teacher, slot4), -1);

        assertEquals(agenda.blockCount(teacher), 0);
    }

    @Test
    public void testAnalyzeTimeSlotAssign() {
        Agenda   agenda  = new Agenda();
        Teacher  teacher = new Teacher(1, "X", "Y");

        TimeSlot slot1   = new TimeSlot(0, 1, row1);
        TimeSlot slot2   = new TimeSlot(1, 1, row1);
        TimeSlot slot3   = new TimeSlot(2, 1, row1);
        TimeSlot slot4   = new TimeSlot(3, 1, row1);

        assertEquals(agenda.blockCount(teacher), 0);
        assertEquals(agenda.analyzeTimeSlotAssign(teacher, slot1), 1);
        agenda.markTimeSlot(teacher, slot1);

        assertEquals(agenda.blockCount(teacher), 1);
        assertEquals(agenda.analyzeTimeSlotAssign(teacher, slot3), 1);
        agenda.markTimeSlot(teacher, slot3);

        assertEquals(agenda.blockCount(teacher), 2);
        assertEquals(agenda.analyzeTimeSlotAssign(teacher, slot4), 0);
        agenda.markTimeSlot(teacher, slot4);

        assertEquals(agenda.blockCount(teacher), 2);
        assertEquals(agenda.analyzeTimeSlotAssign(teacher, slot2), -1);
        agenda.markTimeSlot(teacher, slot2);

        assertEquals(agenda.blockCount(teacher), 1);
    }

    @Test
    public void testAnalyzeTimeSlotUnassign() {
        Agenda   agenda  = new Agenda();
        Teacher  teacher = new Teacher(1, "X", "Y");

        TimeSlot[] slots = {
            new TimeSlot(0, 1, row1),
            new TimeSlot(0, 1, row1),
            new TimeSlot(1, 1, row1),
            new TimeSlot(2, 2, row1),
            new TimeSlot(3, 2, row1),
            new TimeSlot(4, 1, row1),
            new TimeSlot(1, 2, row2),
        };

        int expected[] = { 0, 0, 1, 1, 0, 0, -1 };

        for (TimeSlot slot : slots) {
            agenda.markTimeSlot(teacher, slot);
        }

        assertEquals(agenda.blockCount(teacher), 2);

        for (int i = 0; i < slots.length; ++i) {
            assertEquals(agenda.analyzeTimeSlotUnassign(teacher, slots[i]), expected[i]);
        }
    }

}
