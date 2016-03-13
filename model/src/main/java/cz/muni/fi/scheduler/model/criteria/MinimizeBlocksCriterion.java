package cz.muni.fi.scheduler.model.criteria;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Thesis;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.criteria.AbstractCriterion;

/**
 * Penalizes the solution for each block.
 *
 * The goal of this criterion is to minimize the number of blocks
 * each teacher must come to.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class MinimizeBlocksCriterion extends AbstractCriterion<Slot, Ticket> {
    private final static Logger logger = Logger.getLogger("MinimizeBlocksCriterion");

    private final Agenda agenda;

    public MinimizeBlocksCriterion(Agenda agenda) {
        this.agenda  = requireNonNull(agenda, "agenda");
    }

    @Override
    public double getValue(Assignment<Slot, Ticket> assignment, Ticket value, Set<Ticket> conflicts) {
        if (!value.isTimeSlotTicket())
            return 0;

        Student student = (Student) value.getPerson();
        if (!student.hasThesis())
            return 0;

        Thesis thesis = student.getThesis();

        int diffblocks = Stream.concat(thesis.getOpponents().stream(), Stream.of(thesis.getSupervisor()))
                .mapToInt(teacher -> agenda.analyzeTimeSlot(teacher, (TimeSlot) value.variable()))
                .sum();

        //if (diffblocks > 0) {
        //    logger.debug("found diffblocks of " + diffblocks + "; my weight is " + iWeight);
        //}

        return diffblocks;
    }

}
