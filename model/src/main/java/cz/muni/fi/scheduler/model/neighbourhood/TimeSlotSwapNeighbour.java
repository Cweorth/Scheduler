package cz.muni.fi.scheduler.model.neighbourhood;

import cz.muni.fi.scheduler.data.Person;
import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import java.util.HashMap;
import java.util.Map;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Neighbour;

public class TimeSlotSwapNeighbour implements Neighbour<Slot, Ticket> {
    private final Ticket t1;
    private final Ticket t2;

    private Ticket s2t1;
    private Ticket s1t2;

    public TimeSlotSwapNeighbour(Ticket t1, Ticket t2) {
        this.t1 = requireNonNull(t1, "t1");
        this.t2 = requireNonNull(t2, "t2");

        if (!t1.isTimeSlotTicket())
            throw new IllegalArgumentException("t1 must be a TimeSlot ticket");
        if (!t2.isTimeSlotTicket())
            throw new IllegalArgumentException("t2 must be a TimeSlot ticket");
    }

    public boolean canApply(Assignment<Slot, Ticket> assignment) {
        TimeSlot s1 = (TimeSlot) t1.variable();
        TimeSlot s2 = (TimeSlot) t2.variable();
        Person   p1 = t1.getPerson();
        Person   p2 = t2.getPerson();

        s2t1 = s2.values(assignment).stream()
                .filter(t -> t.getPerson().equals(p1))
                .findFirst()
                .orElse(null);

        s1t2 = s1.values(assignment).stream()
                .filter(t -> t.getPerson().equals(p2))
                .findFirst()
                .orElse(null);

        return s1t2 != null && s2t1 != null;
    }

    @Override
    public void assign(Assignment<Slot, Ticket> assignment, long iteration) {
        assignment.unassign(iteration, t1.variable());
        assignment.unassign(iteration, t2.variable());
        assignment.assign(iteration, s1t2);
        assignment.assign(iteration, s2t1);
    }

    @Override
    public double value(Assignment<Slot, Ticket> assignment) {
        if (!canApply(assignment))
            throw new IllegalArgumentException("Cannot apply this neighbour");

        return s1t2.toDouble(assignment) + s2t1.toDouble(assignment);
    }

    @Override
    public Map<Slot, Ticket> assignments() {
        Map<Slot, Ticket> m = new HashMap<>();
        m.put(s1t2.variable(), s1t2);
        m.put(s2t1.variable(), s2t1);
        return m;
    }

}
