package cz.muni.fi.scheduler.data;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Commission {
    private final LocalDate date;
    private final LocalTime start;
    private final LocalTime end;

    private final Teacher      chairman;
    private final Set<Teacher> members;

    private Integer memHash;

    public Commission(LocalDate date, LocalTime start, LocalTime end,
                      Teacher chairman, Collection<Teacher> members) {
        this.date  = requireNonNull(date,  "Commission.date");
        this.start = requireNonNull(start, "Commission.start");
        this.end   = requireNonNull(end,   "Commission.end");

        this.chairman = requireNonNull(chairman, "Commission.chairman");
        this.members  = new HashSet<>(requireNonNull(members, "Commission.members"));

        this.members.add(chairman);
    }

    //<editor-fold desc="[  Getters  ]" defaultstate="collapsed">

    public LocalDate getDate()       { return date;     }
    public LocalTime getStart()      { return start;    }
    public LocalTime getEnd()        { return end;      }
    public Teacher   getChairman()   { return chairman; }

    public Set<Teacher> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="[  HashCode, Equals & ToString  ]">

    @Override
    public int hashCode() {
        if (memHash == null)
            memHash = Objects.hash(date, start, end, chairman, members);

        return memHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Commission) || (obj.hashCode() != hashCode()))
            return false;

        final Commission other = (Commission) obj;

        return Objects.equals(date,     other.date)
            && Objects.equals(start,    other.start)
            && Objects.equals(end,      other.end)
            && Objects.equals(chairman, other.chairman)
            && Objects.equals(members,  other.members);
    }

    @Override
    public String toString() {
        return "Commission { date: " + date + ", start: " + start + ", end: " + end +
                ", chairman: " + chairman.getId() + ", members: " +
                members.stream().map(Teacher::getId).collect(Collectors.toList()) + " }";
    }

    //</editor-fold>

}

