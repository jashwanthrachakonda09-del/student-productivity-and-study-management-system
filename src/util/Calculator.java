package util;

import java.util.ArrayList;

// UNIT I - static methods, method overloading, recursion, type casting.
public class Calculator {

    // Percentage of 'part' out of 'total'. Example: percentage(8, 10) = 80
    public static int percentage(int part, int total) {
        if (total == 0) {
            return 0;                          // avoid dividing by zero
        }
        double result = (part * 100.0) / total;
        return (int) result;                   // TYPE CASTING: double -> int
    }

    // METHOD OVERLOADING: two methods with the same name but different parameters.

    // 1) Easy version: just give the list
    public static int sumMinutes(ArrayList<Integer> list) {
        return sumMinutes(list, 0);
    }

    // 2) RECURSION: the method calls itself.
    //    Total = first number + total of the remaining numbers.
    //    It stops (base case) when index reaches the end of the list.
    public static int sumMinutes(ArrayList<Integer> list, int index) {
        if (index >= list.size()) {
            return 0;                          // base case
        }
        return list.get(index) + sumMinutes(list, index + 1);
    }

    // Converts 200 minutes into "3h 20m"
    public static String formatMinutes(int totalMinutes) {
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;
        return hours + "h " + mins + "m";
    }
}
