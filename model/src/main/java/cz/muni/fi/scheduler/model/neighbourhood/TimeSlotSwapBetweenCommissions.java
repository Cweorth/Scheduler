package cz.muni.fi.scheduler.model.neighbourhood;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import static cz.muni.fi.scheduler.extensions.ValueCheck.requireNonNull;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.SchModel;
import cz.muni.fi.scheduler.model.context.SchModelContext;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import cz.muni.fi.scheduler.utils.Pair;
import java.util.List;
import java.util.Objects;
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

public class TimeSlotSwapBetweenCommissions implements NeighbourSelection<Slot, Ticket> {
    private static final Logger logger = Logger.getLogger("TimeSlotSwapInCommission");

    private Solver solver;

    public TimeSlotSwapBetweenCommissions(DataProperties properties)
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

        List<EntryRow> rows = context.entryRows().collect(Collectors.toList());
        EntryRow row = ToolBox.random(rows);

        // commissaries in the commission
        List<Teacher> collect = row.streamCommissarySlots()
                .map(assignment::getValue)
                .filter(Objects::nonNull)
                .map(ticket -> (Teacher) ticket.getPerson())
                .collect(Collectors.toList());

        // get slots that have _no_ teacher in the commission
        List<Pair<TimeSlot, Student>> slots = row.streamTimeSlots()
                .map(slot -> Pair.of(slot, assignment.getValue(slot)))
                .filter(p -> p.second() != null)
                .map(p -> Pair.of(p.first(), (Student) p.second().getPerson()))
                .filter(p -> p.second().hasThesis()
                        && (p.second().getThesis().getTeachers().stream()
                                .allMatch(t -> !collect.contains(t))))
                .collect(Collectors.toList());

        if (slots.isEmpty()) {
            logger.debug("no slot to move");
            return null;
        }

        // select one slot
        Pair<TimeSlot, Student> move = ToolBox.random(slots);

        // select possible target rows
        List<EntryRow> targets = move.second().getThesis().getTeachers().stream()
                .map(t -> context.memberSlots(t))
                .flatMap(p -> p.map(slot -> slot.getParent()))
                .filter(r -> !r.equals(row))
                .distinct()
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            logger.debug("no target slot");
            return null;
        }

        EntryRow targetrow = ToolBox.random(targets);

        Ticket   oldt = assignment.getValue(move.first());
        TimeSlot targetslot;
        if (targetrow.hasFreeTimeSlot(assignment)) {
            targetslot = targetrow.streamTimeSlots()
                    .filter(slot -> assignment.getValue(slot) == null)
                    .findFirst()
                    .get();

            return new SimpleNeighbour<>(targetslot, oldt);
        }

        targetslot = ToolBox.random(targetrow.streamTimeSlots().collect(Collectors.toList()));
        Ticket newt = assignment.getValue(targetslot);

        return new TimeSlotSwapNeighbour(oldt, newt);
    }

}
