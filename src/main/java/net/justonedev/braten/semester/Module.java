package net.justonedev.braten.semester;

import java.util.List;

public enum Module {
    PROGRAMMING(),
    ALGORITHMS(),
    GTI(),
    TGI();



    private Module(String title, List<SemesterType> type) {

    }
}
