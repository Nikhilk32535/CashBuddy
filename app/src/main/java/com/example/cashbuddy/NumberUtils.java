package com.example.cashbuddy;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtils {

    // Format number in Indian style (e.g. 5,00,000)
    public static String formatIndianNumber(long number) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));
        return formatter.format(number);
    }

    // Overload for int
    public static String formatIndianNumber(int number) {
        return formatIndianNumber((long) number);
    }

    // Overload for String input (safe parsing)
    public static String formatIndianNumber(String numberStr) {
        try {
            long num = Long.parseLong(numberStr);
            return formatIndianNumber(num);
        } catch (NumberFormatException e) {
            return numberStr; // return original if invalid
        }
    }
}
