package cz.muni.fi.scheduler.model.neighbourhood;

import cz.muni.fi.scheduler.data.Person;
import cz.muni.fi.scheduler.data.Teacher;
import static cz.muni.fi.scheduler.extensions.ValueCheck.requireNonNull;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.Block;
import cz.muni.fi.scheduler.model.SchModel;
import cz.muni.fi.scheduler.model.context.SchModelContext;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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

public class RandomCommissarySwap implements NeighbourSelection<Slot, Ticket> {
    private static final Logger logger = Logger.getLogger("TimeSlotSwapInCommission");

    private Solver solver;

    public RandomCommissarySwap(DataProperties properties)
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

        List<Ticket> tickets = context.entryRows()
                .flatMap(EntryRow::streamMemberSlots)
                .map(assignment::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (tickets.isEmpty()) {
            logger.debug("no tickets to select from");
            return null;
        }

        Ticket oldTicket = ToolBox.random(tickets);

        Set<Person> tabu = oldTicket.variable().getParent().streamMemberSlots()
                .map(assignment::getValue)
                .filter(Objects::nonNull)
                .map(Ticket::getPerson)
                .collect(Collectors.toSet());

        List<Ticket> candidates = oldTicket.variable().values(assignment).stream()
                .filter(value -> !tabu.contains(value.getPerson()))
                .collect(Collectors.toList());

        SortedMap<Long, List<Ticket>> usages = new TreeMap<>();
        candidates.stream()
                .forEach(ticket -> {
                    long count = agenda.getBlocks((Teacher) ticket.getPerson()).values().stream()
                        .flatMap(List::stream)
                        .filter(Block::isSpanning)
                        .count();
                    usages.getOrDefault(count, new ArrayList<>()).add(ticket);
                });

        Ticket candidate = usages.entrySet().stream()
                .filter(kv -> !kv.getValue().isEmpty())
                .map(kv -> ToolBox.random(kv.getValue()))
                .findFirst()
                .orElse(null);

        if (candidate == null) {
            logger.debug("failed to select a candidate");
            return null;
        }

        return new SimpleNeighbour<>(candidate.variable(), candidate);
    }
}
