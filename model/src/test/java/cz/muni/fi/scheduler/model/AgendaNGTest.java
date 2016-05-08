package cz.muni.fi.scheduler.model;

import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import java.time.LocalDate;
import java.time.Month;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AgendaNGTest {

    private static EntryRow[] rows;
    private static Teacher[]  teachers;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Configuration.Builder builder = new Configuration.Builder()
                .addDate(LocalDate.of(2016, Month.MARCH, 17))
                .addDate(LocalDate.of(2016, Month.MARCH, 18));

        rows = new EntryRow[4];

        builder.setFullExamLength(1);
        rows[0] = new EntryRow(0, builder.value());
        rows[2] = new EntryRow(1, builder.value());

        builder.setFullExamLength(2);
        rows[1] = new EntryRow(0, builder.value());
        rows[3] = new EntryRow(1, builder.value());

        for (EntryRow row : rows) {
            while (row.getEnd() < 6)
                row.extendBack();
        }

        teachers = new Teacher[10];
        for (int i = 0; i < 10; ++i) {
            teachers[i] = new Teacher(i, "TN" + i, "TS" + i);
        }
    }

    /*
     * What we got:
     *         /-----------------------------\
     * rows[0] |    |    |    |    |    |    |  DAY 0
     * rows[1] |         |         |         |
     *         |-----------------------------|
     * rows[2] |    |    |    |    |    |    |  DAY 1
     * rows[3] |         |         |         |
     *         \-----------------------------/
     *         0    1    2    3    4    5    6
     */

    @Test
    public void sanity() {
        assertEquals(rows[0].streamTimeSlots().count(), 6);
        assertEquals(rows[1].streamTimeSlots().count(), 3);
        assertEquals(rows[2].streamTimeSlots().count(), 6);
        assertEquals(rows[3].streamTimeSlots().count(), 3);
    }

