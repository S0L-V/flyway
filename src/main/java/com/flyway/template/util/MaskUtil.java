package com.flyway.template.util;

public class MaskUtil {
    public static String maskEmail(String email) {
        if (email == null) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(Math.max(atIndex, 0));
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

}
