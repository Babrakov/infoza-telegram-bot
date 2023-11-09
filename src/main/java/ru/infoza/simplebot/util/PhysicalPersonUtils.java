package ru.infoza.simplebot.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PhysicalPersonUtils {

    public static String md5Hash(String input){
        // Create an instance of the MD5 message digest algorithm
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Compute the MD5 hash
        byte[] md5Bytes = md5.digest(input.getBytes());

        // Convert the byte array to a hexadecimal representation
        BigInteger bigInt = new BigInteger(1, md5Bytes);
        return bigInt.toString(16);
    }

    public static String resolvedInFls(Long inFLS) {
        String binaryString = Long.toBinaryString(inFLS);
        String paddedBinaryString = String.format("%7s", binaryString).replace(' ', '0');

        return (paddedBinaryString.charAt(6) == '1' ? "К" : "-") +
                (paddedBinaryString.charAt(5) == '1' ? "О" : "-") +
                (paddedBinaryString.charAt(4) == '1' ? "Ж" : "-") +
                (paddedBinaryString.charAt(3) == '1' ? "[З]" : "[ ]") +
                (paddedBinaryString.charAt(2) == '1' ? "Г" : "-") +
                (paddedBinaryString.charAt(1) == '1' ? "С" : "-") +
                (paddedBinaryString.charAt(0) == '1' ? "Ф" : "-");

    }

}
