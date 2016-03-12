package cz.muni.fi.scheduler.data.builders;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import cz.muni.fi.scheduler.data.ExamLevel;
import cz.muni.fi.scheduler.data.Field;
import cz.muni.fi.scheduler.data.Student;
import cz.muni.fi.scheduler.data.Repetition;
import cz.muni.fi.scheduler.data.Thesis;

public class StudentBuilder extends PersonBuilder<StudentBuilder> {
    private Field      field;
    private Repetition repetition;
    private ExamLevel  level;
    private Thesis     thesis;

    //<editor-fold desc="[  Getters  ]" defaultstate="collapsed">

    public Field      getField()      { return field;      }
    public Repetition getRepetition() { return repetition; }
    public ExamLevel  getExamLevel()  { return level;      }
    public Thesis     getThesis()     { return thesis;     }

    //</editor-fold>

    //<editor-fold desc="[  Setters  ]" defaultstate="collapsed">

    public StudentBuilder setField(Field field) {
        this.field = requireNonNull(field, "field");
        return this;
    }

    public StudentBuilder setRepetition(Repetition repetition) {
        this.repetition = repetition;
        return this;
    }

    public StudentBuilder setExamLevel(ExamLevel level) {
        this.level = level;
        return this;
    }

    public StudentBuilder setThesis(Thesis thesis) {
        this.thesis = requireNonNull(thesis, "thesis");
        return this;
    }

    //</editor-fold>

    public Student value() {
        return new Student(
                getId(), getName(), getSurname(), getPrefixTitles(), getSuffixTitles(),
                field, repetition, level, thesis
        );
    }
}
