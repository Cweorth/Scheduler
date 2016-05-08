package cz.muni.fi.scheduler.model.criteria;

import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.data.Thesis;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.domain.MemberSlot;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.TimeSlot;
import cz.muni.fi.scheduler.utils.Pair;
import java.util.Set;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;

/**
 * Penalizes the solution for each block.
 *
 * The goal of this criterion is to minimize the number of blocks
 * each teacher must come to.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class MinimizeBlocksCriterion extends BlockCriterion {
    private final static Logger logger = Logger.getLogger("MinimizeBlocksCriterion");

    private final BlockWeightFunction[] weightFunctions;
    private final BlockWeightFunction   wf;

    public MinimizeBlocksCriterion() {
        this.setValueUpdateType(ValueUpdateType.AfterUnassignedBeforeAssigned);

        weightFunctions = new BlockWeightFunction[2];
        weightFunctions[0] = (long c, long d) -> d;
        weightFunctions[1] = (long c, long d) -> d * (2 * c + d);

        wf = weightFunctions[0];
    }

    public double getCommissaryAssignValue(Agenda agenda,
            Assignment<Slot, Ticket> assignment, MemberSlot slot, Teacher commissary) {
        return agenda.analyzeMemberSlotAssign(commissary, slot);
    }

    public double getDefenceAssignValue(Agenda agenda, Assignment<Slot, Ticket> assignment,
            TimeSlot slot, Student student) {
        if (!student.hasThesis())
            return 0.0;

        return student.getThesis().getTeachers().stream()
            .mapToDouble(teacher ->
                wf.value(agenda.blockCount(teacher), agenda.analyzeTimeSlotAssign(teacher, slot))
            ).sum();
    }

    public double evalConflicts(Agenda agenda, Assignment<Slot, Ticket> assignment, Set<Ticket> conflicts) {
        double p = 0.0;

        for (Ticket ticket : conflicts) {
            if (ticket.isTimeSlotTicket()) {
                Student student = (Student) ticket.getPerson();

                if (!student.hasThesis())
                    continue;

                p += student.getThesis().getTeachers().stream()
                    .mapToDouble(teacher -> agenda.analyzeTimeSlotUnassign(teacher, (TimeSlot) ticket.variable()))
                    .sum();
            } else {
                Teacher teacher = (Teacher) ticket.getPerson();
                p += agenda.analyzeMemberSlotUnassign(teacher, (MemberSlot) ticket.variable());
            }
        }

        return p;
    }

    @Override
    public double getValue(Assignment<Slot, Ticket> assignment, Ticket value, Set<Ticket> conflicts) {
        BlockContext context = (BlockContext) getContext(assignment);
        Agenda       agenda  = context.agenda(assignment);

        double vdiff = value.isTimeSlotTicket()
                ? getDefenceAssignValue(agenda, assignment, (TimeSlot) value.variable(), (Student) value.getPerson())
                : getCommissaryAssignValue(agenda, assignment, (MemberSlot) value.variable(), (Teacher) value.getPerson());

        if (conflicts != null && !conflicts.isEmpty())
            vdiff += evalConflicts(agenda, assignment, conflicts);

        return vdiff;
    }

}
