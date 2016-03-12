package cz.muni.fi.scheduler.io;

import cz.muni.fi.scheduler.data.Field;
import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.data.Thesis;
import java.io.IOException;
import java.util.Map;

public interface DataSource extends AutoCloseable {
    public Map<Long, Field> getFields() throws IOException;
    public Map<Long, Thesis> getTheses() throws IOException;
    public Map<Long, Teacher> getTeachers() throws IOException;
    public Map<Long, Student> getStudents() throws IOException;
    public Object getMetadata();
    public Object getConfiguration();

    public void write();
}