package cz.muni.fi.scheduler.model.domain;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import java.util.Comparator;

public class TimeSlotComparator implements Comparator<TimeSlot> {

    private TimeSlotComparator() {
        // hue hue hue
    }

    public static final TimeSlotComparator INSTANCE;

    static {
        INSTANCE = new TimeSlotComparator();
    }

    @Override
    public int compare(TimeSlot o1, TimeSlot o2) {
        requireNonNull(o1, "o1");
        requireNonNull(o2, "o2");

        int[] diffs = {
            o1.getStart() - o2.getStart(),
            o1.getEnd()   - o2.getEnd(),
            Long.compare(o1.getParent().getId(), o2.getParent().getId())
        };

        for (int x : diffs) {
            if (x != 0)
                return x;
        }

        return 0;
    }
}
