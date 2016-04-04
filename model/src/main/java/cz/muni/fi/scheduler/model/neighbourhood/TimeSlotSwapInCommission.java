package cz.muni.fi.scheduler.model.neighbourhood;

import cz.muni.fi.scheduler.data.Student;
import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.SchModel;
import cz.muni.fi.scheduler.model.context.SchModelContext;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import cz.muni.fi.scheduler.utils.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.heuristics.NeighbourSelection;
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
 */
public class TimeSlotSwapInCommission implements NeighbourSelection<Slot, Ticket> {
    private static final Logger logger = Logger.getLogger("TimeSlotSwapInCommission");

    private Solver solver;

    public TimeSlotSwapInCommission(DataProperties properties)
    { }

    @Override
    public void init(Solver<Slot, Ticket> solver) {
        this.solver = requireNonNull(solver, "solver");

        logger.debug("initialized");
    }

    @Override
    public Neighbour<Slot, Ticket> selectNeighbour(Solution<Slot, Ticket> solution) {
        final SchModel                 model      = (SchModel) solution.getModel();
        final Assignment<Slot, Ticket> assignment = solution.getAssignment();
        final SchModelContext          context    = model.getContext(assignment);
        final Agenda                   agenda     = context.getAgenda();

        List<EntryRow> collect = context.entryRows().collect(Collectors.toList());
        EntryRow row = ToolBox.random(collect);

        if (row == null) {
            logger.error("null EntryRow selected");
            return null;
        }

        List<Pair<TimeSlot, TimeSlot>> validmoves = new ArrayList<>();

        row.streamTimeSlots().forEach(slot -> {
            final Ticket ticket = assignment.getValue(slot);

            if (ticket == null)
                return;

            Student student = (Student) ticket.getPerson();
            if (!student.hasThesis())
                return;

            row.streamTimeSlots()
                    .filter(refslot -> refslot.compareTo(slot) > 0)
                    .forEach(refslot -> {
                        int expected = student.getThesis().getTeachers().stream()
                                .mapToInt(t -> agenda.analyzeTimeSlotAssign(t, refslot))
                                .sum();

                        if (expected > 0) {
                            validmoves.add(Pair.of(slot, refslot));
                        }
                    });
        });

        if (validmoves.isEmpty()) {
            logger.debug("no valid moves for row " + row.getId());
            return null;
        }

        Pair<TimeSlot, TimeSlot> move = ToolBox.random(validmoves);
        Ticket oldt = assignment.getValue(move.first());
        Ticket newt = assignment.getValue(move.second());

        return newt == null
                ? new SimpleNeighbour<>(
                        move.second(),
                        new Ticket(move.second(), oldt.getPerson())
                  )
                : new TimeSlotSwapNeighbour(oldt, newt);
    }

}
