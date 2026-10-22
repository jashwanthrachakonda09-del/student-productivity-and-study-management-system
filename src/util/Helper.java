package util;

import java.util.List;

// Small helper methods used in many places.
public class Helper {

    // Today's date as text in the form yyyy-MM-dd (example: 2026-10-21)
    public static String today() {
        return new java.sql.Date(System.currentTimeMillis()).toString();
    }

    // UNIT II - Generic method: works for a list of ANY type (Task, String, ...).
    // <T> before the return type declares the generic type.
    public static <T> String listToText(List<T> list) {
        StringBuilder sb = new StringBuilder();
        for (T item : list) {
            sb.append(item.toString()).append("\n");
        }
        return sb.toString();
    }
}
