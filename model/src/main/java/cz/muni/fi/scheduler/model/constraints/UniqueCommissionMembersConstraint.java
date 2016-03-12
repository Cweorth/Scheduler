package cz.muni.fi.scheduler.model.constraints;

import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.extensions.ValueCheck;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.management.SlotManager;
import java.util.Set;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.GlobalConstraint;

public class UniqueCommissionMembersConstraint extends GlobalConstraint<Slot, Ticket> {
    private final SlotManager mngr;

    public UniqueCommissionMembersConstraint(SlotManager manager) {
        this.mngr = ValueCheck.requireNonNull(manager, "manager");
    }

    @Override
    public void computeConflicts(Assignment<Slot, Ticket> assignment, Ticket value, Set<Ticket> conflicts) {
        if (!(value.getPerson() instanceof Teacher))
            return;

        mngr.memberSlots(value)
                .map(assignment::getValue)
                .forEach(conflicts::add);
    }

}
