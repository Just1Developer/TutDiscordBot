package net.justonedev.braten.semester;

import java.time.LocalDate;

public enum SemesterType {
    WINTER,
    SUMMER;

    public static SemesterType infer(LocalDate date) {
        int month = date.getMonth().getValue();
        if (month > 3 && month < 10) return SUMMER;
        return WINTER;
    }
}
