package za.ac.cput.adp_assignment02.util;

/*
Helper.java
Helper Utility class
Author: David Daniel Sepkitt (240046935)
Date: 10 August 2026
*/

public class Helper {
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /* Trims a value and turns null into "" (older tables have NOT NULL text columns) */
    public static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
