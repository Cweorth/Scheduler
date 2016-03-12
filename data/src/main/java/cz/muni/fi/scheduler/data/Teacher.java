package cz.muni.fi.scheduler.data;

import java.util.Collection;

/**
 * Teacher entity.
 * The teacher can be a member of a commission, thesis supervisor or opponent.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class Teacher extends Person {

    public Teacher(long id, String name, String surname,
            Collection<String> prefixTitles, Collection<String> suffixTitles) {
        super(id, name, surname, prefixTitles, suffixTitles);
    }

    public Teacher(long id, String name, String surname) {
        super(id, name, surname, null, null);
    }

    @Override
    public String toString() {
        return String.format(
                "Teacher { person: %s }",
                super.toString()
        );
    }

}
