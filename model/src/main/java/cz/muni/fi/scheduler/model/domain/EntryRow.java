package cz.muni.fi.scheduler.model.domain;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Commission;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.model.Configuration;
import cz.muni.fi.scheduler.model.domain.management.SlotManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cpsolver.ifs.assignment.Assignment;

/**
 * Represents a single commission and its exams on a specific day.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class EntryRow {
    private static long ID_SEQ = 0L;

    private final long id = ID_SEQ++;

    private final Configuration    config;
    private final SlotManager      mngr;

    private final int              day;
    private final MemberSlot       chairman;
    private final List<MemberSlot> members;
    private final List<TimeSlot>   timeslots;

    public EntryRow(int day, SlotManager manager, Configuration config) {
        this.config = requireNonNull(config,  "config");
        this.mngr   = requireNonNull(manager, "manager");
        this.day    = requireNonNegative(day, "day");

        chairman  = new MemberSlot(this, mngr);
        members   = Stream.generate(() -> new MemberSlot(this, mngr)).limit(2).collect(Collectors.toList());
        timeslots = new LinkedList<>(Arrays.asList(new TimeSlot(0, config.fullExamLength, this, manager)));
    }

    public long getId()    { return id;  }
    public int  getDay()   { return day; }
    public int  getStart() { return timeslots.get(0).getStart();                  }
    public int  getEnd()   { return timeslots.get(timeslots.size() - 1).getEnd(); }

    public Stream<TimeSlot>   streamTimeSlots()       { return timeslots.stream(); }
    public Stream<MemberSlot> streamMemberSlots()     { return members.stream();   }
    public Stream<MemberSlot> streamChairmanSlot()    { return Stream.of(chairman); }

    public Stream<TimeSlot> streamFullSlots() {
        return streamTimeSlots().filter(slot -> slot.getLength() == config.fullExamLength);
    }

    public Stream<TimeSlot> streamShortSlots() {
        return streamTimeSlots().filter(slot -> slot.getLength() == config.shortExamLength);
    }

    public Stream<MemberSlot> streamCommissarySlots() {
        return Stream.concat(Stream.of(chairman), members.stream());
    }

    public Stream<Slot> streamAllSlots() {
        return Stream.concat(Stream.concat(Stream.of(chairman), members.stream()), timeslots.stream());
    }

    public MemberSlot getChairmanSlot()  { return chairman; }

    public boolean hasFreeTimeSlot(Assignment<Slot, Ticket> assignment) {
        return streamTimeSlots().map(assignment::getValue).anyMatch(Objects::isNull);
    }

    public boolean hasFreeMemberSlot(Assignment<Slot, Ticket> assignment) {
        return members.stream().map(assignment::getValue).anyMatch(Objects::isNull);
    }

    public boolean hasFreeChairmanSlot(Assignment<Slot, Ticket> assignment) {
        return assignment.getValue(chairman) != null;
    }

    public boolean hasFreeCommissarySlot(Assignment<Slot, Ticket> assignment) {
        return hasFreeChairmanSlot(assignment) || hasFreeMemberSlot(assignment);
    }

    public boolean hasFreeSlot(Assignment<Slot, Ticket> assignment) {
        return hasFreeCommissarySlot(assignment) || hasFreeTimeSlot(assignment);
    }

    public void extendBack() {
        if (timeslots.isEmpty()) {
            timeslots.add(new TimeSlot(0, config.fullExamLength, this, mngr));
        } else {
            final TimeSlot last = timeslots.get(timeslots.size() - 1);
            timeslots.add(new TimeSlot(last.getEnd(), last.getLength(), this, mngr));
        }
    }

    public Commission getCommission(Assignment<Slot, Ticket> assignment, Configuration config) {
        Set<Teacher> wmembers;
        Teacher      wchairman;

        wmembers = members.stream().map(Optional::of)
                .map(v -> v.map(assignment::getValue))
                .map(v -> v.map(Ticket::getPerson))
                .filter(Optional::isPresent)
                .map(v -> (Teacher) v.get())
                .collect(Collectors.toCollection(HashSet::new));

        wchairman = Optional.ofNullable(assignment.getValue(chairman))
                .map(t -> (Teacher) t.getPerson())
                .orElse(null);

        return new Commission(config.dates.get(day),
                config.dayStart.plusMinutes(getStart()),
                config.dayStart.plusMinutes(getEnd()),
                wchairman, wmembers);
    }
}