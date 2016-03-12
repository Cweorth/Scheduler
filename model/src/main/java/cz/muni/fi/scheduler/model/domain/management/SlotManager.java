package cz.muni.fi.scheduler.model.domain.management;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.domain.MemberSlot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class SlotManager {
    private final Agenda agenda;
    private final Map<Student, Set<TimeSlot>>   students;
    private final Map<Teacher, Set<TimeSlot>>   defences;
    private final Map<Teacher, Set<MemberSlot>> members;

    public SlotManager(Agenda agenda) {
        this.agenda = requireNonNull(agenda, "agenda");
        students = new HashMap<>();
        defences = new HashMap<>();
        members  = new HashMap<>();
    }

    /* package private */ void addTimeSlot(TimeSlot slot, Ticket ticket) {
        final Student student = (Student) ticket.getPerson();
        students.computeIfAbsent(student, k -> new HashSet<>()).add(slot);
    }

    /* package private */ void removeTimeSlot(TimeSlot slot, Ticket ticket) {
        final Student student = (Student) ticket.getPerson();
        students.computeIfAbsent(student, k -> new HashSet<>()).remove(slot);
    }

    public Stream<TimeSlot> studentSlots(Ticket ticket) {
        final Student student = (Student) ticket.getPerson();
        return students.getOrDefault(student, new HashSet<>()).stream();
    }

    /* package private */ void markDefence(TimeSlot slot, Teacher teacher) {
        defences.computeIfAbsent(teacher, k -> new HashSet<>()).add(slot);
        agenda.markTimeSlot(teacher, slot);
    }

    /* package private */ void unmarkDefence(TimeSlot slot, Teacher teacher) {
        defences.computeIfAbsent(teacher, k -> new HashSet<>()).remove(slot);
        agenda.unmarkTimeSlot(teacher, slot);
    }

    public Stream<TimeSlot> defences(Teacher teacher) {
        return defences.getOrDefault(teacher, new HashSet<>()).stream();
    }

    /* package private */ void addMemberSlot(MemberSlot slot, Ticket ticket) {
        final Teacher commissary = (Teacher) ticket.getPerson();
        members.computeIfAbsent(commissary, k -> new HashSet<>()).add(slot);
    }

    /* package private */ void removeMemberSlot(MemberSlot slot, Ticket ticket) {
        final Teacher commissary = (Teacher) ticket.getPerson();
        members.computeIfAbsent(commissary, k -> new HashSet<>()).remove(slot);
    }

    public Stream<MemberSlot> memberSlots(Ticket ticket) {
        final Teacher commissary = (Teacher) ticket.getPerson();
        return members.getOrDefault(commissary, new HashSet<>()).stream();
    }

    public Stream<MemberSlot> memberSlotsDay(Ticket ticket, int day) {
        final Teacher commissary = (Teacher) ticket.getPerson();
        return members.getOrDefault(commissary, new HashSet<>()).stream()
                .filter(slot -> slot.getParent().getDay() == day);
    }
}