//==============================================================================
//  TIME SLOT TESTS
//==============================================================================

    @Test
    public void testMarkTimeSlot() {
        Agenda   agenda  = new Agenda();

        TimeSlot markedSlots[] = new TimeSlot[] {
                rows[1].getSlot(0),
                rows[1].getSlot(1),
                rows[0].getSlot(2),
                rows[0].getSlot(5),
                rows[2].getSlot(4),
                rows[0].getSlot(4),
                rows[2].getSlot(2),
                rows[3].getSlot(1)
        };

        int expectedCount[] = new int[] { 1,  1,  1,  2,  3,  2,  3,  2 };
        int expectedDiff[]  = new int[] { 1,  0,  0,  1,  1, -1,  1, -1 };

        /*
         * Marked slots and their order
         *         /-----------------------------\
         * rows[0] |    |    |2222|    |5555|3333|  DAY 0
         * rows[1] |000000000|111111111|         |
         *         |-----------------------------|
         * rows[2] |    |    |6666|    |4444|    |  DAY 1
         * rows[3] |         |777777777|         |
         *         \-----------------------------/
         */

        Teacher t0 = teachers[0];
        Teacher tr = teachers[1]; // reference teacher

        assertEquals(agenda.blockCount(t0), 0);
        assertEquals(agenda.blockCount(tr), 0);

        for (int i = 0; i < markedSlots.length; ++i) {
             assertEquals(agenda.markTimeSlot(t0, markedSlots[i]), expectedDiff[i]);
             assertEquals(agenda.blockCount(t0), expectedCount[i]);
             assertEquals(agenda.blockCount(tr), 0);
        }
    }

    @Test
    public void testUnmarkTimeSlot() {
        Agenda   agenda  = new Agenda();

        TimeSlot markedSlots[] = new TimeSlot[] {
                rows[0].getSlot(3),
                rows[1].getSlot(1),
                rows[3].getSlot(2),
                rows[0].getSlot(4),
                rows[0].getSlot(2),
                rows[0].getSlot(1),
                rows[3].getSlot(0),
                rows[3].getSlot(1)
        };

        int expectedCount[] = new int[] { 2,  3,  3,  2,  2,  1,  1,  0};
        int expectedDiff[]  = new int[] { 0,  1,  0, -1,  0, -1,  0, -1};

        /*
         * Marked slots and their order of removal
         *         /-----------------------------\
         * rows[0] |    |6666|5555|1111|4444|    |  DAY 0
         * rows[1] |         |222222222|         |
         *         |-----------------------------|
         * rows[2] |    |    |    |    |    |    |  DAY 1
         * rows[3] |777777777|888888888|333333333|
         *         \-----------------------------/
         */

        Teacher t0 = teachers[0];
        Teacher tr = teachers[1]; // reference teacher (marked)
        Teacher tx = teachers[2]; // reference teacher (unmarked)

        for (TimeSlot slot : markedSlots) {
            agenda.markTimeSlot(t0, slot);
            agenda.markTimeSlot(tr, slot);
        }

        assertEquals(agenda.blockCount(t0), 2);
        assertEquals(agenda.blockCount(tr), 2);
        assertEquals(agenda.blockCount(tx), 0);

        for (int i = 0; i < markedSlots.length; ++i) {
            assertEquals(agenda.unmarkTimeSlot(t0, markedSlots[i]), expectedDiff[i]);
            assertEquals(agenda.blockCount(t0), expectedCount[i]);
        }

        assertEquals(agenda.blockCount(tr), 2);
        assertEquals(agenda.blockCount(tx), 0);
    }

    @Test
    public void testAnalyzeTimeSlotAssign() {
        Agenda   agenda  = new Agenda();

        TimeSlot fixedSlots[] = new TimeSlot[] {
                rows[0].getSlot(1),
                rows[0].getSlot(3),
                rows[1].getSlot(2),
                rows[2].getSlot(3),
                rows[3].getSlot(0)
        };

        TimeSlot markedSlots[] = new TimeSlot[] {
                rows[0].getSlot(0),
                rows[0].getSlot(2),
                rows[1].getSlot(1),
                rows[0].getSlot(4),
                rows[2].getSlot(4),
                rows[2].getSlot(2),
                rows[3].getSlot(1),
                rows[2].getSlot(5)
        };

        int expectedDiff[]  = new int[] { 0, -1, -1,  0,  0, -1, -1,  1};

        /*
         * What shall be tested:
         *         /-----------------------------\
         * rows[0] |1111|XXXX|2222|XXXX|4444|    |  DAY 0
         * rows[1] |         |333333333|XXXXXXXXX|
         *         |-----------------------------|
         * rows[2] |    |    |6666|XXXX|5555|8888|  DAY 1
         * rows[3] |XXXXXXXXX|777777777|         |
         *         \-----------------------------/
         */

        Teacher t0 = teachers[0];
        Teacher tx = teachers[2]; // reference teacher (unmarked)

        for (TimeSlot slot : fixedSlots) {
            agenda.markTimeSlot(t0, slot);
        }

        assertEquals(agenda.blockCount(t0), 4);
        assertEquals(agenda.blockCount(tx), 0);

        for (int i = 0; i < markedSlots.length; ++i) {
            assertEquals(agenda.analyzeTimeSlotAssign(t0, markedSlots[i]), expectedDiff[i]);

        }

        assertEquals(agenda.blockCount(t0), 4);
        assertEquals(agenda.blockCount(tx), 0);
    }

    @Test
    public void testAnalyzeTimeSlotUnassign() {
        Agenda   agenda  = new Agenda();

        TimeSlot markedSlots[] = new TimeSlot[] {
                rows[0].getSlot(0),
                rows[0].getSlot(2),
                rows[0].getSlot(3),
                rows[0].getSlot(5),
                rows[1].getSlot(0),
                rows[2].getSlot(0),
                rows[2].getSlot(1),
                rows[2].getSlot(2),
                rows[2].getSlot(4),
        };

        int expectedDiff[]  = new int[] {  0,  1,  0, -1,  1,  0,  1,  0, -1 };

        /*
         * Marked slots and their order of tests
         *         /-----------------------------\
         * rows[0] |1111|    |2222|3333|    |4444|  DAY 0
         * rows[1] |555555555|         |         |
         *         |-----------------------------|
         * rows[2] |6666|7777|8888|    |9999|    |  DAY 1
         * rows[3] |         |         |         |
         *         \-----------------------------/
         */

        Teacher t0 = teachers[0];
        Teacher tr = teachers[1]; // reference teacher (marked)

        for (TimeSlot slot : markedSlots) {
            agenda.markTimeSlot(t0, slot);
        }

        assertEquals(agenda.blockCount(t0), 4);
        assertEquals(agenda.blockCount(tr), 0);

        for (int i = 0; i < markedSlots.length; ++i) {
            assertEquals(agenda.analyzeTimeSlotUnassign(t0, markedSlots[i]), expectedDiff[i]);
        }

        assertEquals(agenda.blockCount(t0), 4);
        assertEquals(agenda.blockCount(tr), 0);
    }

