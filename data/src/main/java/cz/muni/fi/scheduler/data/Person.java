package cz.muni.fi.scheduler.data;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Base class for {@link Student} and {@link Teacher}.
 * Attributes of this class should be enough to identify a person; that is,
 * <ul>
 *     <li>{@code id}, an unique ID,</li>
 *     <li>{@code name} and {@code surname},</li>
 *     <li>optional {@code fullName} that contains titles</li>
 * </ul>
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public abstract class Person {
    private final long         id;
    private final String       name;
    private final String       surname;
    private final List<String> prefixTitles;
    private final List<String> suffixTitles;

    private String  memFullName; // memoized full name
    private Integer memHash;     // memoized hash code

    // titles must contain at least one character, a-z, A-Z or a dot and nothing else
    private static final Pattern TITLE_PATTERN = Pattern.compile("^[a-zA-Z\\.]+$");

    public Person(long id, String name, String surname) {
        this(id, name, surname, null, null);
    }

    /**
     * Initializes class attributes.
     * @param id            positive and unique number assigned to a person
     * @param name          given name
     * @param surname       family name
     * @param prefixTitles  titles written before person's name, empty if @{code null}
     * @param suffixTitles  titles written after person's name, empty if @{code null}
     */
    public Person(long id, String name, String surname,
                  Collection<String> prefixTitles, Collection<String> suffixTitles) {
        this.id       = requireNonNegative(id,  "Person.id");
        this.name     = requireNonNull(name,    "Person.name");
        this.surname  = requireNonNull(surname, "Person.surname");

        this.prefixTitles = prefixTitles == null ? new ArrayList<>(0) : new ArrayList<>(prefixTitles);
        this.suffixTitles = suffixTitles == null ? new ArrayList<>(0) : new ArrayList<>(suffixTitles);

        this.prefixTitles.stream().forEach(title -> requireMatch(TITLE_PATTERN, title, "prefixTitle"));
        this.suffixTitles.stream().forEach(title -> requireMatch(TITLE_PATTERN, title, "suffixTitle"));
    }

    //<editor-fold defaultstate="collapsed" desc="[  Getters  ]">

    public long   getId()       { return id;       }
    public String getName()     { return name;     }
    public String getSurname()  { return surname;  }

    public List<String> getPrefixTitles() { return Collections.unmodifiableList(prefixTitles); }
    public List<String> getSuffixTitles() { return Collections.unmodifiableList(suffixTitles); }

    //</editor-fold>

    public String getFullName() {
        if (memFullName != null)
            return memFullName;

        StringBuilder sb = new StringBuilder();

        if (!prefixTitles.isEmpty())
            prefixTitles.stream().forEach(title -> sb.append(title).append(' '));

        sb.append(name).append(' ').append(surname);

        if (!suffixTitles.isEmpty()) {
            sb.append(",");
            suffixTitles.stream().forEach(title -> sb.append(' ').append(title));
        }

        return (memFullName = sb.toString());
    }

    //<editor-fold defaultstate="collapsed" desc="[  HashCode, Equals & ToString  ]">

    @Override
    public int hashCode() {
        if (memHash == null)
            memHash = Objects.hash(id, name, surname, prefixTitles, suffixTitles);

        return memHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Person) || (obj.hashCode() != hashCode()))
            return false;

        final Person other = (Person) obj;

        return (id  == other.id)
            && (        name.equals(other.name)    )
            && (     surname.equals(other.surname) )
            && (prefixTitles.equals(other.prefixTitles))
            && (suffixTitles.equals(other.suffixTitles));
    }

    @Override
    public String toString() {
        return String.format(
                "Person { id: %d, name: \"%s\", surname: \"%s\", prefixTitles: \"%s\", suffixTitles: \"%s\" }",
                id, name, surname, prefixTitles, suffixTitles);
    }

    //</editor-fold>

}
