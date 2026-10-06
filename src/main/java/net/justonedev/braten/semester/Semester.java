package net.justonedev.braten.semester;

import java.time.LocalDate;

public class Semester {
    private static final int LATEST_WINTER_SEMESTER_MONTH = 3;

    private int startingYear;
    private SemesterType type;

    public Semester(LocalDate date) {
        this((date.getMonth().getValue() <= LATEST_WINTER_SEMESTER_MONTH ? -1 : 0) + date.getYear(), SemesterType.infer(date));
    }

    public Semester(int startingYear, SemesterType type) {
        this.startingYear = startingYear;
        this.type = type;
    }

    @Override
    public String toString() {
        if (type == SemesterType.SUMMER) {
            return "Sommersemester %04d".formatted(startingYear);
        }
        int nextYear = (startingYear + 1) % 100;
        return "Wintersemester %04d/%02d".formatted(startingYear, nextYear);
    }
}
