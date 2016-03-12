package cz.muni.fi.scheduler.data.builders;

import cz.muni.fi.scheduler.data.Teacher;

public class TeacherBuilder extends PersonBuilder<TeacherBuilder> {

    public Teacher value() {
        return new Teacher(
                getId(), getName(), getSurname(), getPrefixTitles(), getSuffixTitles()
        );
    }

}
