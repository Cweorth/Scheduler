package cz.muni.fi.scheduler.model.solver;

import cz.muni.fi.scheduler.data.Availability;
import cz.muni.fi.scheduler.data.Commission;
import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.data.Thesis;
import cz.muni.fi.scheduler.io.DataSource;
import cz.muni.fi.scheduler.io.DirectoryDataSource;
import cz.muni.fi.scheduler.model.Agenda;
import cz.muni.fi.scheduler.model.Block;
import cz.muni.fi.scheduler.model.Configuration;
import cz.muni.fi.scheduler.model.SchModel;
import cz.muni.fi.scheduler.model.constraints.DefenceTeacherOverlapConstraint;
import cz.muni.fi.scheduler.model.constraints.UniqueCommissionMembersConstraint;
import cz.muni.fi.scheduler.model.constraints.UniqueStudentTicketConstraint;
import cz.muni.fi.scheduler.model.context.SchModelContext;
import cz.muni.fi.scheduler.model.criteria.BlockCriterion;
import cz.muni.fi.scheduler.model.criteria.MinimizeBlocksCriterion;
import cz.muni.fi.scheduler.model.domain.EntryRow;
import cz.muni.fi.scheduler.model.domain.Slot;
import cz.muni.fi.scheduler.model.domain.Ticket;
import cz.muni.fi.scheduler.utils.IntCounter;
import cz.muni.fi.scheduler.utils.Pair;
import cz.muni.fi.scheduler.utils.Range;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;

public class Demo {
    private static final Logger logger = Logger.getLogger("Demo");

    private static DataProperties getProperties() {
        return ToolBox.loadProperties(new File("model.properties"));
    }

