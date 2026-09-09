package gr.uni.cinema.cinemamanagement.controller;

import gr.uni.cinema.cinemamanagement.dto.*;
import gr.uni.cinema.cinemamanagement.entity.User;
import gr.uni.cinema.cinemamanagement.exception.InvalidCredentialsException;
import gr.uni.cinema.cinemamanagement.exception.ForbiddenOperationException;
import gr.uni.cinema.cinemamanagement.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    // Eggrafi neou user.
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @RequestBody RegisterUserRequest request) {

        User user = userService.register(
                request.username(),
                request.fullName(),
                request.password()
        );

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getFailedLoginAttempts(),
                user.getCreatedAt()
        );
    }


    // Login tou user.
    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request) {

        return userService.login(
                request.username(),
                request.password()
        );
    }


    // Energopoiisi user apo ADMIN.
    @PatchMapping("/{id}/activate")
    public UserResponse activateUser(
            @PathVariable Long id,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User admin = getUserFromAuthorizationHeader(
                authorizationHeader
        );

        User user = userService.activateUser(
                id,
                admin
        );

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getFailedLoginAttempts(),
                user.getCreatedAt()
        );
    }


    // Apenergopoiisi user apo ADMIN.
    @PatchMapping("/{id}/deactivate")
    public UserResponse deactivateUser(
            @PathVariable Long id,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User admin = getUserFromAuthorizationHeader(
                authorizationHeader
        );

        User user = userService.deactivateUser(
                id,
                admin
        );

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getFailedLoginAttempts(),
                user.getCreatedAt()
        );
    }


    // Allagi password mono apo ton idio user.
    @PatchMapping("/{id}/change-password")
    public UserResponse changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser = getUserFromAuthorizationHeader(
                authorizationHeader
        );

        if (!currentUser.getId().equals(id)) {
            throw new ForbiddenOperationException(
                    "You can only change your own password"
            );
        }

        User user = userService.changePassword(
                id,
                request.oldPassword(),
                request.newPassword()
        );

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getFailedLoginAttempts(),
                user.getCreatedAt()
        );
    }


    // Update profile mono apo ton idio user.
    @PatchMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser = getUserFromAuthorizationHeader(
                authorizationHeader
        );

        if (!currentUser.getId().equals(id)) {
            throw new ForbiddenOperationException(
                    "You can only update your own profile"
            );
        }

        User user = userService.updateUser(
                id,
                request.username(),
                request.fullName()
        );

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getFailedLoginAttempts(),
                user.getCreatedAt()
        );
    }


    // Diagrafi user apo ADMIN.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable Long id,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User admin = getUserFromAuthorizationHeader(
                authorizationHeader
        );

        userService.deleteUser(
                id,
                admin
        );
    }


    // Elegxos an ena token einai egkiro.
    @PostMapping("/validate-token")
    public UserResponse validateToken(
            @RequestBody TokenRequest request) {

        User user = userService.validateToken(
                request.token()
        );

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getFailedLoginAttempts(),
                user.getCreatedAt()
        );
    }


    // Logout kai akyrosi tou token.
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestBody TokenRequest request) {

        userService.logout(request.token());
    }


    // Pairnei ton user apo to Bearer token.
    private User getUserFromAuthorizationHeader(
            String authorizationHeader) {

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            throw new InvalidCredentialsException(
                    "Authorization token is required"
            );
        }

        String token = authorizationHeader.substring(7);

        return userService.validateToken(token);
    }
}