//==============================================================================
//  MEMBER SLOT TESTS
//==============================================================================

    @Test
    public void testMarkMemberSlot() {
        Agenda   agenda  = new Agenda();

        /*
         * What we got:
         *         /-----------------------------\  T0  T1  T2
         * rows[0] |    |    |    |    |    |    |  11  33
         * rows[1] |         |         |         |  22
         *         |-----------------------------|
         * rows[2] |    |    |    |    |    |    |      44
         * rows[3] |         |         |         |          55
         *         \-----------------------------/
         */

        Teacher t0 = teachers[0];
        Teacher t1 = teachers[1];
        Teacher t2 = teachers[2];
        Teacher tr = teachers[3];

        assertEquals(agenda.blockCount(t0), 0);
        assertEquals(agenda.blockCount(t1), 0);
        assertEquals(agenda.blockCount(t2), 0);
        assertEquals(agenda.blockCount(tr), 0);

        agenda.markMemberSlot(t0, rows[0].getMemberSlot(0));
        agenda.markMemberSlot(t0, rows[1].getMemberSlot(0));
        agenda.markMemberSlot(t1, rows[0].getMemberSlot(1));
        agenda.markMemberSlot(t1, rows[2].getMemberSlot(1));
        agenda.markMemberSlot(t2, rows[3].getMemberSlot(2));

        assertEquals(agenda.blockCount(t0), 1);
        assertEquals(agenda.blockCount(t1), 2);
        assertEquals(agenda.blockCount(t2), 1);
        assertEquals(agenda.blockCount(tr), 0);
    }

    @Test
    public void testUnmarkMemberSlot() {
        Agenda agenda = new Agenda();


        /*
         * What we got:
         *         /-----------------------------\  T0 T1 T2
         * rows[0] |    |    |    |    |    |    |  11 33
         * rows[1] |         |         |         |  22
         *         |-----------------------------|
         * rows[2] |    |    |    |    |    |    |     44
         * rows[3] |         |         |         |        55
         *         \-----------------------------/
         *         0    1    2    3    4    5    6
         */

        Teacher t0 = teachers[0];
        Teacher t1 = teachers[1];
        Teacher t2 = teachers[2];
        Teacher tr = teachers[3];

        agenda.markMemberSlot(t0, rows[0].getMemberSlot(0));
        agenda.markMemberSlot(t0, rows[1].getMemberSlot(0));
        agenda.markMemberSlot(t1, rows[0].getMemberSlot(1));
        agenda.markMemberSlot(t1, rows[2].getMemberSlot(1));
        agenda.markMemberSlot(t2, rows[3].getMemberSlot(2));

        assertEquals(agenda.blockCount(t0), 1);
        assertEquals(agenda.blockCount(t1), 2);
        assertEquals(agenda.blockCount(t2), 1);
        assertEquals(agenda.blockCount(tr), 0);

        assertEquals(agenda.unmarkMemberSlot(t0, rows[0].getMemberSlot(0)),  0);
        assertEquals(agenda.blockCount(t0), 1);
        assertEquals(agenda.unmarkMemberSlot(t0, rows[1].getMemberSlot(0)), -1);
        assertEquals(agenda.blockCount(t0), 0);
        assertEquals(agenda.unmarkMemberSlot(t1, rows[0].getMemberSlot(1)), -1);
        assertEquals(agenda.blockCount(t1), 1);
        assertEquals(agenda.unmarkMemberSlot(t1, rows[2].getMemberSlot(1)), -1);
        assertEquals(agenda.blockCount(t1), 0);
        assertEquals(agenda.unmarkMemberSlot(t2, rows[3].getMemberSlot(2)), -1);
        assertEquals(agenda.blockCount(t2), 0);
    }

    @Test
    public void testAnalyzeMemberSlotAssign() {
        Agenda  agenda = new Agenda();
        Teacher t0     = teachers[0];

        agenda.markMemberSlot(t0, rows[0].getMemberSlot(0));

        assertEquals(agenda.analyzeMemberSlotAssign(t0, rows[1].getMemberSlot(0)), 0);
        assertEquals(agenda.analyzeMemberSlotAssign(t0, rows[1].getMemberSlot(1)), 0);
        assertEquals(agenda.analyzeMemberSlotAssign(t0, rows[2].getMemberSlot(0)), 1);
        assertEquals(agenda.analyzeMemberSlotAssign(t0, rows[2].getMemberSlot(1)), 1);
    }

    @Test
    public void testAnalyzeMemberSlotUnassign() {
        Agenda  agenda = new Agenda();
        Teacher t0     = teachers[0];

        agenda.markMemberSlot(t0, rows[0].getMemberSlot(0));
        agenda.markMemberSlot(t0, rows[1].getMemberSlot(0));
        agenda.markMemberSlot(t0, rows[2].getMemberSlot(0));

        assertEquals(agenda.blockCount(t0), 2);

        assertEquals(agenda.analyzeMemberSlotUnassign(t0, rows[0].getMemberSlot(0)),  0);
        assertEquals(agenda.analyzeMemberSlotUnassign(t0, rows[1].getMemberSlot(0)),  0);
        assertEquals(agenda.analyzeMemberSlotUnassign(t0, rows[2].getMemberSlot(0)), -1);
        assertEquals(agenda.analyzeMemberSlotUnassign(t0, rows[2].getMemberSlot(1)), -1);
    }

