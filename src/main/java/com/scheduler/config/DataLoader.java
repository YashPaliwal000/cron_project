package com.scheduler.config;

import com.scheduler.entity.User;
import com.scheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@test.com").isEmpty()) {
            User user = User.builder()
                    .email("admin@test.com")
                    .password(passwordEncoder.encode("password"))
                    .build();
            userRepository.save(user);
        }
    }
}
