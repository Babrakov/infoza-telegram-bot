package ru.infoza.simplebot.util;

public class PhoneUtils {
    public static String formatPhoneNumber(String phoneNumber) {
        return "+7" + formatPhoneNumberTenDigits(phoneNumber);
    }

    public static String formatPhoneNumberTenDigits(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
        if (phoneNumber.length() == 11) {
            phoneNumber = phoneNumber.substring(1);
        }
        return phoneNumber;
    }
}
