package net.justonedev.braten.semester;

import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Semester {
    private static final String SUMMER_KEY_FORMAT = "summer%04d";
    private static final String WINTER_KEY_FORMAT = "winter%04d%04d";
    private static final Pattern SUMMER_KEY_REGEX = Pattern.compile("^[^-]+-summer(\\d{4})(?:-.+)?$");
    private static final Pattern WINTER_KEY_REGEX = Pattern.compile("^[^-]+-winter(\\d{4})\\d{4}(?:-.+)?$");
    private static final String SUMMER_LABEL_FORMAT = "Sommersemester %04d";
    private static final String WINTER_LABEL_FORMAT = "Wintersemester %04d/%02d";
    private static final String SUMMER_NUMBER_LABEL_FORMAT = "%02d";
    private static final String WINTER_NUMBER_LABEL_FORMAT = "%02d/%02d";
    private static final String SUMMER_SHORT_LABEL_FORMAT = "SS " + SUMMER_NUMBER_LABEL_FORMAT;
    private static final String WINTER_SHORT_LABEL_FORMAT = "WS " + WINTER_NUMBER_LABEL_FORMAT;

    private static final int LATEST_WINTER_SEMESTER_MONTH = 3;

    private final int startingYear;
    private final SemesterType type;

    public Semester(LocalDate date) {
        this((date.getMonth().getValue() <= LATEST_WINTER_SEMESTER_MONTH ? -1 : 0) + date.getYear(), SemesterType.infer(date));
    }

    public Semester(int startingYear, SemesterType type) {
        this.startingYear = startingYear;
        this.type = type;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public SemesterType getSemesterType() {
        return type;
    }

    public String generateValueKey() {
        if (type == SemesterType.SUMMER) {
            return SUMMER_KEY_FORMAT.formatted(startingYear);
        }
        return WINTER_KEY_FORMAT.formatted(startingYear, startingYear + 1);
    }

    public String getShortName() {
        if (type == SemesterType.SUMMER) {
            return SUMMER_SHORT_LABEL_FORMAT.formatted(startingYear % 100);
        }
        return WINTER_SHORT_LABEL_FORMAT.formatted(startingYear % 100, (startingYear + 1) % 100);
    }

    public String getShortNumbersOnly() {
        if (type == SemesterType.SUMMER) {
            return SUMMER_NUMBER_LABEL_FORMAT.formatted(startingYear % 100);
        }
        return WINTER_NUMBER_LABEL_FORMAT.formatted(startingYear % 100, (startingYear + 1) % 100);
    }

    @Override
    public String toString() {
        if (type == SemesterType.SUMMER) {
            return SUMMER_LABEL_FORMAT.formatted(startingYear);
        }
        int nextYear = (startingYear + 1) % 100;
        return WINTER_LABEL_FORMAT.formatted(startingYear, nextYear);
    }

    public Semester getPreviousSemester() {
        if (type == SemesterType.SUMMER) {
            return new Semester(startingYear - 1, SemesterType.WINTER);
        }
        return new Semester(startingYear, SemesterType.SUMMER);
    }

    public static Semester fromKey(String key) {
        Matcher matcher = SUMMER_KEY_REGEX.matcher(key);
        if (matcher.matches()) {
            return new Semester(Integer.parseInt(matcher.group(1)), SemesterType.SUMMER);
        }
        matcher = WINTER_KEY_REGEX.matcher(key);
        if (matcher.matches()) {
            return new Semester(Integer.parseInt(matcher.group(1)), SemesterType.WINTER);
        }
        return null;
    }

    public static Semester fromRoleName(String roleName) {
        roleName = roleName.contains(" ") ? roleName.split(" ")[1] : "";
        if (roleName.matches("\\d{2}/\\d{2}")) {
            return new Semester(2000 + Integer.parseInt(roleName.substring(0, 2)), SemesterType.WINTER);
        } else if (roleName.matches("\\d{2}")) {
            return new Semester(2000 + Integer.parseInt(roleName), SemesterType.SUMMER);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Semester semester = (Semester) o;
        return startingYear == semester.startingYear && type == semester.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startingYear, type);
    }
}
