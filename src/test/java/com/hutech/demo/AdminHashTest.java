package com.hutech.demo;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdminHashTest {

    @Test
    public void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("123123123");
        System.out.println("ADMIN_HASH_START");
        System.out.println(hash);
        System.out.println("ADMIN_HASH_END");
    }
}
