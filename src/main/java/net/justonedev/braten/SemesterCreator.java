package net.justonedev.braten;

import net.justonedev.braten.semester.Module;
import net.justonedev.braten.semester.Semester;

public class SemesterCreator {

    public static String getSemesterRoleName(Module module, Semester semester) {
        return "%s %s".formatted(module.getShortName(), semester.getShortNumbersOnly());
    }

    public static String getCategoryName(Module module, Semester semester) {
        return "%s %s".formatted(module.getSemiShortName(), semester.getShortName());
    }

}