    public static void printRows(List<EntryRow> rows, Assignment<Slot, Ticket> sol, Configuration cfg) {
        DateTimeFormatter datefmt = DateTimeFormatter.ofPattern("HH:mm");

        System.out.println("SOLUTION: ");
        rows.stream().sorted((a,b) -> a.getDay() - b.getDay()).forEach(row -> {
            System.out.println("-- ENTRY ROW " + row.getId() + " (DAY " + row.getDay() + ")");

            Commission commission = row.getCommission(sol, cfg);
            System.out.print("Commission: " +
                    commission.getMembers().stream()
                            .map(Teacher::getSurname)
                            .reduce((a,b) -> a + ", " + b)
                            .get()
                    );

            System.out.println("\n-- SCHEDULE");
            System.out.printf("%5s | %10.10s | %10.10s %10.10s\n", "time", "student", "supervisor", "opponents");
            row.streamTimeSlots().forEach(slot -> {
                System.out.printf("%5s | ", cfg.dayStart.plusMinutes(slot.getStart()).format(datefmt));

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
                        System.out.printf("%10.10s", thesis.getSupervisor().getSurname());

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
        });
    }

    private static void dumpAvailability(Availability av) {
        System.out.print("LL");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Expected directory name.");
            System.exit(1);
        }

        SchModel model  = new SchModel();

        try (DataSource ds = new DirectoryDataSource(new File(args[0]))) {
            int students   = ds.getStudents().size();
            int capacity   = 8;
            int days       = 5;
            int rowsNeeded = (students / capacity) + Math.min(students % capacity, 1);

            Availability avail = ds.getAvailability();
            dumpAvailability(avail);
            Configuration.Builder cfgbld = new Configuration.Builder()
                .setDayStart(LocalTime.of(8, 00))
                .setFullExamLength(30)
                .setShortExamLength(15);

            List<EntryRow> rows = new ArrayList(rowsNeeded);

            for (int i = 0; i < rowsNeeded; ++i) {
                cfgbld.addDate(LocalDate.of(2016, Month.MAY, 1).plusDays(i));
            }

            Configuration       cfg     = cfgbld.value();
            DataProperties      props   = getProperties();

            for (int row = 0; row < rowsNeeded; ++row)
                rows.add(new EntryRow(row % days, cfg));

            rows.stream().forEach(model::addEntryRow);

            logger.info("initializing rows");
            for (EntryRow row : rows) {
                LocalDate date = cfg.dates.get(row.getDay());

                for (int i = 0; i < Math.min(students, capacity) - 1; ++i) {
                    row.extendBack();
                }

                students -= capacity;

                row.streamAllSlots().forEach(model::addVariable);

                row.streamCommissarySlots().forEach(s -> {
                    try {
                        s.setCommissaries(ds.getTeachers().values().stream()
                            .filter(teacher -> avail.isAvailable(teacher, date, cfg.dayStart, cfg.dayStart.plusMinutes(row.getEnd() - row.getStart())))
                            .collect(Collectors.toList())
                        );
                    } catch (IOException ex) {
                        logger.error(ex);
                        System.exit(2);
                    }
                });

                row.streamTimeSlots().forEach(s -> {
                    LocalTime start = cfg.dayStart.plusMinutes(s.getStart());
                    LocalTime end   = cfg.dayStart.plusMinutes(s.getEnd());

                    try {
                        List<Student> possible = ds.getStudents().values().stream()
                                .filter(student -> !student.hasThesis()
                                        || student.getThesis().getTeachers().stream()
                                                .allMatch(t -> avail.isAvailable(t, date, start, end))
                                )
                                .collect(Collectors.toList());

                        logger.debug("TS " + s.getId() + " (ER " + s.getParent().getId() + ": " + s.getParent().getDay() + "; S " + s.getStart() +
                                " has " + possible.size() + " values");
                        s.setStudents(possible);
                    } catch (IOException ex) {
                        logger.error(ex);
                        System.exit(2);
                    }
                });
            }
            logger.info("rows initialized, solving");

            Solver solver = new Solver(props);

            model.addGlobalConstraint(new UniqueStudentTicketConstraint());
            model.addGlobalConstraint(new UniqueCommissionMembersConstraint());
            model.addGlobalConstraint(new DefenceTeacherOverlapConstraint());

            MinimizeBlocksCriterion mbcrit = new MinimizeBlocksCriterion();
            model.addCriterion(mbcrit);
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

            logger.info(ToolBox.dict2string(lastSolution.getBestInfo(), 2));
            printRows(rows, lastSolution.getAssignment(), cfg);
            double totalValueBest = model.getTotalValue(lastSolution.getAssignment());
            logger.info("Total value: " + totalValueBest);

            logger.info("=========================================");
            logger.info("  BLOCK COUNTS                           ");
            logger.info("=========================================");

            SchModelContext modelctxt = model.getContext(lastSolution.getAssignment());

            BlockCriterion.BlockContext context = (BlockCriterion.BlockContext)
                    mbcrit.getContext(lastSolution.getAssignment());
            Agenda agenda = modelctxt.getAgenda();

            Map<Teacher, String[]> blockmap = new HashMap<>();
            Map<Teacher, IntCounter>  scmmap   = new HashMap<>();

            final DateTimeFormatter datefmt = DateTimeFormatter.ofPattern("HH:mm");
            final int fields = Math.min(days, rowsNeeded);
            final Assignment<Slot, Ticket> assignment = lastSolution.getAssignment();

            // compute scmmap
            modelctxt.entryRows().forEach(row -> {
                Set<Teacher> members = row.streamCommissarySlots()
                    .map(assignment::getValue)
                    .filter(Objects::nonNull)
                    .map(ticket -> (Teacher) ticket.getPerson())
                    .collect(Collectors.toSet());

                row.streamTimeSlots()
                    .map(assignment::getValue)
                    .filter(Objects::nonNull)
                    .map(ticket -> (Student) ticket.getPerson())
                    .filter(Student::hasThesis)
                    .map(Student::getThesis)
                    .flatMap(thesis -> thesis.getTeachers().stream())
                    .filter(teacher -> !members.contains(teacher))
                    .forEach(teacher -> scmmap.computeIfAbsent(teacher, t -> new IntCounter(0)).inc());
            });

            Function<Integer,String> fmttime = (num) -> cfg.dayStart.plusMinutes((long) num).format(datefmt);

            ds.getTeachers().values().stream().forEach(t -> {
                @SuppressWarnings("MismatchedReadAndWriteOfArray")
                String[] blocks = blockmap.computeIfAbsent(t, (x) -> new String[fields]);

                Map<Integer, List<Block>> data = agenda.getBlocks(t);

                for (int i = 0; i < fields; ++i) {
                    blocks[i] = data.getOrDefault(i, new ArrayList<>()).stream()
                            .map(block -> {
                                Range<Integer> range = block.getInterval();
                                String min  = fmttime.apply(range.getMin());
                                String max  = fmttime.apply(range.getMax());

                                return block.isSpanning()
                                        ? ("<" + min + "--" + max + ">")
                                        : ("[" + min + "--" + max + "]");
                            })
                            .reduce(new StringBuilder(), (b,s) -> b.append(s), (p,q) -> p.append(q))
                            .toString();
                }
            });

            List<String> widths = blockmap.values().stream()
                    .map(p -> Arrays.stream(p).map(String::length))
                    .reduce(Stream.generate(() -> 0).limit(fields),
                            (l,r) -> StreamUtils.zipWith(l, r, (a,b) -> Integer.max(a, b))
                    ).map(p -> "%" + (p > 0 ? String.valueOf(p) : "") + "s")
                    .collect(Collectors.toList());

            Map<Teacher, Long> studentCounts;
            try {
                studentCounts = ds.getTeachers().values().stream()
                        .map(teacher -> {
                            long count;
                            try {
                                // I <3 Java!
                                count = ds.getStudents().values().stream()
                                        .filter(Student::hasThesis)
                                        .map(Student::getThesis)
                                        .filter(thesis -> thesis.hasTeacher(teacher))
                                        .count();
                            } catch (IOException ex) {
                                count = 999L;
                            }
                            return Pair.of(teacher, count);
                        })
                        .collect(Collectors.toMap(p -> p.first(), p -> p.second()));
            } catch (IOException ex) {
                studentCounts = new HashMap<>();
            }

            final Map<Teacher, Long> studentCountsC = studentCounts;
            System.out.printf("%3s | %10s | BLK | COM | STU | SCM | DAYS\n", "ID", "NAME");
            blockmap.entrySet().stream()
                    //.sorted((a,b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getKey().getSurname(), b.getKey().getSurname()))
                    .sorted((a, b) -> {
                        int diff = -Long.compare(agenda.blockCount(a.getKey()), agenda.blockCount(b.getKey()));
                        if (diff == 0)
                            diff = -Long.compare(modelctxt.memberSlots(a.getKey()).count(),
                                                 modelctxt.memberSlots(b.getKey()).count());
                        if (diff == 0)
                            diff = -Long.compare(studentCountsC.getOrDefault(a.getKey(), 0L),
                                                 studentCountsC.getOrDefault(b.getKey(), 0L));
                        if (diff == 0)
                            diff = String.CASE_INSENSITIVE_ORDER.compare(a.getKey().getSurname(), b.getKey().getSurname());
                        return diff;
                    })
                    .forEach(kvp -> {
                        final Teacher  key   = kvp.getKey();
                        final String[] value = kvp.getValue();
                        final long      COM   = modelctxt.memberSlots(key).count();
                        System.out.printf("%3d | %10.10s | %3d | %3d | %3d | %3d |",
                                key.getId(),
                                key.getSurname(),
                                agenda.blockCount(key),
                                COM,
                                studentCountsC.getOrDefault(key, 0L),
                                COM == 0 ? 0 : scmmap.getOrDefault(key, new IntCounter()).get()
                        );

                        for (int i = 0; i < value.length; ++i) {
                            System.out.printf(" " + widths.get(i) + " |", value[i]);
                        }

                        System.out.println("");
                    });
        }
    }
}
