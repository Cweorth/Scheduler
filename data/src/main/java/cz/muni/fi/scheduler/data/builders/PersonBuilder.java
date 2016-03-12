package cz.muni.fi.scheduler.data.builders;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class PersonBuilder<ChildType extends PersonBuilder> {
    private long   id;
    private String name;
    private String surname;
    private List<String> prefixTitles;
    private List<String> suffixTitles;

    public PersonBuilder() {
        prefixTitles = new ArrayList<>();
        suffixTitles = new ArrayList<>();
    }

    //<editor-fold desc="[  Getters  ]" defaultstate="collapsed">

    public long   getId()      { return id;      }
    public String getName()    { return name;    }
    public String getSurname() { return surname; }

    public List<String> getPrefixTitles() { return Collections.unmodifiableList(prefixTitles); }
    public List<String> getSuffixTitles() { return Collections.unmodifiableList(suffixTitles); }

    //</editor-fold>

    //<editor-fold desc="[  Setters  ]" defaultstate="collapsed">

    public ChildType setId(long id) {
        this.id = id;
        return (ChildType) this;
    }

    public ChildType setName(String name) {
        this.name = requireNonNull(name, "name");
        return (ChildType) this;
    }

    public ChildType setSurname(String surname) {
        this.surname = requireNonNull(surname, "surname");
        return (ChildType) this;
    }

    public ChildType addPrefixTitle(String title) {
        this.prefixTitles.add(requireNonNull(title, "title"));
        return (ChildType) this;
    }

    public ChildType addSuffixTitle(String title) {
        this.suffixTitles.add(requireNonNull(title, "title"));
        return (ChildType) this;
    }

    public ChildType setPrefixTitles(List<String> titles) {
        this.prefixTitles = new ArrayList<>(requireNonNull(titles, "titles"));
        return (ChildType) this;
    }

    public ChildType setSuffixTitles(List<String> titles) {
        this.suffixTitles = new ArrayList<>(requireNonNull(titles, "titles"));
        return (ChildType) this;
    }

    //</editor-fold>
}
