package cz.muni.fi.scheduler.model.criteria;

import cz.muni.fi.scheduler.data.Student;
import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.criteria.AbstractCriterion;

public abstract class BlockCriterion extends AbstractCriterion<Slot, Ticket> {

    @Override
    public ValueContext createAssignmentContext(Assignment<Slot, Ticket> assignment) {
        return new BlockContext(assignment);
    }

    public final class BlockContext extends ValueContext {
        private final Logger logger = Logger.getLogger(BlockContext.class);
        private final Agenda agenda;

        public BlockContext(Assignment<Slot, Ticket> assignment) {
            super(requireNonNull(assignment, "assignment"));
            agenda = new Agenda();

            assignment.assignedValues().forEach(
                    v -> assigned(assignment, v)
            );
        }

        @Override
        public void assigned(Assignment<Slot, Ticket> assignment, Ticket ticket) {
            super.assigned(assignment, ticket);

            if (!ticket.isTimeSlotTicket())
                return;

            Student student = (Student) ticket.getPerson();

            if (!student.hasThesis())
                return;

            student.getThesis().getTeachers()
                    .forEach(teacher -> agenda.markTimeSlot(teacher, (TimeSlot) ticket.variable()));
        }

        @Override
        public void unassigned(Assignment<Slot, Ticket> assignment, Ticket ticket) {
            if (!ticket.isTimeSlotTicket())
                return;

            Student student = (Student) ticket.getPerson();

            if (!student.hasThesis())
                return;

            student.getThesis().getTeachers()
                    .forEach(teacher -> agenda.unmarkTimeSlot(teacher, (TimeSlot) ticket.variable()));

            super.unassigned(assignment, ticket);
        }

        public Agenda getAgenda() { return agenda; }
    }

}
