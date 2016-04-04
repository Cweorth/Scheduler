package cz.muni.fi.scheduler.model.domain;

import cz.muni.fi.scheduler.data.Person;
import cz.muni.fi.scheduler.extensions.ValueCheck;
import java.util.Collection;
import java.util.stream.Collectors;
import org.cpsolver.ifs.model.Variable;

/**
 * Represents the model variable, a slot either for students or commission members.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public abstract class Slot extends Variable<Slot, Ticket> {
    private final EntryRow  parent;

    protected Slot(EntryRow parent) {
        this.parent = ValueCheck.requireNonNull(parent,  "parent");
    }

    public EntryRow getParent() { return parent; }

    protected void setDomain(Collection<? extends Person> people) {
        setValues(people.stream().map(p -> new Ticket(this, p)).collect(Collectors.toList()));
    }
}
