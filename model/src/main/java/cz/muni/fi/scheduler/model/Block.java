package cz.muni.fi.scheduler.model;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.model.domain.TimeSlot;
import cz.muni.fi.scheduler.model.domain.TimeSlotComparator;
import java.util.ArrayList;
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
    private final int      day;
    private final TimeSlot first;
    private final TimeSlot last;

    private final SortedSet<TimeSlot> slots;

    private Block(TimeSlot first, TimeSlot last, SortedSet<TimeSlot> slots) {
        this.day = first.getParent().getDay();

        this.first = first;
        this.last  = last;
        this.slots = slots;
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
        requireNonNull(other, "block");

        return  (day == other.getDay())
            && (   (last.getEnd() >= other.getFirst().getStart())
                || (last.getEnd() >= other.getFirst().getStart()));

    }

    public Block join(Block other) {
        if (!canJoin(other)) {
            throw new IllegalArgumentException("Cannot join these blocks.");
        }

        TimeSlot nfirst = first.getStart() <= other.first.getStart()
                ? first : other.first;
        TimeSlot nlast  = other.last.getEnd() >= last.getEnd()
                ? other.last : last;

        SortedSet<TimeSlot> nslots = new TreeSet<>(slots);
        nslots.addAll(other.slots);
        return new Block(nfirst, nlast, nslots);
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

    public int      getDay()   { return day;   }
    public TimeSlot getFirst() { return first; }
    public TimeSlot getLast()  { return last;  }

    public Set<TimeSlot> getSlots() { return Collections.unmodifiableSet(slots); }

    @Override
    public int compareTo(Block other) {
        requireNonNull(other, "other");

        int sdiff = first.getStart() - other.first.getStart();
        int ediff =  last.getEnd()   - other.last.getEnd();

        return sdiff != 0 ? sdiff : ediff;
    }
}
