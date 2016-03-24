package cz.muni.fi.scheduler.model;

import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import cz.muni.fi.scheduler.model.domain.management.SlotManager;
import java.time.LocalDate;
import java.time.Month;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author cweorth
 */
public class AgendaNGTest {

    private static Configuration config;
    private static SlotManager   mngr;

    private static EntryRow row1;
    private static EntryRow row2;

    @BeforeClass
    public static void setUpClass() throws Exception {
        config = new Configuration.Builder()
                .addDate(LocalDate.of(2016, Month.MARCH, 17))
                .addDate(LocalDate.of(2016, Month.MARCH, 18))
                .value();

        mngr   = mock(SlotManager.class);

        row1 = mock(EntryRow.class);
        when(row1.getDay()).thenReturn(0);

        row2 = mock(EntryRow.class);
        when(row2.getDay()).thenReturn(1);
    }

    @Test
    public void testMarkTimeSlot() {
        Agenda   agenda  = new Agenda(config);
        Teacher  teacher = new Teacher(1, "X", "Y");

        TimeSlot slot1   = new TimeSlot(0, 2, row1, mngr);
        TimeSlot slot2   = new TimeSlot(2, 2, row1, mngr);
        TimeSlot slot3   = new TimeSlot(6, 2, row1, mngr);
        TimeSlot slot4   = new TimeSlot(8, 2, row2, mngr);

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
        Agenda   agenda  = new Agenda(config);
        Teacher  teacher = new Teacher(1, "X", "Y");

        TimeSlot slot1   = new TimeSlot(0, 1, row1, mngr);
        TimeSlot slot2   = new TimeSlot(1, 1, row1, mngr);
        TimeSlot slot3   = new TimeSlot(2, 1, row1, mngr);
        TimeSlot slot4   = new TimeSlot(3, 1, row1, mngr);

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
        Agenda   agenda  = new Agenda(config);
        Teacher  teacher = new Teacher(1, "X", "Y");

        TimeSlot slot1   = new TimeSlot(0, 1, row1, mngr);
        TimeSlot slot2   = new TimeSlot(1, 1, row1, mngr);
        TimeSlot slot3   = new TimeSlot(2, 1, row1, mngr);
        TimeSlot slot4   = new TimeSlot(3, 1, row1, mngr);

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
    public void testAnalyzeTimeSlot() {
        Agenda   agenda  = new Agenda(config);
        Teacher  teacher = new Teacher(1, "X", "Y");

        TimeSlot slot1   = new TimeSlot(0, 1, row1, mngr);
        TimeSlot slot2   = new TimeSlot(1, 1, row1, mngr);
        TimeSlot slot3   = new TimeSlot(2, 1, row1, mngr);
        TimeSlot slot4   = new TimeSlot(3, 1, row1, mngr);

        assertEquals(agenda.blockCount(teacher), 0);
        assertEquals(agenda.analyzeTimeSlot(teacher, slot1), 1);
        agenda.markTimeSlot(teacher, slot1);

        assertEquals(agenda.blockCount(teacher), 1);
        assertEquals(agenda.analyzeTimeSlot(teacher, slot3), 1);
        agenda.markTimeSlot(teacher, slot3);

        assertEquals(agenda.blockCount(teacher), 2);
        assertEquals(agenda.analyzeTimeSlot(teacher, slot4), 0);
        agenda.markTimeSlot(teacher, slot4);

        assertEquals(agenda.blockCount(teacher), 2);
        assertEquals(agenda.analyzeTimeSlot(teacher, slot2), -1);
        agenda.markTimeSlot(teacher, slot2);

        assertEquals(agenda.blockCount(teacher), 1);
    }

}
