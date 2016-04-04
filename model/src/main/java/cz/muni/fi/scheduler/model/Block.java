package cz.muni.fi.scheduler.model;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.model.domain.TimeSlot;
import cz.muni.fi.scheduler.model.domain.TimeSlotComparator;
import cz.muni.fi.scheduler.utils.Range;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The class represents a single block of TimeSlots.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class Block implements Comparable<Block> {
    private final int   day;
    private final TimeSlot  first;
    private final TimeSlot  last;
    private final Range<Integer>    interval;

    private final SortedSet<TimeSlot> slots;

    private int memBlockMap[];

    private void createBlockMap() {
        if (memBlockMap != null)
            return;

        memBlockMap = new int[last.getEnd() - first.getStart()];

        int shift = first.getStart();
        slots.stream().forEach((slot) -> {
            for (int i = slot.getStart(); i < slot.getEnd(); ++i) {
                ++memBlockMap[i - shift];
            }
        });
    }

    private Block(TimeSlot first, TimeSlot last, SortedSet<TimeSlot> slots) {
        this.day = first.getParent().getDay();

        this.first = first;
        this.last  = last;
        this.slots = slots;

        this.interval = new Range<>(first.getStart(), last.getEnd());
    }

    private static SortedSet<TimeSlot> singleton(TimeSlot slot) {
        SortedSet<TimeSlot> set = new TreeSet<>(TimeSlotComparator.INSTANCE);
        set.add(slot);
        return set;
    }

    public Block(TimeSlot slot) {
        this(requireNonNull(slot, "slot"), slot, singleton(slot));
    }

    public boolean canJoin(Block other) {
        requireNonNull(other, "other");

        return (day == other.getDay()) && (interval.overlaps(other.interval));
    }

    public Block join(Block other) {
        if (!canJoin(other)) {
            throw new IllegalArgumentException("Cannot join these blocks.");
        }

        TimeSlot newFirst = interval.contains(other.interval.getMin())
                ? first : other.first;
        TimeSlot newLast  = interval.contains(other.interval.getMax())
                ? last  : other.last;

        SortedSet<TimeSlot> nslots = new TreeSet<>(slots);
        nslots.addAll(other.slots);
        return new Block(newFirst, newLast, nslots);
    }

    public List<Block> split(TimeSlot slot) {
        if (!slots.contains(slot)) {
            throw new IllegalArgumentException("The block does not contain given slot.");
        }

        Queue<TimeSlot> queue = new LinkedList<>(slots);
        List<Block> nblocks   = new ArrayList<>();

        while (!queue.isEmpty()) {
            TimeSlot nfirst = queue.poll();
            if (nfirst.equals(slot))
                continue;

            TimeSlot            nlast  = nfirst;
            SortedSet<TimeSlot> nslots = singleton(nfirst);

            while (!queue.isEmpty() && (queue.peek().getStart() <= nlast.getEnd())) {
                TimeSlot ns = queue.poll();
                if (ns.equals(slot))
                    continue;

                nslots.add(ns);
                if (ns.getEnd() > nlast.getEnd())
                    nlast = ns;
            }

            nblocks.add(new Block(nfirst, nlast, nslots));
        }

        return nblocks;
    }

    public boolean contains(TimeSlot slot) {
        return (slot != null) && slots.contains(slot);
    }

    public int splitFactor(TimeSlot slot) {
        requireNonNull(slot, "slot");

        createBlockMap();
        int tmpMap[] = Arrays.copyOf(memBlockMap, memBlockMap.length);

        int shift = first.getStart();

        for (int i = slot.getStart(); i < slot.getEnd(); ++i) {
            --tmpMap[i - shift];
        }

        int counter = 0;
        for (int i = 0; i < tmpMap.length; ++i) {
            if (tmpMap[i] != 0 && (i == 0 || tmpMap[i - 1] == 0))
                ++counter;
        }

        return counter;
    }

    public int      getDay()   { return day;   }
    public TimeSlot getFirst() { return first; }
    public TimeSlot getLast()  { return last;  }

    public Set<TimeSlot>  getSlots()    { return Collections.unmodifiableSet(slots); }
    public Range<Integer> getInterval() { return interval; }

    @Override
    public int compareTo(Block other) {
        requireNonNull(other, "other");

        int sdiff = first.getStart() - other.first.getStart();
        int ediff =  last.getEnd()   - other.last.getEnd();

        return sdiff != 0 ? sdiff : ediff;
    }
}
