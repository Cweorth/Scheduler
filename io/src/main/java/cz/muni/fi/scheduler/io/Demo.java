package cz.muni.fi.scheduler.io;

import cz.muni.fi.scheduler.data.Field;
import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Teacher;
import cz.muni.fi.scheduler.data.Thesis;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Demo {

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected directory name.");
        }

        try (DirectoryDataSource dds = new DirectoryDataSource(new File(args[0]))) {
            Map<Long, Field>   fields   = dds.getFields();
            fields.forEach((key, field) -> System.out.println(field));

            Map<Long, Teacher> teachers = dds.getTeachers();
            teachers.forEach((key, teacher) -> System.out.println(teacher + " [" + teacher.getFullName() + "]"));

            Map<Long, Thesis> theses = dds.getTheses();
            theses.forEach((key, thesis) -> System.out.println(thesis));

            Map<Long, Student> students = dds.getStudents();
            students.forEach((key, student) -> System.out.println(student));
        } catch (IOException ex) {
            System.err.println("Oh damn!");
            System.err.println(ex);
        }
    }
}
