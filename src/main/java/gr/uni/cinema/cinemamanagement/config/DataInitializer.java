package gr.uni.cinema.cinemamanagement.config;

import gr.uni.cinema.cinemamanagement.entity.User;
import gr.uni.cinema.cinemamanagement.entity.UserRole;
import gr.uni.cinema.cinemamanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {

        if (userRepository.findByUsername("admin1").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin1");
            admin.setPassword("dummy");
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
        }

        if (userRepository.findByUsername("programmer1").isEmpty()) {
            User programmer = new User();
            programmer.setUsername("programmer1");
            programmer.setPassword("dummy");
            programmer.setRole(UserRole.PROGRAMMER);
            programmer.setActive(true);

            userRepository.save(programmer);
        }

        if (userRepository.findByUsername("user1").isEmpty()) {
            User user = new User();
            user.setUsername("user1");
            user.setPassword("dummy");
            user.setRole(UserRole.USER);
            user.setActive(true);

            userRepository.save(user);
        }
    }
}
