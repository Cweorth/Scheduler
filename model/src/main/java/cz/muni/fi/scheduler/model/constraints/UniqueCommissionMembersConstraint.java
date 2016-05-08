package cz.muni.fi.scheduler.model.constraints;

import cz.muni.fi.scheduler.model.SchModel;
import cz.muni.fi.scheduler.model.context.SchModelContext;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import java.util.Set;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.GlobalConstraint;
import org.cpsolver.ifs.model.Model;

/**
 * The constraint that makes sure each commission contains unique teachers.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class UniqueCommissionMembersConstraint extends GlobalConstraint<Slot, Ticket> {
    private static final Logger logger = Logger.getLogger(UniqueCommissionMembersConstraint.class);

    private SchModelContext getContext(Assignment<Slot, Ticket> assignment) {
        final SchModel schModel = (SchModel) getModel();
        return schModel.getContext(assignment);
    }

    @Override
    public void setModel(Model model) {
        if (!(model instanceof SchModel))
            throw new IllegalArgumentException("Model is not SchModel");
        super.setModel(model);
    }

    @Override
    public void computeConflicts(Assignment<Slot, Ticket> assignment, Ticket value, Set<Ticket> conflicts) {
        if (value.isTimeSlotTicket())
            return;

        getContext(assignment).memberSlots(value)
                .filter(slot -> slot.getParent().getDay() == value.variable().getParent().getDay())
                .map(assignment::getValue)
                .forEach(conflicts::add);
    }
}
