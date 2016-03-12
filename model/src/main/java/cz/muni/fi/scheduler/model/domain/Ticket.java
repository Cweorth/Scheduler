package cz.muni.fi.scheduler.model.domain;

import cz.muni.fi.scheduler.data.Person;
import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.extensions.ValueCheck;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Value;

public class Ticket extends Value<Slot, Ticket> {
    private final Person  person;
    private final boolean hasTimeSlot;

    public Ticket(Slot slot, Person person) {
        super(ValueCheck.requireNonNull(slot, "slot"));
        this.person      = ValueCheck.requireNonNull(person, "person");
        this.hasTimeSlot = (slot instanceof TimeSlot);

        // the slot is either a TimeSlot and we need the person to be a Student
        if (hasTimeSlot && !(person instanceof Student))
            throw new IllegalArgumentException("Only a Student can be assigned to a TimeSlot.");

        // or this is a regular slot for commission members
        if (!hasTimeSlot && !(person instanceof Teacher))
            throw new IllegalArgumentException("Only a Teacher can be assigned to a regular Slot.");
    }

    //<editor-fold defaultstate="collapsed" desc="[  Getters  ]">

    public Person  getPerson()        { return person;      }
    public boolean isTimeSlotTicket() { return hasTimeSlot; }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="[  HashCode, Equals & ToString  ]">

    @Override
    public int hashCode() {
        return 41 * person.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Ticket) || (obj.hashCode() != hashCode()))
            return false;

        final Ticket other = (Ticket) obj;

        return person.equals(other.person);
    }

    @Override
    public String toString() {
        return "Ticket { " + (hasTimeSlot ? "timeSlot, " : "") +
                "slot: " + variable() + "; " + person + '}';
    }

    //</editor-fold>

    @Override
    public double toDouble(Assignment<Slot, Ticket> assignment) {
        double value = variable().getModel().getCriteria().stream()
                .map((criterion) -> criterion.getWeightedValue(assignment, this, null))
                .reduce(0.0, (accumulator, item) -> accumulator + item);

        return value;
    }
}
