package cz.muni.fi.scheduler.model.criteria;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.SchModel;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.criteria.AbstractCriterion;
import org.cpsolver.ifs.model.Model;

public abstract class BlockCriterion extends AbstractCriterion<Slot, Ticket> {

    protected SchModel model;

    @Override
    public ValueContext createAssignmentContext(Assignment<Slot, Ticket> assignment) {
        return new BlockContext(assignment, model);
    }

    @Override
    public void setModel(Model<Slot, Ticket> model) {
        if (!(model instanceof SchModel)) {
            throw new IllegalArgumentException("The model must be an instance of SchModel");
        }

        super.setModel(model);
        this.model = (SchModel) model;
    }

    public final class BlockContext extends ValueContext {
        private final Logger   logger = Logger.getLogger(BlockContext.class);
        private final SchModel model;

        public BlockContext(Assignment<Slot, Ticket> assignment, SchModel model) {
            super(requireNonNull(assignment, "assignment"));
            this.model = requireNonNull(model, "model");

            assignment.assignedValues().forEach(
                    v -> assigned(assignment, v)
            );
        }

        public Agenda agenda(Assignment<Slot, Ticket> assignment) {
            return model.getContext(assignment).getAgenda();
        }
/*
        @Override
        public void assigned(Assignment<Slot, Ticket> assignment, Ticket ticket) {
            super.assigned(assignment, ticket);

            if (!ticket.isTimeSlotTicket()) {
                agenda(assignment).markMemberSlot((Teacher) ticket.getPerson(), (MemberSlot) ticket.variable());
            } else {
                Student student = (Student) ticket.getPerson();

                if (!student.hasThesis())
                    return;

                student.getThesis().getTeachers()
                        .forEach(teacher -> agenda(assignment).markTimeSlot(teacher, (TimeSlot) ticket.variable()));
            }

        }

        @Override
        public void unassigned(Assignment<Slot, Ticket> assignment, Ticket ticket) {
            super.unassigned(assignment, ticket);

            if (!ticket.isTimeSlotTicket()) {
                agenda(assignment).unmarkMemberSlot((Teacher) ticket.getPerson(), (MemberSlot) ticket.variable());
            } else {
                Student student = (Student) ticket.getPerson();

                if (!student.hasThesis())
                    return;

                student.getThesis().getTeachers()
                        .forEach(teacher -> agenda(assignment).unmarkTimeSlot(teacher, (TimeSlot) ticket.variable()));
            }

        }
*/
    }
}