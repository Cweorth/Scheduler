package cz.muni.fi.scheduler.model.criteria;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Thesis;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import cz.muni.fi.scheduler.utils.Pair;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;

/**
 * Penalizes the solution for each block.
 *
 * The goal of this criterion is to minimize the number of blocks
 * each teacher must come to.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class MinimizeBlocksCriterion extends BlockCriterion {
    private final static Logger logger = Logger.getLogger("MinimizeBlocksCriterion");

    public MinimizeBlocksCriterion() {
        //this.setValueUpdateType(ValueUpdateType.AfterUnassignedBeforeAssigned);
    }

    @Override
    public double getValue(Assignment<Slot, Ticket> assignment, Ticket value, Set<Ticket> conflicts) {
        BlockContext context = (BlockContext) getContext(assignment);
        Agenda       agenda  = context.getAgenda();

        if (!value.isTimeSlotTicket())
            return 0;

        Student student = (Student) value.getPerson();
        if (!student.hasThesis())
            return 0;

        Thesis thesis = student.getThesis();

        int diffblocks = thesis.getTeachers().stream()
                .mapToInt(teacher -> agenda.analyzeTimeSlotAssign(teacher, (TimeSlot) value.variable()))
                .sum();

        //--  FIXME  -----------------------------------------------------------
        Ticket current = value.variable().getAssignment(assignment);
        if ((current != null) && !value.equals(current)) {
            if (conflicts == null)
                conflicts = new HashSet<>();
            conflicts.add(current);
        }

        if (conflicts != null) {
            diffblocks += conflicts.stream()
                    .filter(Ticket::isTimeSlotTicket)
                    .map(ticket -> Pair.of(ticket, (Student) ticket.getPerson()))
                    .filter(p -> p.second().hasThesis())
                    .mapToInt(p -> {
                        return p.second().getThesis().getTeachers().stream()
                                .mapToInt(t -> agenda.analyzeTimeSlotUnassign(t, (TimeSlot) p.first().variable()))
                                .sum();
                    }).sum();
        }
        //--  END FIXME  -------------------------------------------------------

        return diffblocks;
    }

}
