package cz.muni.fi.scheduler.data;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Thesis entity.
 * Represents bachelor's or master's thesis that is supposed to be defended on final exam.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class Thesis {
    private final long          id;
    private final String        name;
    private final Teacher       supervisor;
    private final List<Teacher> opponents;

    private Integer       memHash; // memoized hash code
    private List<Teacher> memAll;  // list of all teachers

    public Thesis(long id, String name, Teacher supervisor, Collection<Teacher> opponents) {
        this.id         = requireNonNegative(id,     "Thesis.id");
        this.name       = requireNonNull(name,       "Thesis.name");
        this.supervisor = requireNonNull(supervisor, "Thesis.supervisor");

        this.opponents  = new ArrayList<>(requireNonNull(opponents, "Thesis.opponents"));

        requirePositive(this.opponents.size(), "Thesis.opponents.size");

        if (opponents.contains(supervisor))
            throw new IllegalArgumentException("The thesis supervisor cannot be an opponent of the same thesis.");
    }

    //<editor-fold defaultstate="collapsed" desc="[  Getters  ]">

    public long    getId()         { return id;         }
    public String  getName()       { return name;       }
    public Teacher getSupervisor() { return supervisor; }

    public List<Teacher> getOpponents() {
        return Collections.unmodifiableList(opponents);
    }

    public List<Teacher> getTeachers() {
        if (memAll == null) {
            memAll = new ArrayList<>(opponents.size() + 1);

            memAll.add(supervisor);
            memAll.addAll(opponents);
        }

        return memAll;
    }

    public boolean hasTeacher(Teacher teacher) {
        return (supervisor.equals(teacher) || opponents.contains(teacher));
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="[  HashCode, Equals & ToString  ]">

    @Override
    public int hashCode() {
        if (memHash == null)
            memHash =  Objects.hash(id, name, supervisor, opponents);

        return memHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Thesis) || (obj.hashCode() != hashCode()))
            return false;

        final Thesis other = (Thesis) obj;

        return (id         == other.id)
            && (      name.equals(other.name)      )
            && (supervisor.equals(other.supervisor))
            && ( opponents.equals(other.opponents) );
    }

    @Override
    public String toString() {
        List<Long> opponentIds = opponents.stream().map(Teacher::getId).collect(Collectors.toList());

        return String.format(
                "Thesis { id: %d, name: \"%s\", supervisor: %d, opponents: [ %s ] }",
                id, name, supervisor.getId(), opponentIds
        );
    }

    //</editor-fold>

}
