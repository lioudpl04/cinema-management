package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.dto.LoginResponse;
import gr.uni.cinema.cinemamanagement.entity.User;

public interface UserService {

    // Eggrafi neou user.
    User register(
            String username,
            String fullName,
            String password
    );

    // Login kai dimiourgia token.
    LoginResponse login(
            String username,
            String password
    );

    // Energopoiisi user apo ADMIN.
    User activateUser(
            Long userId,
            User admin
    );

    // Apenergopoiisi user apo ADMIN.
    User deactivateUser(
            Long userId,
            User admin
    );

    // Allagi password tou user.
    User changePassword(
            Long userId,
            String oldPassword,
            String newPassword
    );

    // Enimerosi vasikon stoixeion tou user.
    User updateUser(
            Long userId,
            String username,
            String fullName
    );

    // Diagrafi user apo ADMIN.
    void deleteUser(
            Long userId,
            User admin
    );

    // Elegxos token kai epistrofi tou antistoixou user.
    User validateToken(String token);

    // Logout kai akyrosi tou token.
    void logout(String token);
}