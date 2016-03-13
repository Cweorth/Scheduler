package cz.muni.fi.scheduler.model.domain;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.model.domain.management.SlotManager;
import cz.muni.fi.scheduler.model.domain.management.TimeSlotListener;
import java.util.Collection;

/**
 * A variable for exams.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class TimeSlot extends Slot {
    private final int start;
    private final int end;
    private final int length;

    public TimeSlot(int start, int length, EntryRow parent, SlotManager manager) {
        super(parent, manager);
        this.start  = start;
        this.end    = start + length;
        this.length = requirePositive(length, "length");

        addVariableListener(new TimeSlotListener(this, manager));
    }

    //<editor-fold defaultstate="collapsed" desc="[  Getters  ]">

    public int getStart()  { return start;  }
    public int getEnd()    { return end;    }
    public int getLength() { return length; }

    //</editor-fold>

    public void setStudents(Collection<Student> students) {
        setDomain(students);
    }

    //<editor-fold defaultstate="collapsed" desc="[  Comparable  ]">

    @Override
    public int compareTo(Slot variable) {
        if (!(variable instanceof TimeSlot))
            return super.compareTo(variable);

        final TimeSlot other = (TimeSlot) variable;

        int diff = start - other.start;

        if (diff != 0)
            return diff;

        return end - other.end;
    }

    //</editor-fold>

}
