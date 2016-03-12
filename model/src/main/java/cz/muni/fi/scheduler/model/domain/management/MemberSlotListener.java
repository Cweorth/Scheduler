package cz.muni.fi.scheduler.model.domain.management;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.model.domain.MemberSlot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.VariableListener;

public class MemberSlotListener implements VariableListener<Ticket> {
    private final MemberSlot  slot;
    private final SlotManager mngr;

    public MemberSlotListener(MemberSlot slot, SlotManager manager) {
        this.slot = requireNonNull(slot,    "slot");
        this.mngr = requireNonNull(manager, "manager");
    }

    @Override
    public void variableAssigned(Assignment<?, Ticket> assignment, long iteration, Ticket value) {
        mngr.addMemberSlot(slot, value);
    }

    @Override
    public void variableUnassigned(Assignment<?, Ticket> assignment, long iteration, Ticket value) {
        mngr.removeMemberSlot(slot, value);
    }

    @Override
    public void valueRemoved(long iteration, Ticket value) {
        mngr.removeMemberSlot(slot, value);
    }

}
