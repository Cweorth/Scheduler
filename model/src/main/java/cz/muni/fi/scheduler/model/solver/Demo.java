package cz.muni.fi.scheduler.model.solver;

import cz.muni.fi.scheduler.data.Commission;
import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.data.Thesis;
import cz.muni.fi.scheduler.io.DataSource;
import cz.muni.fi.scheduler.io.DirectoryDataSource;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.Block;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Model;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;

public class Demo {
    private static final Logger logger = Logger.getLogger("Demo");

    private static DataProperties getProperties() {
        DataProperties prop = new DataProperties();

        prop.setProperty("General.SaveBestUnassigned", "-1");
        prop.setProperty("Neighbour.Class", "org.cpsolver.ifs.algorithms.SimulatedAnnealing");

        prop.setProperty("Termination.Class",       "cz.muni.fi.scheduler.model.ImprovementTerminalCondition");
        prop.setProperty("Termination.MaxIters",    "500000");

        prop.setProperty("Weight.MinimizeBlocksCriterion", "1.0");

        prop.setProperty("Extensions.Classes", "org.cpsolver.ifs.extension.ConflictStatistics");

        prop.setProperty("SimulatedAnnealing.Neighbours",
                String.join(";",
                        "org.cpsolver.ifs.heuristics.StandardNeighbourSelection",
                        "cz.muni.fi.scheduler.model.neighbourhood.TimeSlotSwapInCommission@0.1"
                )
        );

        return prop;
    }

    public static void printRows(List<EntryRow> rows, Assignment<Slot, Ticket> sol, Configuration cfg) {
        System.out.println("SOLUTION: ");
        for (EntryRow row : rows) {
            System.out.println("-- ENTRY ROW " + row.getId());

            Commission commission = row.getCommission(sol, cfg);
            System.out.print("Commission: " +
                    commission.getMembers().stream()
                            .map(Teacher::getSurname)
                            .reduce((a,b) -> a + ", " + b)
                            .get()
                    );

            System.out.println("\n-- SCHEDULE");
            System.out.printf("%7.7s | %10.10s | %10.10s %10.10s\n", "time", "student", "supervisor", "opponents");
            row.streamTimeSlots().forEach(slot -> {
                System.out.printf("%3d-%3d | ", slot.getStart(), slot.getEnd());

                Student student = Optional.ofNullable(sol.getValue(slot))
                        .map(Ticket::getPerson)
                        .map(p -> (Student) p)
                        .orElse(null);

                if (student == null) {
                    System.out.printf("%10.10s | %10.10s\n", "--", "--");
                } else {
                    System.out.printf("%10.10s | ", student.getSurname());

                    if (student.getThesis() != null) {
                        Thesis thesis = student.getThesis();
                        System.out.printf("%10.10s", thesis.getSupervisor().getName());

                        thesis.getOpponents().stream().forEach((opponent) -> {
                            System.out.printf(" %10.10s", opponent.getSurname());
                        });
                    } else {
                        System.out.printf("%10.10s", "--");
                    }

                    System.out.println();
                }
            });

            System.out.println("-- END");
            System.out.println();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Expected directory name.");
            System.exit(1);
        }

        Model<Slot, Ticket> model  = new Model();

        try (DataSource ds = new DirectoryDataSource(new File(args[0]))) {
            int students   = ds.getStudents().size();
            int capacity   = 8;
            int rowsNeeded = (students / capacity) + Math.min(students % capacity, 1);

            Configuration.Builder cfgbld = new Configuration.Builder()
                .setDayStart(LocalTime.of(8, 00))
                .setFullExamLength(30)
                .setShortExamLength(15);

            List<EntryRow> rows = new ArrayList(rowsNeeded);

            for (int i = 0; i < rowsNeeded; ++i) {
                cfgbld.addDate(LocalDate.of(2016, Month.MAY, 1).plusDays(i));
            }

            Configuration       cfg     = cfgbld.value();
            Agenda              agenda  = new Agenda(cfg);
            DataProperties      props   = getProperties();
            SlotManager         smngr   = new SlotManager(agenda);

            for (int row = 0; row < rowsNeeded; ++row) {
                rows.add(new EntryRow(0, smngr, cfg));
            }

            for (EntryRow row : rows) {
                for (int i = 0; i < Math.min(students, capacity) - 1; ++i) {
                    row.extendBack();
                }

                students -= capacity;

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
            //logger.info("Last solution:" + ToolBox.dict2string(solver.currentSolution().getInfo(), 2));
            logger.info("Best solution:" + ToolBox.dict2string(solver.currentSolution().getBestInfo(), 2));
            
            logger.info("=========================================");
            logger.info("Block counts for the current solution");
            Solution dump = solver.lastSolution();
            logger.info(ToolBox.dict2string(dump.getInfo(), 2));
            
            for (Teacher t : ds.getTeachers().values()) {
                logger.info(t.getSurname() + ": " + agenda.blockCount(t));
                
                Map<Integer, List<Block>> data = agenda.getBlocks(t);
                data.entrySet().stream()
                        .forEach(kv -> {
                            System.out.println(kv.getKey() + ":");
                            kv.getValue().stream()
                                    .forEach(block -> System.out.println("    " + block.getInterval()));
                        });
            }
        }
    }
}
