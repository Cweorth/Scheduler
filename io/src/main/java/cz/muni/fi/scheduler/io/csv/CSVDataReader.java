package cz.muni.fi.scheduler.io.csv;

import cz.muni.fi.scheduler.data.Availability;
import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.ExamLevel;
import cz.muni.fi.scheduler.data.Field;
import cz.muni.fi.scheduler.data.Person;
import cz.muni.fi.scheduler.data.Repetition;
import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.data.Thesis;
import cz.muni.fi.scheduler.data.builders.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseEnum;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.LongCellProcessor;
import org.supercsv.util.CsvContext;

/**
 * Helper class for {@link cz.muni.fi.scheduler.io.DataSource} implementations.
 *
 * This class reads lists of entities from CSV files specified as input streams.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class CSVDataReader {

    private static final Logger logger = Logger.getLogger("CSVDataReader");

    //<editor-fold desc="[  Cell Processors  ]" defaultstate="collapsed">

    private static class TitleProcessor extends CellProcessorAdaptor {
        public TitleProcessor()
        { }

        public TitleProcessor(CellProcessor next) {
            super(next);
        }

        @Override
        public Object execute(Object value, CsvContext context) {
            requireNonNull(context, "context");

            if (value == null) {
                return next.execute(value, context);
            }

            List<String> titles = Arrays.asList(String.valueOf(value).split("\\s+"));

            return next.execute(titles, context);
        }
    }

    private static class ParseReference<K,V> extends CellProcessorAdaptor implements LongCellProcessor {
        private final Map<K,V> map;

        public ParseReference(Map<K,V> map) {
            this.map = requireNonNull(map, "map");
        }

        public ParseReference(CellProcessor next, Map<K,V> map) {
            super(next);
            this.map = requireNonNull(map, "map");
        }

        @Override
        public Object execute(Object value, CsvContext context) {
            requireNonNull(value,   "value");
            requireNonNull(context, "context");

            return next.execute(map.get((K) value), context);
        }
    }

    private static class ParseList extends CellProcessorAdaptor {
        public ParseList()
        { }

        public ParseList(CellProcessor next) {
            super(next);
        }

        @Override
        public Object execute(Object value, CsvContext context) {
            requireNonNull(value,   "value");
            requireNonNull(context, "context");

            return Arrays.stream(String.valueOf(value).split("\\s+"))
                    .map(v -> next.execute(v, context))
                    .collect(Collectors.toList());
        }

    }

    //</editor-fold>

    private CellProcessor[] buildCellProcessors(String[] header, HashMap<String, CellProcessor> processors)
            throws IOException {
        CellProcessor[] result = new CellProcessor[header.length];

        for (int i = 0; i < header.length; ++i) {
            String name = header[i].toLowerCase();
            if (!processors.containsKey(name))
                throw new IOException("Unknown column name '" + name + "'.");
            result[i] = processors.get(name);
        }

        return result;
    }

    public Map<Long, Field> readFields(InputStream source) throws IOException {
        requireNonNull(source, "source");

        logger.debug("reading fields");
        CsvBeanReader   reader = new CsvBeanReader(new InputStreamReader(source), CsvPreference.TAB_PREFERENCE);
        String[]        header = reader.getHeader(true);
        Map<Long,Field> fields = new HashMap<>();

        logger.debug("source header: " + Arrays.asList(header));

        HashMap<String, CellProcessor> procmap = new HashMap();
        procmap.put("id", new Unique(new ParseLong()));
        procmap.put("code", new Unique());
        procmap.put("name", null);

        CellProcessor[] processors = buildCellProcessors(header, procmap);

        FieldBuilder    fb;
        try {
            while ((fb = reader.read(FieldBuilder.class, header, processors)) != null) {
                Field field = fb.value();
                fields.put(field.getId(), field);
                logger.debug("# READ " + field);
            }
        } catch (Exception ex) {
            logger.error("failed to parse fields.csv:" + reader.getLineNumber());
        }

        logger.debug("done, " + fields.size() + " fields were read");
        return fields;
    }

    public Map<Long, Teacher> readTeachers(InputStream source) throws IOException {
        requireNonNull(source, "source");

        logger.debug("reading teachers");
        CsvBeanReader      reader   = new CsvBeanReader(new InputStreamReader(source), CsvPreference.TAB_PREFERENCE);
        String[]           header   = reader.getHeader(true);
        Map<Long, Teacher> teachers = new HashMap<>();

        logger.debug("source header: " + Arrays.asList(header));

        HashMap<String, CellProcessor> procmap = new HashMap();
        procmap.put("id", new Unique(new ParseLong()));
        procmap.put("name", null);
        procmap.put("surname", null);
        procmap.put("prefixtitles", new TitleProcessor());
        procmap.put("suffixtitles", new TitleProcessor());

        CellProcessor[] processors = buildCellProcessors(header, procmap);

        TeacherBuilder tb;
        try {
            while ((tb = reader.read(TeacherBuilder.class, header, processors)) != null) {
                Teacher teacher = tb.value();
                teachers.put(teacher.getId(), teacher);
                logger.debug("# READ " + teacher);
            }
        } catch (Exception ex) {
            logger.error("failed to parse teachers.csv:" + reader.getLineNumber());
        }

        logger.debug("done, " + teachers.size() + " teachers were read");
        return teachers;
    }

    public Map<Long, Thesis> readTheses(InputStream source, Map<Long, Teacher> teachers) throws IOException {
        requireNonNull(source,   "source");
        requireNonNull(teachers, "teachers");

        logger.debug("reading theses");
        CsvBeanReader     reader = new CsvBeanReader(new InputStreamReader(source), CsvPreference.TAB_PREFERENCE);
        String[]          header = reader.getHeader(true);
        Map<Long, Thesis> theses = new HashMap<>();

        HashMap<String, CellProcessor> procmap = new HashMap();
        procmap.put("id", new Unique(new ParseLong()));
        procmap.put("name", null);
        procmap.put("supervisor", new ParseLong(new ParseReference(teachers)));
        procmap.put("opponents",  new ParseList(new ParseLong(new ParseReference(teachers))));

        CellProcessor[] processors = buildCellProcessors(header, procmap);

        ThesisBuilder tb;
        try {
            while ((tb = reader.read(ThesisBuilder.class, header, processors)) != null) {
                Thesis thesis = tb.value();
                theses.put(thesis.getId(), thesis);
                logger.debug("# READ " + thesis);
            }
        } catch (Exception ex) {
            logger.error("failed to parse theses.csv:" + reader.getLineNumber());
        }

        logger.debug("done, " + theses.size() + " theses were read");
        return theses;
    }

    public Map<Long, Student> readStudents(InputStream source,
            Map<Long, Thesis> theses, Map<Long, Field> fields) throws IOException {
        requireNonNull(source, "source");
        requireNonNull(theses, "theses");

        Map<String, Field> strfields = new HashMap<>();
        fields.values().stream().forEach((f) -> {
            strfields.put(f.getCode(), f);
        });

        logger.debug("reading students");
        CsvBeanReader      reader   = new CsvBeanReader(new InputStreamReader(source), CsvPreference.TAB_PREFERENCE);
        String[]           header   = reader.getHeader(true);
        Map<Long, Student> students = new HashMap<>();

        HashMap<String, CellProcessor> procmap = new HashMap();
        procmap.put("id",           new Unique(new ParseLong()));
        procmap.put("name",         null);
        procmap.put("surname",      null);
        procmap.put("prefixtitles", new TitleProcessor());
        procmap.put("suffixtitles", new TitleProcessor());
        procmap.put("field",        new ParseReference(strfields));
        procmap.put("repetition",   new ParseEnum(Repetition.class, true));
        procmap.put("examlevel",    new ParseEnum(ExamLevel.class, true));
        procmap.put("thesis",       new Optional(new ParseLong(new ParseReference(theses))));

        CellProcessor[] processors = buildCellProcessors(header, procmap);

        StudentBuilder sb;
        try {
            while ((sb = reader.read(StudentBuilder.class, header, processors)) != null) {
                Student student = sb.value();
                students.put(student.getId(), student);
                logger.debug("# READ " + student);
            }
        } catch (Exception ex) {
            logger.error("failed to parse students.csv:" + reader.getLineNumber());
            throw ex;
        }

        logger.debug("done, " + theses.size() + " students were read");
        return students;
    }

    public void readAvailability(InputStream source,
            Map<Long, Person> people, Availability target) throws IOException {
        requireNonNull(source, "source");
        requireNonNull(people, "people");
        requireNonNull(target, "target");

        logger.debug("reading availability");
        CsvBeanReader      reader   = new CsvBeanReader(new InputStreamReader(source), CsvPreference.TAB_PREFERENCE);
        String[]           header   = reader.getHeader(true);
        Map<Long, Student> students = new HashMap<>();

        HashMap<String, CellProcessor> procmap = new HashMap();
        procmap.put("person", new ParseLong(new ParseReference(people)));
        procmap.put("date",   new ParseDate("dd.MM.yyyy"));
        procmap.put("from",   new Optional(new ParseDate("HH:mm")));
        procmap.put("to",     new Optional(new ParseDate("HH:mm")));

        CellProcessor[] processors = buildCellProcessors(header, procmap);

        AvailabilityEntry entry;
        int counter = 0;
        while ((entry = reader.read(AvailabilityEntry.class, header, processors)) != null) {
            logger.debug("# READ " + entry);

            if ((entry.getFrom() == null) && (entry.getTo() == null)) {
                target.addAvailability(entry.getPerson(), entry.getDate());
            } else {
                if (entry.getFrom() == null)
                    entry.setFrom(LocalTime.MIN);
                if (entry.getTo() == null)
                    entry.setTo(LocalTime.MAX);

                LocalDateTime lo = LocalDateTime.of(entry.getDate(), entry.getFrom());
                LocalDateTime hi = LocalDateTime.of(entry.getDate(), entry.getTo());

                logger.debug("@ ADDING (entry.getPerson(), " + lo + ", " + hi + ")");
                target.addAvailability(entry.getPerson(), lo, hi);
            }
            ++counter;
        }

        logger.debug("done, " + counter + " students were read");
    }

    public static class AvailabilityEntry {
        private Person    person;
        private LocalDate date;
        private LocalTime from;
        private LocalTime to;

        public Person    getPerson()  { return person; }
        public LocalDate getDate()    { return date;   }
        public LocalTime getFrom()    { return from;   }
        public LocalTime getTo()      { return to;     }

        public void setPerson(Person person) { this.person = person; }
        public void setDate(LocalDate date)  { this.date   = date; }
        public void setFrom(LocalTime from)  { this.from   = from; }
        public void setTo(LocalTime to)      { this.to     = to; }

        public void setDate(Date date) {
            this.date = date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        public void setFrom(Date date) {
            this.from = date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        }

        public void setTo(Date date) {
            this.to = date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        }

        @Override
        public String toString() {
            return String.format("AvailEntry { person: %d, date: %s, from: %s, to: %s }",
                    person.getId(),
                    date,
                    from,
                    to
            );
        }
    }
}
