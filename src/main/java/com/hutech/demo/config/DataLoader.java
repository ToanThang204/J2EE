package com.hutech.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hutech.demo.model.User;
import com.hutech.demo.model.enums.UserRole;
import com.hutech.demo.model.enums.UserStatus;
import com.hutech.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin account if it doesn't exist
        createAdminAccount();
    }

    private void createAdminAccount() {
        String adminEmail = "admin@gmail.com";
        String adminPass = "admintuyendung8386";

        User admin = userRepository.findByEmail(adminEmail).orElse(new User());
        admin.setName("System Admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPass));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);

        System.out.println("✅ Admin account synced!");
        System.out.println("   Email: " + adminEmail);
        System.out.println("   Password: " + adminPass);

        // Create a backup admin just in case
        String backupEmail = "manager@gmail.com";
        if (userRepository.findByEmail(backupEmail).isEmpty()) {
            User backup = new User();
            backup.setName("Backup Admin");
            backup.setEmail(backupEmail);
            backup.setPassword(passwordEncoder.encode("admin123"));
            backup.setRole(UserRole.ADMIN);
            backup.setStatus(UserStatus.ACTIVE);
            userRepository.save(backup);
            System.out.println("✅ Backup Admin created: manager@gmail.com / admin123");
        }
    }
}
