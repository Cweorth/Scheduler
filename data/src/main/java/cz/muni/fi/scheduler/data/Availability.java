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
    private final Map<Person, Set<LocalDate>> restrictedDays;

    public Availability() {
        mapping        = new HashMap<>();
        restrictedDays = new HashMap<>();
    }

    public void addAvailability(Person person, LocalDate day) {
        LocalDateTime from = LocalDateTime.of(day, LocalTime.MIN);
        LocalDateTime to   = LocalDateTime.of(day, LocalTime.MAX);

        addAvailability(person, from, to);
        restrictedDays.computeIfAbsent(person, k -> new HashSet<>()).add(day);
    }

    public void addAvailability(Person person, LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to))
            throw new IllegalArgumentException("DateTime from is after to.");

        mapping.computeIfAbsent(person, k -> new HashSet<>()).add(Pair.of(from, to));

        LocalDate daystart = from.toLocalDate();
        LocalDate dayend   = to.toLocalDate();

        for (LocalDate date = daystart; date.isBefore(dayend); date = date.plusDays(1)) {
            restrictedDays.computeIfAbsent(person, k -> new HashSet<>()).add(date);
        }

        if (to.isAfter(dayend.atTime(LocalTime.MIN))) {
            restrictedDays.computeIfAbsent(person, k -> new HashSet<>()).add(dayend);
        }

        // TODO: condense intervals?
    }

    public boolean isAvailable(Person person, LocalDateTime when) {
        Set<Pair<LocalDateTime, LocalDateTime>> intervals = mapping.get(person);

        if (intervals == null)
            return true;

        return intervals.stream().anyMatch(
            p -> (p.first().isEqual(when)  || p.first().isBefore(when))
                &&  p.second().isAfter(when)
        );
    }

    public boolean isAvailable(Person person, LocalDate day, LocalTime from, LocalTime to) {
        Set<LocalDate> restricted = restrictedDays.get(person);

        if (restricted == null || !restricted.contains(day))
            return true;

        Set<Pair<LocalDateTime, LocalDateTime>> intervals = mapping.get(person);

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
