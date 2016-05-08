package cz.muni.fi.scheduler.data;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import java.util.Collection;

/**
 * Student entity.
 * In addition to {@link Person}'s attributes this entity contains
 * <ol>
 *     <li>{@code field} of study,</li>
 *     <li>{@code repeater} flag set to {@code true} if the student only takes a part of the exam</li>
 *     <li>{@code terminal} flag set to {@code true} if the student requires to be scheduled the last</li>
 *     <li>exam {@code level} the students takes,</li>
 *     <li>{@code thesis} the student defends</li>
 * </ol>
 *
 * The relation between {@code repeater} and {@code thesis} is as follows:
 * <table summary="relation between repeater and thesis">
 * <thead>
 *     <tr><th>{@code repeater}</th><th>{@code thesis}</th><th></th></tr>
 * </thead>
 * <tbody>
 *     <tr><td colspan="2">{@code false}</td><td>{@code !null}</td><td>student takes both parts of the exam</td></tr>
 *     <tr>                                  <td>{@code  null}</td><td><b>forbidden</b> by constructor</td></tr>
 *     <tr><td colspan="2">{@code true} </td><td>{@code !null}</td><td>student only defends his thesis</td></tr>
 *     <tr>                                  <td>{@code  null}</td><td>student only takes the oral exam</td></tr>
 * </tbody>
 * </table>
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class Student extends Person {
    private final Field      field;
    private final Repetition repetition;
    private final ExamLevel  level;
    private final Thesis     thesis;

    public Student(long id, String name, String surname,
            Collection<String> prefixTitles, Collection<String> suffixTitles,
            Field     field, Repetition repetition,
            ExamLevel level, Thesis     thesis) {
        super(id, name, surname, prefixTitles, suffixTitles);

        this.field      = requireNonNull(field,      "Student.field"     );
        this.repetition = requireNonNull(repetition, "Student.repetition");
        this.level      = level;

        if ((repetition != Repetition.ORAL_EXAM) && (thesis == null))
            throw new IllegalArgumentException("Thesis is required when repetition is set to " + repetition.name());

        this.thesis   = thesis;
    }

    public Student(long id, String name, String surname,
            Field     field, Repetition repetition,
            ExamLevel level, Thesis     thesis) {
        this(id, name, surname, null, null, field, repetition, level, thesis);
    }

    //<editor-fold defaultstate="collapsed" desc="[  Getters  ]">

    public Field      getField()      { return field;      }
    public Repetition getRepetition() { return repetition; }
    public ExamLevel  getExamLevel()  { return level;      }
    public Thesis     getThesis()     { return thesis;     }

    public boolean    hasThesis()     { return thesis != null; }

    //</editor-fold>

    public boolean isRepetent() {
        return repetition != Repetition.NOTHING;
    }

    @Override
    public String toString() {
        return String.format(
                "Student { person: %s, field: %s, repetition: %s, level: %s, thesis: %d }",
                super.toString(), field.getCode(), repetition, level, thesis.getId()
        );
    }
}
