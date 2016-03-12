package cz.muni.fi.scheduler.model.constraints;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.extensions.ValueCheck;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.management.SlotManager;
import java.util.Set;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.GlobalConstraint;

public class UniqueStudentTicketConstraint extends GlobalConstraint<Slot, Ticket> {
    private final SlotManager mngr;

    public UniqueStudentTicketConstraint(SlotManager manager) {
        this.mngr = ValueCheck.requireNonNull(manager, "manager");
    }

    @Override
    public void computeConflicts(Assignment<Slot, Ticket> assignment, Ticket value, Set<Ticket> conflicts) {
        if (!(value.getPerson() instanceof Student))
            return;

        mngr.studentSlots(value)
                .map(assignment::getValue)
                .forEach(conflicts::add);
    }

}
