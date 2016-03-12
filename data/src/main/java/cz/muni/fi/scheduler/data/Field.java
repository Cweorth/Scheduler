package cz.muni.fi.scheduler.data;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Field of study.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class Field {
    private final long   id;
    private final String name;
    private final String code;

    private Integer memHash; // memoized hash code

    // code must contain at least one character, any of a-z, A-Z, 0-9 or _
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\w+$");

    /**
     * Creates a new field of study.
     *
     * @param id        non-negative and unique ID number
     * @param name      field name
     * @param code      short code; if {@code null} provided, will be set to first four upper characters of {@code name}
     */
    public Field(long id, String name, String code) {
        this.id   = requireNonNegative(id, "Field.id");
        this.name = requireNonNull(name,   "Field.name");

        this.code = (code == null) ? name.substring(0, 3).toUpperCase() : code.toUpperCase();

        requireMatch(CODE_PATTERN, code, "code");
    }

    //<editor-fold defaultstate="collapsed" desc="[  Getters  ]">

    public long   getId()   { return id;   }
    public String getName() { return name; }
    public String getCode() { return code; }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="[  HashCode, Equals & ToString  ]">

    @Override
    public int hashCode() {
        if (memHash == null)
            memHash = Objects.hash(id, name, code);

        return memHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Field) || (obj.hashCode() != hashCode()))
            return false;

        final Field other = (Field) obj;

        return (id         == other.id)
            && (name.equals(other.name))
            && (code.equals(other.code));
    }

    @Override
    public String toString() {
        return String.format(
                "Field { id: %d, code: %s, name: \"%s\" }",
                id, code, name
        );
    }

    //</editor-fold>

}
