package cz.muni.fi.scheduler.model.domain;

import cz.muni.fi.scheduler.data.Teacher;
import java.util.Collection;

/**
 * A variable for commission members.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class MemberSlot extends Slot {

    public MemberSlot(EntryRow parent) {
        super(parent);
    }

    public void setCommissaries(Collection<Teacher> members) {
        setDomain(members);
    }

}
