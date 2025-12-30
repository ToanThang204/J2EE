package com.hutech.demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123123123";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("------------------------------------------------");
        System.out.println("Raw Password: " + rawPassword);
        System.out.println("BCrypt Hash:  " + encodedPassword);
        System.out.println("------------------------------------------------");
        System.out.println("SQL Script update:");
        System.out.println("UPDATE users SET password = '" + encodedPassword + "' WHERE email = 'admin@gmail.com';");
        System.out.println("------------------------------------------------");
    }
}
