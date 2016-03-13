package cz.muni.fi.scheduler.model.domain;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Person;
import cz.muni.fi.scheduler.utils.Pair;
import java.util.HashMap;
import java.util.Map;

/**
 * Not used yet.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class EntityData {
    private final Person person;

    private int blocks;
    private final Map<Pair<Integer, Integer>, TimeSlot> bs;
    private final Map<Pair<Integer, Integer>, TimeSlot> es;

    public EntityData(Person person) {
        this.person = requireNonNull(person, "person");

        this.bs = new HashMap<>();
        this.es = new HashMap<>();
    }

    public Person getPerson() { return person; }

    public int  getBlocks()   { return blocks;  }

    public void addSlot(TimeSlot slot) {
        requireNonNull(slot, "slot");

        TimeSlot left  = es.get(Pair.of(slot.getParent().getDay(), slot.getEnd()  ));
        TimeSlot right = bs.get(Pair.of(slot.getParent().getDay(), slot.getStart()));


    }
}
