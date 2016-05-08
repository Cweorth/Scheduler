package cz.muni.fi.scheduler.io;

import cz.muni.fi.scheduler.data.Availability;
import static cz.muni.fi.scheduler.extensions.ValueCheck.*;

import cz.muni.fi.scheduler.data.Field;
import cz.muni.fi.scheduler.data.Person;
import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.data.Thesis;
import cz.muni.fi.scheduler.io.csv.CSVDataReader;
import cz.muni.fi.scheduler.utils.IOFunction;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Logger;

/**
 * Implementation of {@link DataSource} that provides data from a directory.
 *
 * The directory should contain the following files:
 * <pre>
 *   |-- students.csv
 *   |-- teachers.csv
 *   |-- fields.csv
 *   \-- theses.csv
 * </pre>
 *
 * @author cweorth
 */
public class DirectoryDataSource implements DataSource, AutoCloseable {

    private static final Logger logger = Logger.getLogger("DirectoryDataSource");

    private File flock;
    private final File source;
    private static final CSVDataReader csvreader;

    private Map<Long, Field>    memFields;
    private Map<Long, Teacher>  memTeachers;
    private Map<Long, Thesis>   memTheses;
    private Map<Long, Student>  memStudents;

    private Map<Long, Person>   memPeople;
    private Availability        memAvailability;

    static {
        logger.debug("static initializer");
        csvreader = new CSVDataReader();
    }

    private <T> Map<Long, T> genericFileReader(String fileName,
            IOFunction<FileInputStream, Map<Long,T>> reader) throws IOException {
        File fields = new File(source, fileName);
        logger.debug("probing file '" + fields.getAbsolutePath() + "'");

        if (!fields.exists()) {
            throw new FileNotFoundException("File " + fileName + " does not exist.");
        }

        Map<Long, T> data = null;
        logger.debug("reading file");

        try (FileInputStream fis = new FileInputStream(fields)) {
            data = reader.apply(fis);
        } catch (Exception ex) {
            logger.error("failed to read data from " + fileName);
            logger.debug(ex);
            throw ex;
        }

        return data;
    }

    private Map<Long, Person> getPeople() throws IOException {
        if (memPeople == null) {
            memPeople = Stream.concat(getTeachers().values().stream(),
                                      getStudents().values().stream())
                    .collect(Collectors.toMap(p -> p.getId(), Function.identity()));
        }

        return memPeople;
    }

    public DirectoryDataSource(File source) throws IOException {
        this.source = requireNonNull(source, "source");
        logger.debug("source path: '" + source.getAbsolutePath() + "'");

        if (!source.exists()) {
            IOException ex = new FileNotFoundException(source.getName() + " does not exist.");
            logger.error(ex);
            throw ex;
        }

        if (!source.isDirectory()) {
            IOException ex = new IOException("Argument " + source.getName() + " is not a directory.");
            logger.error(ex);
            throw ex;
        }

        if (Stream.of(source.listFiles()).filter(file -> file.getName().equals(".lock")).findAny().isPresent()) {
            IOException ex = new IOException("Source " + source.getName() + " is locked.");
            logger.error(ex);
            throw ex;
        }

        //flock = new File(source, ".lock");
        //logger.debug("source flock: '" + flock.getAbsolutePath() + "'");
        //flock.createNewFile();
        //logger.debug("source locked");
    }

    @Override
    public void close() {
        logger.debug("flushing data");
        write();
        logger.debug("unlocking source");
        //flock.delete();
        //logger.debug("source unlocked");
    }

    @Override
    public Map<Long, Field> getFields() throws IOException {
        if (memFields == null) {
            memFields = genericFileReader("fields.csv", csvreader::readFields);
        }

        return memFields;
    }

    @Override
    public Map<Long, Teacher> getTeachers() throws IOException {
        if (memTeachers == null) {
            memTeachers = genericFileReader("teachers.csv", csvreader::readTeachers);
        }

        return memTeachers;
    }

    @Override
    public Map<Long, Thesis> getTheses() throws IOException {
        if (memTheses == null) {
            memTheses = genericFileReader("theses.csv", x -> csvreader.readTheses(x, getTeachers()));
        }

        return memTheses;
    }

    @Override
    public Map<Long, Student> getStudents() throws IOException {
        if (memStudents == null) {
            memStudents = genericFileReader("students.csv",
                                            x -> csvreader.readStudents(x, getTheses(), getFields()));
        }

        return memStudents;
    }

    @Override
    public Availability getAvailability() throws IOException {
        if (memAvailability != null)
            return memAvailability;

        memAvailability = new Availability();
        File file = new File(source, "availability.csv");
        logger.debug("probing file '" + file.getAbsolutePath() + "'");

        if (!file.exists()) {
            logger.warn("File 'availability.csv' does not exist (but I can live without it)");
            return memAvailability;
        }

        logger.debug("reading file");

        try (FileInputStream fis = new FileInputStream(file)) {
            csvreader.readAvailability(fis, getPeople(), memAvailability);
        } catch (Exception ex) {
            logger.error("failed to read availability from a CSV file availability.csv");
            logger.debug(ex);
            throw ex;
        }

        return memAvailability;
    }

    @Override
    public Object getMetadata() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getConfiguration() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void write() {

    }

}
