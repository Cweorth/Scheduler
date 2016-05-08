package cz.muni.fi.scheduler.model.neighbourhood;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import java.util.HashMap;
import java.util.Map;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Neighbour;

public class SlotAssignNeighbour implements Neighbour<Slot, Ticket> {
    private Ticket ticket;

    public SlotAssignNeighbour(Ticket ticket) {
        this.ticket = requireNonNull(ticket, "ticket");
    }

    @Override
    public void assign(Assignment<Slot, Ticket> assignment, long iteration) {
        assignment.assign(iteration, ticket);
    }

    @Override
    public double value(Assignment<Slot, Ticket> assignment) {
        Ticket current = assignment.getValue(ticket.variable());

        double val = ticket.toDouble(assignment);

        if (current != null) {
            assignment.unassign(0, ticket.variable());
            val -= current.toDouble(assignment);
            assignment.assign(0, current);
        }

        return val;
    }

    @Override
    public Map<Slot, Ticket> assignments() {
        Map<Slot,Ticket> map = new HashMap<>(1);
        map.put(ticket.variable(), ticket);
        return map;
    }
}
