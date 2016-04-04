package cz.muni.fi.scheduler.model.constraints;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.model.SchModel;
import cz.muni.fi.scheduler.model.context.SchModelContext;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import java.util.Set;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.GlobalConstraint;
import org.cpsolver.ifs.model.Model;

public class DefenceTeacherOverlapConstraint extends GlobalConstraint<Slot, Ticket> {

    private SchModelContext getContext(Assignment<Slot, Ticket> assignment) {
        final SchModel schModel = (SchModel) getModel();
        return schModel.getContext(assignment);
    }

    void getTeacherConflicts(Assignment<Slot, Ticket> assignment,
            Set<Ticket> conflicts,
            TimeSlot refslot, Teacher teacher, EntryRow reference) {
        final SchModelContext context = getContext(assignment);

        context.entryRows(reference.getDay())
                .filter(row -> !row.equals(reference))
                .forEach(row -> {
                    row.streamMemberSlots()
                            .map(assignment::getValue)
                            .filter(ticket -> (ticket != null) && ticket.getPerson().equals(teacher))
                            .forEach(conflicts::add);

                    if (refslot != null) {
                        row.streamTimeSlots()
                            .map(slot -> (TimeSlot) slot)
                            .filter(slot -> slot.getRange().overlaps(refslot.getRange()))
                            .map(assignment::getValue)
                            .filter(ticket -> {
                                if (ticket == null)
                                    return false;

                                Student student = (Student) ticket.getPerson();
                                if (!student.hasThesis())
                                    return false;

                                return student.getThesis().getTeachers().contains(teacher);
                            })
                            .forEach(conflicts::add);
                    }
                });
    }

    private void checkTimeSlot(Assignment<Slot, Ticket> assignment, Ticket value, Set<Ticket> conflicts) {
        final Student  student = (Student) value.getPerson();
        final EntryRow where   = value.variable().getParent();

        if (!student.hasThesis())
            return;

        student.getThesis().getTeachers().stream().forEach(teacher ->
                getTeacherConflicts(assignment, conflicts, (TimeSlot) value.variable(), teacher, where)
        );
    }

    private void checkMemberSlot(Assignment<Slot, Ticket> assignment, Ticket value, Set<Ticket> conflicts) {
        final Teacher  teacher = (Teacher) value.getPerson();
        final EntryRow where   = value.variable().getParent();

        getTeacherConflicts(assignment, conflicts, null, teacher, where);
    }

    @Override
    public void setModel(Model model) {
        if (!(model instanceof SchModel))
            throw new IllegalArgumentException("Model is not SchModel");
        super.setModel(model);
    }

    @Override
    public void computeConflicts(Assignment<Slot, Ticket> assignment, Ticket value, Set<Ticket> conflicts) {
        if (value.isTimeSlotTicket()) {
            checkTimeSlot(assignment, value, conflicts);
        } else {
            checkMemberSlot(assignment, value, conflicts);
        }
    }
}
