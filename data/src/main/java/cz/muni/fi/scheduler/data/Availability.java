package cz.muni.fi.scheduler.data;

import cz.muni.fi.scheduler.utils.Pair;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Availability {
    private final Map<Person, Set<Pair<LocalDateTime, LocalDateTime>>> mapping;

    public Availability() {
        mapping = new HashMap<>();
    }

    public void addAvailability(Teacher teacher, LocalDate day) {
        LocalDateTime from = LocalDateTime.of(day, LocalTime.MIN);
        LocalDateTime to   = LocalDateTime.of(day, LocalTime.MAX);

        addAvailability(teacher, from, to);
    }

    public void addAvailability(Teacher teacher, LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to))
            throw new IllegalArgumentException("DateTime from is after to.");

        mapping.computeIfAbsent(teacher, k -> new HashSet<>()).add(Pair.of(from, to));
        // TODO: condense intervals?
    }

    public boolean isAvailable(Teacher teacher, LocalDateTime when) {
        Set<Pair<LocalDateTime, LocalDateTime>> intervals = mapping.get(teacher);

        if (intervals == null)
            return true;

        return intervals.stream().anyMatch(
            p -> (p.first().isEqual(when)  || p.first().isBefore(when))
              &&  p.second().isAfter(when)
        );
    }

    public boolean isAvailable(Teacher teacher, LocalDate day, LocalTime from, LocalTime to) {
        Set<Pair<LocalDateTime, LocalDateTime>> intervals = mapping.get(teacher);

        if (intervals == null)
            return true;

        LocalDateTime start = LocalDateTime.of(day, from);
        LocalDateTime end   = LocalDateTime.of(day, to);

        return intervals.stream().anyMatch(
            p -> (p.first().isEqual(start) || p.first().isBefore(start))
              && (p.second().isEqual(end)  || p.second().isAfter(end))
        );
    }
}
