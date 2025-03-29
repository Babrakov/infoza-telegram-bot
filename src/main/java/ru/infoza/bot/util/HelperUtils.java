package ru.infoza.bot.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HelperUtils {

    public static String getFormattedDate(Instant phone) {
        String pattern = "dd.MM.yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(java.util.Date.from(phone));
    }

    public static String getFormattedDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return formatter.format(date);
    }

    public static String getRemark(String rem, String ist, String date) {
        return rem +
                " (источник: <b>" +
                ist +
                "</b> " +
                date +
                ")\n";
    }

    public static String replaceLatinWithCyrillic(String input) {
        // Define mapping of Latin to Cyrillic letters
        String latin = "ABCEHKMOPTXY";
        String cyrillic = "АВСЕНКМОРТХУ";

        // Create a StringBuilder to build the result
        StringBuilder result = new StringBuilder();

        // Iterate through each character in the input string
        for (char ch : input.toCharArray()) {
            int index = latin.indexOf(ch);
            if (index != -1) {
                // If the character is in the mapping, replace it with the corresponding Cyrillic letter
                result.append(cyrillic.charAt(index));
            } else {
                // Otherwise, keep the original character
                result.append(ch);
            }
        }

        // Convert the StringBuilder back to a String
        return result.toString();
    }

    public static boolean isValidCar(String carNumber) {
        // Regular expressions for valid car numbers
        String regex1 = "^[АВСЕНКМОРТХУ]\\d{3}[АВСЕНКМОРТХУ]{2}\\d{2,3}$";
        String regex2 = "^[АВСЕНКМОРТХУ]{2}\\d{4}[АВСЕНКМОРТХУ]\\d{2,3}$";
        String regex3 = "^[АВСЕНКМОРТХУ]\\d{4}[АВСЕНКМОРТХУ]{2}\\d{2,3}$";
        String regex4 = "^\\d{4}[АВСЕНКМОРТХУ]{2}\\d{2,3}$";
        String regex5 = "^\\d{4}[АВСЕНКМОРТХУ]{2}\\d{2,3}$";
        String regex6 = "^[АВСЕНКМОРТХУ]{1}\\d{4}\\d{2,3}$";
        String regex7 = "^[АВСЕНКМОРТХУ]{2}\\d{4}\\d{2,3}$";
        String regex8 = "^[АВСЕНКМОРТХУ]{2}\\d{3}\\d{2,3}$";
        String regex9 = "^\\d{3}[АВСЕНКМОРТХУ]{1}\\d{2,3}$";

        // Check if the car number matches any of the regular expressions
        return carNumber.matches(regex1) ||
                carNumber.matches(regex2) ||
                carNumber.matches(regex3) ||
                carNumber.matches(regex4) ||
                carNumber.matches(regex5) ||
                carNumber.matches(regex6) ||
                carNumber.matches(regex7) ||
                carNumber.matches(regex8) ||
                carNumber.matches(regex9);
    }

}
