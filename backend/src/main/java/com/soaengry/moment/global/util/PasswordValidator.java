package com.soaengry.moment.global.util;

import java.util.regex.Pattern;

public class PasswordValidator {
    
    // 8자 이상, 영문 대소문자, 숫자, 특수문자 포함
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_+<>?,./-=]).{8,}$";
    
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }
}
