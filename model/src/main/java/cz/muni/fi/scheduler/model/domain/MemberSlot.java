package cz.muni.fi.scheduler.model.domain;

import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.model.domain.management.MemberSlotListener;
import cz.muni.fi.scheduler.model.domain.management.SlotManager;
import java.util.Collection;

public class MemberSlot extends Slot {

    public MemberSlot(EntryRow parent, SlotManager manager) {
        super(parent, manager);
        addVariableListener(new MemberSlotListener(this, manager));
    }

    public void setCommissaries(Collection<Teacher> members) {
        setDomain(members);
    }

}
