package cz.muni.fi.scheduler.model;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.model.context.SchModelContext;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import java.util.ArrayList;
import java.util.List;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.context.ModelWithContext;

public class SchModel extends ModelWithContext<Slot, Ticket, SchModelContext>{
    private final List<EntryRow> entryRows;

    public SchModel() {
        entryRows = new ArrayList<>();
        setContextUpdateType(ContextUpdateType.AfterUnassignedAfterAssigned);
    }

    public void addEntryRow(EntryRow row) {
        entryRows.add(requireNonNull(row, "row"));
    }

    @Override
    public SchModelContext createAssignmentContext(Assignment<Slot, Ticket> assignment) {
        SchModelContext context = new SchModelContext();
        entryRows.stream().forEach(context::addEntryRow);
        return context;
    }

    @Override
    public double getTotalValue(Assignment<Slot, Ticket> assignment) {
        //return getCriteria().stream().mapToDouble((c) -> c.getValue(assignment)).sum();
        return getContext(assignment).getAgenda().blockSum();
    }

}