//==============================================================================
//  MIXED TESTS
//==============================================================================

    @Test
    public void testAnalyzeTimeSlotAssignWithFixedMemberSlot() {
        Agenda agenda = new Agenda();

        /*
         * What we got:
         *         /-----------------------------\  T0
         * rows[0] |    |    |4444|    |1111|    |  11
         * rows[1] |         |         |222222222|
         *         |-----------------------------|
         * rows[2] |    |    |    |    |    |3333|
         * rows[3] |         |555555555|         |
         *         \-----------------------------/
         *         0    1    2    3    4    5    6
         */

        Teacher t0 = teachers[0];

        agenda.markMemberSlot(t0, rows[0].getMemberSlot(1));

        TimeSlot markedSlots[] = new TimeSlot[] {
                rows[0].getSlot(4),
                rows[1].getSlot(2),
                rows[2].getSlot(4),
                rows[0].getSlot(2),
                rows[3].getSlot(1)
        };

        int expectedDiff[]  = new int[] {  0,  0,  1,  0,  1 };

//        int i = 1;
        for (int i = 0; i < markedSlots.length; ++i) {
            assertEquals(agenda.analyzeTimeSlotAssign(t0, markedSlots[i]), expectedDiff[i]);
        }

        assertEquals(agenda.blockCount(t0), 1);
    }

    @Test
    public void testMarkTimeSlotWithFixedMemberSlot() {
        Agenda agenda = new Agenda();

        /*
         * What we got:
         *         /-----------------------------\  T0
         * rows[0] |    |    |4444|6666|1111|    |  11
         * rows[1] |         |         |222222222|
         *         |-----------------------------|
         * rows[2] |    |    |    |    |    |3333|
         * rows[3] |         |555555555|         |
         *         \-----------------------------/
         *         0    1    2    3    4    5    6
         */

        Teacher t0 = teachers[0];

        agenda.markMemberSlot(t0, rows[0].getMemberSlot(1));

        TimeSlot markedSlots[] = new TimeSlot[] {
                rows[0].getSlot(4),
                rows[1].getSlot(2),
                rows[2].getSlot(5),
                rows[0].getSlot(2),
                rows[3].getSlot(1),
                rows[0].getSlot(3)
        };

        int expectedDiff[]  = new int[] {  0,  0,  1,  0,  1,  0 };
        int expectedCount[] = new int[] {  1,  1,  2,  2,  3,  3 };

        for (int i = 0; i < markedSlots.length; ++i) {
            assertEquals(agenda.markTimeSlot(t0, markedSlots[i]), expectedDiff[i]);
            assertEquals(agenda.blockCount(t0), expectedCount[i]);
        }

        assertEquals(agenda.blockCount(t0), 3);
    }

    @Test
    public void testAnalyzeTimeSlotUnassignWithFixedMemberSlot() {
        Agenda agenda = new Agenda();

        /*
         * What we got:
         *         /-----------------------------\  T0
         * rows[0] |    |    |    |6666|1111|    |  11
         * rows[1] |         |         |222222222|
         *         |-----------------------------|
         * rows[2] |4444|    |    |    |7777|3333|
         * rows[3] |         |555555555|         |
         *         \-----------------------------/
         *         0    1    2    3    4    5    6
         */

        Teacher t0 = teachers[0];

        agenda.markMemberSlot(t0, rows[0].getMemberSlot(1));

        TimeSlot markedSlots[] = new TimeSlot[] {
                rows[0].getSlot(4),
                rows[1].getSlot(2),
                rows[2].getSlot(5),
                rows[2].getSlot(0),
                rows[3].getSlot(1),
                rows[0].getSlot(3),
                rows[2].getSlot(4)
        };

        for (TimeSlot slot : markedSlots) {
            agenda.markTimeSlot(t0, slot);
        }

        assertEquals(agenda.blockCount(t0), 3);

        int expectedDiff[] = new int[] {  0,  0,  0, -1,  0,  0,  1 };

        for (int i = 0; i < markedSlots.length; ++i) {
            assertEquals(agenda.analyzeTimeSlotUnassign(t0, markedSlots[i]), expectedDiff[i]);
        }

        assertEquals(agenda.blockCount(t0), 3);
    }

    @Test
    public void testUnmarkTimeSlotWithFixedMemberSlot() {
        Agenda agenda = new Agenda();

        /*
         * What we got:
         *         /-----------------------------\  T0
         * rows[0] |    |    |    |6666|1111|    |  11
         * rows[1] |         |         |222222222|
         *         |-----------------------------|
         * rows[2] |4444|    |    |    |3333|7777|
         * rows[3] |         |555555555|         |
         *         \-----------------------------/
         *         0    1    2    3    4    5    6
         */

        Teacher t0 = teachers[0];

        agenda.markMemberSlot(t0, rows[0].getMemberSlot(1));

        TimeSlot markedSlots[] = new TimeSlot[] {
                rows[0].getSlot(4),
                rows[1].getSlot(2),
                rows[2].getSlot(4),
                rows[2].getSlot(0),
                rows[3].getSlot(1),
                rows[0].getSlot(3),
                rows[2].getSlot(5)
        };

        for (TimeSlot slot : markedSlots) {
            agenda.markTimeSlot(t0, slot);
        }

        assertEquals(agenda.blockCount(t0), 3);

        int expectedDiff[]  = new int[] {  0,  0,  1, -1, -1,  0, -1 };
        int expectedCount[] = new int[] {  3,  3,  4,  3,  2,  2,  1 };

        for (int i = 0; i < markedSlots.length; ++i) {
            assertEquals(agenda.unmarkTimeSlot(t0, markedSlots[i]), expectedDiff[i]);
            assertEquals(agenda.blockCount(t0), expectedCount[i]);
        }

        assertEquals(agenda.blockCount(t0), 1);
    }

    @Test
    public void testAnalyzeMemberSlotOverFixedTimeSlots() {
        Agenda agenda = new Agenda();

        /*
         * What we got:
         *         /-----------------------------\  T0
         * rows[0] |    |XXXX|    |XXXX|    |    |  11
         * rows[1] |         |         |XXXXXXXXX|  22
         *         |-----------------------------|
         * rows[2] |    |XXXX|    |XXXX|    |XXXX|  33
         * rows[3] |XXXXXXXXX|         |         |  44
         *         \-----------------------------/
         *         0    1    2    3    4    5    6
         */

        Teacher t0 = teachers[0];

        agenda.markTimeSlot(t0, rows[0].getSlot(1));
        agenda.markTimeSlot(t0, rows[0].getSlot(3));
        agenda.markTimeSlot(t0, rows[1].getSlot(2));
        agenda.markTimeSlot(t0, rows[2].getSlot(1));
        agenda.markTimeSlot(t0, rows[2].getSlot(3));
        agenda.markTimeSlot(t0, rows[2].getSlot(5));
        agenda.markTimeSlot(t0, rows[3].getSlot(0));

        assertEquals(agenda.blockCount(t0), 5);

        assertEquals(agenda.analyzeMemberSlotAssign(t0, rows[0].getMemberSlot(0)), -1);
        assertEquals(agenda.analyzeMemberSlotAssign(t0, rows[1].getMemberSlot(1)), -1);
        assertEquals(agenda.analyzeMemberSlotAssign(t0, rows[2].getMemberSlot(0)), -2);
        assertEquals(agenda.analyzeMemberSlotAssign(t0, rows[3].getMemberSlot(1)), -2);

        assertEquals(agenda.blockCount(t0), 5);
    }

    @Test
    public void testMarkMemberSlotOverFixedTimeSlots() {
        Agenda agenda = new Agenda();

        /*
         * What we got:
         *         /-----------------------------\  T0
         * rows[0] |    |XXXX|    |XXXX|    |    |  11
         * rows[1] |         |         |XXXXXXXXX|  22
         *         |-----------------------------|
         * rows[2] |    |XXXX|    |XXXX|    |XXXX|  33
         * rows[3] |XXXXXXXXX|         |         |  44
         *         \-----------------------------/
         *         0    1    2    3    4    5    6
         */

        Teacher t0 = teachers[0];

        agenda.markTimeSlot(t0, rows[0].getSlot(1));
        agenda.markTimeSlot(t0, rows[0].getSlot(3));
        agenda.markTimeSlot(t0, rows[1].getSlot(2));
        agenda.markTimeSlot(t0, rows[2].getSlot(1));
        agenda.markTimeSlot(t0, rows[2].getSlot(3));
        agenda.markTimeSlot(t0, rows[2].getSlot(5));
        agenda.markTimeSlot(t0, rows[3].getSlot(0));

        assertEquals(agenda.blockCount(t0), 5);

        assertEquals(agenda.markMemberSlot(t0, rows[0].getMemberSlot(0)), -1);
        assertEquals(agenda.markMemberSlot(t0, rows[1].getMemberSlot(1)),  0);
        assertEquals(agenda.markMemberSlot(t0, rows[2].getMemberSlot(0)), -2);
        assertEquals(agenda.markMemberSlot(t0, rows[3].getMemberSlot(1)),  0);

        assertEquals(agenda.blockCount(t0), 2);
    }

    @Test
    public void testAnalyzeMemberSlotUnassignOverFixedTimeSlots() {
        Agenda agenda = new Agenda();

        /*
         * What we got:
         *         /-----------------------------\  T0
         * rows[0] |    |XXXX|    |XXXX|    |    |  11
         * rows[1] |         |         |XXXXXXXXX|
         *         |-----------------------------|
         * rows[2] |    |XXXX|    |XXXX|    |XXXX|
         * rows[3] |XXXXXXXXX|         |         |  22
         *         \-----------------------------/
         *         0    1    2    3    4    5    6
         */

        Teacher t0 = teachers[0];

        agenda.markTimeSlot(t0, rows[0].getSlot(1));
        agenda.markTimeSlot(t0, rows[0].getSlot(3));
        agenda.markTimeSlot(t0, rows[1].getSlot(2));
        agenda.markTimeSlot(t0, rows[2].getSlot(1));
        agenda.markTimeSlot(t0, rows[2].getSlot(3));
        agenda.markTimeSlot(t0, rows[2].getSlot(5));
        agenda.markTimeSlot(t0, rows[3].getSlot(0));

        assertEquals(agenda.blockCount(t0), 5);

        agenda.markMemberSlot(t0, rows[0].getMemberSlot(0));
        agenda.markMemberSlot(t0, rows[3].getMemberSlot(1));

        assertEquals(agenda.blockCount(t0), 2);

        assertEquals(agenda.analyzeMemberSlotUnassign(t0, rows[0].getMemberSlot(0)), 1);
        assertEquals(agenda.analyzeMemberSlotUnassign(t0, rows[3].getMemberSlot(1)), 2);

        assertEquals(agenda.blockCount(t0), 2);
    }

    @Test
    public void testUnmarkMemberSlotOverFixedTimeSlots() {
        Agenda agenda = new Agenda();

        /*
         * What we got:
         *         /-----------------------------\  T0
         * rows[0] |    |XXXX|    |XXXX|    |    |  11
         * rows[1] |         |         |XXXXXXXXX|  22
         *         |-----------------------------|
         * rows[2] |    |XXXX|    |XXXX|    |XXXX|  33
         * rows[3] |XXXXXXXXX|         |         |  44
         *         \-----------------------------/
         *         0    1    2    3    4    5    6
         */

        Teacher t0 = teachers[0];

        agenda.markTimeSlot(t0, rows[0].getSlot(1));
        agenda.markTimeSlot(t0, rows[0].getSlot(3));
        agenda.markTimeSlot(t0, rows[1].getSlot(2));
        agenda.markTimeSlot(t0, rows[2].getSlot(1));
        agenda.markTimeSlot(t0, rows[2].getSlot(3));
        agenda.markTimeSlot(t0, rows[2].getSlot(5));
        agenda.markTimeSlot(t0, rows[3].getSlot(0));

        assertEquals(agenda.blockCount(t0), 5);

        agenda.markMemberSlot(t0, rows[0].getMemberSlot(0));
        agenda.markMemberSlot(t0, rows[1].getMemberSlot(1));
        agenda.markMemberSlot(t0, rows[2].getMemberSlot(0));
        agenda.markMemberSlot(t0, rows[3].getMemberSlot(1));

        assertEquals(agenda.blockCount(t0), 2);

        assertEquals(agenda.unmarkMemberSlot(t0, rows[0].getMemberSlot(0)), 0);
        assertEquals(agenda.unmarkMemberSlot(t0, rows[2].getMemberSlot(0)), 0);

        assertEquals(agenda.blockCount(t0), 2);

        assertEquals(agenda.unmarkMemberSlot(t0, rows[1].getMemberSlot(1)), 1);
        assertEquals(agenda.blockCount(t0), 3);

        assertEquals(agenda.unmarkMemberSlot(t0, rows[3].getMemberSlot(1)), 2);
        assertEquals(agenda.blockCount(t0), 5);
    }
}
