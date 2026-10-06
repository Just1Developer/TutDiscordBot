package net.justonedev.braten.semester;

import java.util.Arrays;
import java.util.List;

public enum Module {
    PROGRAMMING("programming", "Programmieren", "Proggen", SemesterType.values()),
    ALGORITHMS("algorithms", "Algorithmen", "Algo", SemesterType.SUMMER),
    GTI("gti", "Grundlagen der theoretischen Informatik", "GTI", SemesterType.WINTER),
    TGI("tgi", "Theoretische Grundlagen der Informatik", "TGI", SemesterType.WINTER);

    private final String key;
    private final String title;
    private final String shortName;
    private final List<SemesterType> semesterTypes;

    private Module(String key, String title, String shortName, SemesterType type) {
        this(key, title, shortName, List.of(type));
    }

    private Module(String key, String title, String shortName, SemesterType... types) {
        this(key, title, shortName, Arrays.asList(types));
    }

    private Module(String key, String title, String shortName, List<SemesterType> types) {
        this.key = key;
        this.title = title;
        this.shortName = shortName;
        this.semesterTypes = types;
    }

    public String getTitle() {
        return title;
    }

    public String getShortName() {
        return shortName;
    }

    public boolean isInSemester(Semester semester) {
        return this.semesterTypes.contains(semester.getSemesterType());
    }

    public String getKey() {
        return key;
    }

    public String getSemiShortName() {
        return title.length() > 15 ? shortName : title;
    }

    public static Module fromKey(String key) {
        key = key.split("-")[0];
        for (Module module : values()) {
            if (module.getKey().equals(key)) {
                return module;
            }
        }
        return null;
    }

    public static Module fromRoleName(String roleName) {
        roleName = roleName.split(" ")[0];
        for (Module module : values()) {
            if (module.getShortName().equals(roleName)) {
                return module;
            }
        }
        return null;
    }
}
