package cz.muni.fi.scheduler.model.context;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.MemberSlot;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.context.AssignmentConstraintContext;

public class SchModelContext implements AssignmentConstraintContext<Slot, Ticket> {
    private final Map<Student, Set<TimeSlot>>   stud2ts;
    private final Map<Teacher, Set<TimeSlot>>   tchr2ts;
    private final Map<Teacher, Set<MemberSlot>> tchr2ms;
    private final Agenda                        agenda;

    private final Map<Integer, Set<EntryRow>>   day2erow;

    public SchModelContext() {
        agenda   = new Agenda();
        stud2ts  = new HashMap<>();
        tchr2ts  = new HashMap<>();
        tchr2ms  = new HashMap<>();
        day2erow = new HashMap<>();
    }

    public Stream<TimeSlot> studentSlots(Ticket ticket) {
        final Student student = (Student) ticket.getPerson();
        return stud2ts.getOrDefault(student, new HashSet<>()).stream();
    }

    public Stream<TimeSlot> defenceSlots(Ticket ticket) {
        final Teacher teacher = (Teacher) ticket.getPerson();
        return tchr2ts.getOrDefault(teacher, new HashSet<>()).stream();
    }

    public Stream<MemberSlot> memberSlots(Ticket ticket) {
        final Teacher teacher = (Teacher) ticket.getPerson();
        return tchr2ms.getOrDefault(teacher, new HashSet<>()).stream();
    }

    public Stream<Slot> teacherSlots(Ticket ticket) {
        return Stream.concat(memberSlots(ticket), defenceSlots(ticket));
    }

    public void addEntryRow(EntryRow row) {
        requireNonNull(row, "row");

        day2erow.computeIfAbsent(row.getDay(), (r) -> new HashSet<>()).add(row);
    }

    public Stream<EntryRow> entryRows() {
        return day2erow.values().stream().flatMap(Set::stream);
    }

    public Stream<EntryRow> entryRows(int day) {
        return day2erow.getOrDefault(day, new HashSet<>()).stream();
    }

    public Agenda getAgenda() { return agenda; }

    //<editor-fold defaultstate="collapsed" desc="[  AssignmentConstraintContext  ]">

    private void assignedTimeSlot(Ticket ticket) {
        final Student  student = (Student)  ticket.getPerson();
        final TimeSlot slot    = (TimeSlot) ticket.variable();

        stud2ts.computeIfAbsent(student, (s) -> new HashSet<>()).add(slot);

        if (!student.hasThesis())
            return;

        student.getThesis().getTeachers().stream().forEach((teacher) -> {
            tchr2ts.computeIfAbsent(teacher, (t) -> new HashSet<>()).add(slot);
            agenda.markTimeSlot(teacher, slot);
        });
    }

    private void assignedMemberSlot(Ticket ticket) {
        final Teacher    teacher = (Teacher)    ticket.getPerson();
        final MemberSlot slot    = (MemberSlot) ticket.variable();

        tchr2ms.computeIfAbsent(teacher, (t) -> new HashSet<>()).add(slot);
    }

    private void unassignedTimeSlot(Ticket ticket) {
        final Student  student = (Student)  ticket.getPerson();
        final TimeSlot slot    = (TimeSlot) ticket.variable();

        stud2ts.computeIfAbsent(student, (s) -> new HashSet<>()).remove(slot);

        if (!student.hasThesis())
            return;

        student.getThesis().getTeachers().stream().forEach((teacher) -> {
            tchr2ts.computeIfAbsent(teacher, (t) -> new HashSet<>()).remove(slot);
            agenda.unmarkTimeSlot(teacher, slot);
        });
    }

    private void unassignedMemberSlot(Ticket ticket) {
        final Teacher    teacher = (Teacher)    ticket.getPerson();
        final MemberSlot slot    = (MemberSlot) ticket.variable();

        tchr2ms.computeIfAbsent(teacher, (t) -> new HashSet<>()).remove(slot);
    }

    @Override
    public void assigned(Assignment<Slot, Ticket> assignment, Ticket value) {
        if (value.isTimeSlotTicket())
            assignedTimeSlot(value);
        else
            assignedMemberSlot(value);
    }

    @Override
    public void unassigned(Assignment<Slot, Ticket> assignment, Ticket value) {
        if (value.isTimeSlotTicket())
            unassignedTimeSlot(value);
        else
            unassignedMemberSlot(value);
    }

    //</editor-fold>

}
