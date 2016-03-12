package cz.muni.fi.scheduler.io.csv;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.ExamLevel;
import cz.muni.fi.scheduler.data.Field;
import cz.muni.fi.scheduler.data.Repetition;
import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.data.Thesis;
import cz.muni.fi.scheduler.data.builders.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ParseEnum;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.LongCellProcessor;
import org.supercsv.util.CsvContext;

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

            V obj = map.get((K) value);
            return next.execute(obj, context);
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

        while ((fb = reader.read(FieldBuilder.class, header, processors)) != null) {
            Field field = fb.value();
            fields.put(field.getId(), field);
            logger.debug("-- " + field);
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
        while ((tb = reader.read(TeacherBuilder.class, header, processors)) != null) {
            Teacher teacher = tb.value();
            teachers.put(teacher.getId(), teacher);
            logger.debug("-- " + teacher);
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
        while ((tb = reader.read(ThesisBuilder.class, header, processors)) != null) {
            Thesis thesis = tb.value();
            theses.put(thesis.getId(), thesis);
            logger.debug("-- " + thesis);
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

        logger.debug("reading theses");
        CsvBeanReader      reader   = new CsvBeanReader(new InputStreamReader(source), CsvPreference.TAB_PREFERENCE);
        String[]           header   = reader.getHeader(true);
        Map<Long, Student> students = new HashMap<>();

        HashMap<String, CellProcessor> procmap = new HashMap();
        procmap.put("id", new Unique(new ParseLong()));
        procmap.put("name", null);
        procmap.put("surname", null);
        procmap.put("prefixtitles", new TitleProcessor());
        procmap.put("suffixtitles", new TitleProcessor());
        procmap.put("field", new ParseReference(strfields));
        procmap.put("repetition", new ParseEnum(Repetition.class, true));
        procmap.put("examlevel", new ParseEnum(ExamLevel.class, true));
        procmap.put("thesis", new ParseLong(new ParseReference(theses)));

        CellProcessor[] processors = buildCellProcessors(header, procmap);

        StudentBuilder sb;
        while ((sb = reader.read(StudentBuilder.class, header, processors)) != null) {
            Student student = sb.value();
            students.put(student.getId(), student);
            logger.debug("-- " + student);
        }

        logger.debug("done, " + theses.size() + " students were read");
        return students;
    }
}
