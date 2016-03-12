package cz.muni.fi.scheduler.model.solver;

import cz.muni.fi.scheduler.data.Commission;
import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Thesis;
import cz.muni.fi.scheduler.io.DataSource;
import cz.muni.fi.scheduler.io.DirectoryDataSource;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.Configuration;
import cz.muni.fi.scheduler.model.constraints.UniqueCommissionMembersConstraint;
import cz.muni.fi.scheduler.model.constraints.UniqueStudentTicketConstraint;
import cz.muni.fi.scheduler.model.criteria.MinimizeBlocksCriterion;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.model.domain.management.SlotManager;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.heuristics.StandardNeighbourSelection;
import org.cpsolver.ifs.model.Model;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;

public class Demo {
    private static final Logger logger = Logger.getLogger("Demo");

    private static DataProperties getProperties() {
        DataProperties prop = new DataProperties();

        prop.setProperty("General.SaveBestUnassigned", "5");
        prop.setProperty("Neighbour.Class", "org.cpsolver.ifs.algorithms.SimulatedAnnealing");

        prop.setProperty("Termination.Class",       "cz.muni.fi.scheduler.model.ImprovementTerminalCondition");
        prop.setProperty("Termination.MaxIters",    "500000");

        prop.setProperty("Weight.MinimizeBlocksCriterion", "1.0");

        prop.setProperty("SimulatedAnnealing.Neighbours",
                String.join(";",
                        "org.cpsolver.ifs.heuristics.StandardNeighbourSelection",
                        "cz.muni.fi.scheduler.model.neighbourhood.DummySwap@0.001",
                        "cz.muni.fi.scheduler.model.neighbourhood.TimeSlotSwapInCommission@0.1"
                )
        );

        return prop;
    }

    private static String printStudent(Student s) {
        StringBuilder sb = new StringBuilder();

        sb.append(s.getFullName())
          .append(" [");

        if (s.hasThesis()) {
            Thesis t = s.getThesis();
            sb.append(t.getName())
              .append("; S ")
              .append(t.getSupervisor().getFullName())
              .append("; O ")
              .append(t.getOpponents().stream().reduce("", (q,o) -> q + "; " + o.getFullName(), (a,b) -> a + b));
        } else {
            sb.append("--");
        }

        return sb.append(']').toString();
    };

    public static void printRows(EntryRow[] rows, Assignment<Slot, Ticket> sol, Configuration cfg) {
        System.out.println("SOLUTION: ");
        for (EntryRow row : rows) {
            System.out.println("-- COMMISSION");

            Commission commission = row.getCommission(sol, cfg);
            System.out.println(commission);

            System.out.println("-- SCHEDULE");
            row.streamTimeSlots().forEach(slot -> {
                System.out.print("> " + slot.getStart() + '-' + slot.getEnd() + ": ");

                String entry = Optional.ofNullable(sol.getValue(slot))
                        .map(Ticket::getPerson)
                        .map(p -> (Student) p)
                        .map(Demo::printStudent)
                        .orElse("<empty>");
                System.out.println(entry);
            });

            System.out.println("-- END");
            System.out.println();
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration cfg = new Configuration.Builder()
                .setDayStart(LocalTime.of(8, 00))
                .setFullExamLength(30)
                .setShortExamLength(15)
                .addDate(LocalDate.of(2016, Month.JANUARY, 1))
                .addDate(LocalDate.of(2016, Month.JANUARY, 2))
                .value();

        if (args.length != 1) {
            System.err.println("Expected directory name.");
            System.exit(1);
        }

        Model<Slot, Ticket> model  = new Model();

        try (DataSource ds = new DirectoryDataSource(new File(args[0]))) {
            Agenda              agenda = new Agenda(cfg);
            DataProperties      props  = getProperties();
            SlotManager         smngr  = new SlotManager(agenda);
            EntryRow[] rows = new EntryRow[] {
                new EntryRow(0, smngr, cfg),
                new EntryRow(1, smngr, cfg)
            };
            for (EntryRow row : rows) {
                for (int i = 0; i < 7; ++i)
                    row.extendBack();

                row.streamAllSlots().forEach(model::addVariable);

                row.streamCommissarySlots().forEach(s -> {
                    try {
                        s.setCommissaries(ds.getTeachers().values());
                    } catch (IOException ex) {
                        logger.error(ex);
                        System.exit(2);
                    }
                });
                row.streamTimeSlots().forEach(s -> {
                    try {
                        s.setStudents(ds.getStudents().values());
                    } catch (IOException ex) {
                        logger.error(ex);
                        System.exit(2);
                    }
                });
            }

            Solver solver = new Solver(props);

            model.addGlobalConstraint(new UniqueStudentTicketConstraint(smngr));
            model.addGlobalConstraint(new UniqueCommissionMembersConstraint(smngr));
            model.addCriterion(new MinimizeBlocksCriterion(agenda));
            model.init(solver);

            solver.setInitalSolution(model);

            solver.start();

            try {
                solver.getSolverThread().join();
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }

            Solution lastSolution = solver.lastSolution();
            lastSolution.restoreBest();
            printRows(rows, lastSolution.getAssignment(), cfg);

            logger.info("Last solution:" + ToolBox.dict2string(solver.currentSolution().getInfo(), 2));
            logger.info("Best solution:" + ToolBox.dict2string(solver.currentSolution().getBestInfo(), 2));

        }
    }
}
