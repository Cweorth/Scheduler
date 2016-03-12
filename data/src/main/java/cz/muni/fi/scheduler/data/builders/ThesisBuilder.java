package cz.muni.fi.scheduler.data.builders;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.data.Thesis;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThesisBuilder {
    private long    id;
    private String  name;
    private Teacher supervisor;
    private Set<Teacher> opponents;

    public ThesisBuilder() {
        opponents = new HashSet<>();
    }

    //<editor-fold desc="[  Getters  ]" defaultstate="collapsed">

    public long         getId()         { return id;         }
    public String       getName()       { return name;       }
    public Teacher      getSupervisor() { return supervisor; }
    public Set<Teacher> getOpponents()  { return opponents;  }

    //</editor-fold>

    //<editor-fold desc="[  Setters  ]" defaultstate="collapsed">

    public ThesisBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public ThesisBuilder setName(String name) {
        this.name = requireNonNull(name, "name");
        return this;
    }

    public ThesisBuilder setSupervisor(Teacher teacher) {
        this.supervisor = requireNonNull(teacher, "teacher");
        return this;
    }

    public ThesisBuilder setOpponents(List<Teacher> teachers) {
        this.opponents = new HashSet<>(teachers);
        return this;
    }

    //</editor-fold>

    public ThesisBuilder addOpponent(Teacher teacher) {
        opponents.add(teacher);
        return this;
    }

    public ThesisBuilder removeOpponent(Teacher teacher) {
        opponents.remove(teacher);
        return this;
    }

    public Thesis value() {
        return new Thesis(id, name, supervisor, opponents);
    }

}
