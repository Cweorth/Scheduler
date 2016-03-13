package cz.muni.fi.scheduler.model.neighbourhood;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.heuristics.NeighbourSelection;
import org.cpsolver.ifs.model.LazySwap;
import org.cpsolver.ifs.model.Neighbour;
import org.cpsolver.ifs.model.SimpleNeighbour;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;

/**
 * Experimental local change that swaps defenses in a single commission.
 *
 * The algorithm tries to swap a random teacher in a way that decreases the number
 * of blocks for the given teacher.
 *
 * @author cweorth
 */
public class TimeSlotSwapInCommission implements NeighbourSelection<Slot, Ticket> {
    private static final Logger logger = Logger.getLogger("TimeSlotSwapInCommission");

    private Solver solver;

    private static class TestNeighbour implements Neighbour<Slot, Ticket> {
        private final LazySwap<Slot, Ticket> lswp;

        public TestNeighbour(Ticket v1, Ticket v2) {
            logger.debug("created test neighbour");

            lswp = new LazySwap<>(v1, v2);
            lswp.setAcceptanceCriterion((a,b,c) -> { logger.debug("accept?"); return true; });
        }

        @Override
        public void assign(Assignment<Slot, Ticket> assignment, long iteration) {
            logger.debug("called assign");
            lswp.assign(assignment, iteration);
        }

        @Override
        public double value(Assignment<Slot, Ticket> assignment) {
            double value = lswp.value(assignment);
            logger.debug("called value = " + value);
            return value;
        }

        @Override
        public Map<Slot, Ticket> assignments() {
            Map<Slot, Ticket> assignments = lswp.assignments();
            logger.debug("called assignments" + ToolBox.dict2string(assignments, 2));
            return assignments;
        }
    }

    public TimeSlotSwapInCommission(DataProperties properties)
    { }

    @Override
    public void init(Solver<Slot, Ticket> solver) {
        this.solver = requireNonNull(solver, "solver");

        logger.debug("initialized");
    }

    @Override
    public Neighbour<Slot, Ticket> selectNeighbour(Solution<Slot, Ticket> solution) {
        logger.debug("called");
        Assignment<Slot, Ticket> assignment = solution.getAssignment();

        Slot fst = ToolBox.random(
                assignment.assignedVariables().stream()
                .filter(var -> var.getAssignment(assignment).isTimeSlotTicket())
                .collect(Collectors.toList())
        );

        if (fst == null) {
            logger.debug("no variables are assigned yet");
            return null;
        }

        Student student = (Student) assignment.getValue(fst).getPerson();

        if (!student.hasThesis()) {
            logger.debug("oops, this student does not have a thesis");
            return null;
        }

        Teacher        teacher = ToolBox.random(student.getThesis().getTeachers());
        EntryRow       row     = fst.getParent();
        List<TimeSlot> slots   = row.streamTimeSlots().collect(Collectors.toList());
        List<TimeSlot> improv  = new ArrayList<>();

        // bitmaps of teacher presence
        int bitmap[] = new int[slots.size()];

        // comute bitmap for the teacher
        for (int ix = 0; ix < slots.size(); ++ix) {
            Slot slot = slots.get(ix);

            if (slot == fst) {
                bitmap[ix] = 0;
                continue;
            }

            Ticket  ticket = assignment.getValue(slots.get(ix));
            if (ticket == null) {
                bitmap[ix] = 0;
                continue;
            }

            Student other  = (Student) ticket.getPerson();

            if (other.hasThesis() && other.getThesis().hasTeacher(teacher)) {
                bitmap[ix] = 1;
            }
        }

        // compute preference array
        for (int ix = 0; ix < slots.size(); ++ix) {
            if (slots.get(ix).equals(fst)) {
                continue;
            }

            int l = ix ==                0 ? 0 : bitmap[ix - 1];
            int r = ix == slots.size() - 1 ? 0 : bitmap[ix + 1];

            for (int p = 0; p < l + r; ++p) {
                improv.add(slots.get(ix));
            }
        }

        Slot snd = ToolBox.random(improv);

        if (snd == null) {
            logger.debug("could not find a better timeslot");
            return null;
        }

        logger.debug("found a better timeslot");

        //LazySwap lazySwap = new LazySwap(assignment.getValue(fst), assignment.getValue(snd));
        //lazySwap.setAcceptanceCriterion((a, n, v) -> { logger.info("called criterion"); return true; } );
        //return lazySwap;

        Neighbour neighbour;
        if (assignment.getValue(snd) == null) {
            neighbour = new SimpleNeighbour(snd, assignment.getValue(fst), null);
        } else {
            neighbour = new TestNeighbour(assignment.getValue(fst), assignment.getValue(snd));
        }

        return neighbour;
    }

}
