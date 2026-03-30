package com.ourmemories.OurMemoriesEduSmart.config;

import com.ourmemories.OurMemoriesEduSmart.model.Role;
import com.ourmemories.OurMemoriesEduSmart.model.User;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Check if admin exists
        Optional<User> existingAdmin = userRepository.findByEmail("shupe@gmail.com");
        if (existingAdmin.isEmpty()) {
            // Create admin
            User admin = new User();
            admin.setEmail("shupe@gmail.com");
            admin.setFirstName("Dann");
            admin.setLastName("Mphofela");
            admin.setPassword(passwordEncoder.encode("admin123"));  // Change this password!
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
            System.out.println("✅ Admin user created: admin@oursmart.com / admin123");
        } else {
            System.out.println("ℹ️ Admin user already exists.");
        }
    }
}