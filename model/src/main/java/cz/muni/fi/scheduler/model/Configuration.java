package cz.muni.fi.scheduler.model;

import static cz.muni.fi.scheduler.extensions.ValueCheck.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model configuration class.
 *
 * The implementation may (and probably will) change in the future.
 *
 * @author Roman Lacko &lt;<a href="mailto:xlacko1@fi.muni.cz">xlacko1@fi.muni.cz</a>&gt;
 */
public class Configuration {
    public final int                fullExamLength;
    public final int                shortExamLength;
    public final List<LocalDate>    dates;
    public final LocalTime          dayStart;

    private Configuration(int fullExamLength, int shortExamLength, List<LocalDate> dates, LocalTime dayStart) {
        this.fullExamLength  = fullExamLength;
        this.shortExamLength = shortExamLength;
        this.dates           = Collections.unmodifiableList(dates);
        this.dayStart        = dayStart;
    }

    public static class Builder {
        private int         fullExamLength;
        private int         shortExamLength;
        private LocalTime   dayStart;

        private final List<LocalDate> dates;

        public Builder() {
            dates    = new ArrayList<>();
            dayStart = LocalTime.of(8, 00);
        }

        public Builder setFullExamLength(int fullExamLength) {
            this.fullExamLength = requirePositive(fullExamLength, "fullExamLength");
            return this;
        }

        public Builder setShortExamLength(int shortExamLength) {
            this.shortExamLength = requirePositive(shortExamLength, "shortExamLength");
            return this;
        }

        public Builder addDate(LocalDate date) {
            dates.add(requireNonNull(date, "date"));
            return this;
        }

        public Builder setDayStart(LocalTime start) {
            dayStart = requireNonNull(start, "start");
            return this;
        }

        public Configuration value() {
            return new Configuration(fullExamLength, shortExamLength, new ArrayList<>(dates), dayStart);
        }

        public int getFullExamLength()       { return fullExamLength;  }
        public int getShortExamLength()      { return shortExamLength; }
        public List<LocalDate> getDates()    { return dates;           }
        public LocalTime       getDayStart() { return dayStart;        }
    }
}