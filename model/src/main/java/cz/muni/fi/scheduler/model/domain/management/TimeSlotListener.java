package cz.muni.fi.scheduler.model.domain.management;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import java.util.stream.Stream;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.VariableListener;

public class TimeSlotListener implements VariableListener<Ticket> {
    private final TimeSlot    slot;
    private final SlotManager mngr;

    public TimeSlotListener(TimeSlot slot, SlotManager manager) {
        this.slot = requireNonNull(slot,    "slot"   );
        this.mngr = requireNonNull(manager, "manager");
    }

    @Override
    public void variableAssigned(Assignment<?, Ticket> assignment, long iteration, Ticket value) {
        mngr.addTimeSlot(slot, value);

        final Student student = (Student) value.getPerson();

        if (student.hasThesis()) {
            Stream.concat(Stream.of(student.getThesis().getSupervisor()), student.getThesis().getOpponents().stream())
                    .forEach(t -> mngr.markDefence(slot, t));
        }
    }

    @Override
    public void variableUnassigned(Assignment<?, Ticket> assignment, long iteration, Ticket value) {
        final Student student = (Student) value.getPerson();

        if (student.hasThesis()) {
            Stream.concat(Stream.of(student.getThesis().getSupervisor()), student.getThesis().getOpponents().stream())
                    .forEach(t -> mngr.unmarkDefence(slot, t));
        }

        mngr.removeTimeSlot(slot, value);
    }

    @Override
    public void valueRemoved(long iteration, Ticket value) {
        variableUnassigned(null, iteration, value);
    }

}
