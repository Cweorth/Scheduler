package cz.muni.fi.scheduler.model.domain;

import cz.muni.fi.scheduler.model.domain.management.SlotManager;
import cz.muni.fi.scheduler.data.Person;
import cz.muni.fi.scheduler.extensions.ValueCheck;
import java.util.Collection;
import java.util.stream.Collectors;
import org.cpsolver.ifs.model.Variable;

public abstract class Slot extends Variable<Slot, Ticket> {
    private final SlotManager mngr;
    private final EntryRow    parent;

    protected Slot(EntryRow   parent, SlotManager manager) {
        this.parent = ValueCheck.requireNonNull(parent,  "parent");
        this.mngr   = ValueCheck.requireNonNull(manager, "manager");
    }

    public EntryRow    getParent()      { return parent; }
    public SlotManager getSlotManager() { return mngr;   }

    protected void setDomain(Collection<? extends Person> people) {
        setValues(people.stream().map(p -> new Ticket(this, p)).collect(Collectors.toList()));
    }
}
