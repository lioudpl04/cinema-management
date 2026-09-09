package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.dto.LoginResponse;
import gr.uni.cinema.cinemamanagement.entity.AuthToken;
import gr.uni.cinema.cinemamanagement.entity.User;
import gr.uni.cinema.cinemamanagement.entity.UserRole;
import gr.uni.cinema.cinemamanagement.exception.ForbiddenOperationException;
import gr.uni.cinema.cinemamanagement.exception.InvalidCredentialsException;
import gr.uni.cinema.cinemamanagement.exception.InvalidUserDataException;
import gr.uni.cinema.cinemamanagement.exception.UserCannotBeDeletedException;
import gr.uni.cinema.cinemamanagement.exception.UserNotFoundException;
import gr.uni.cinema.cinemamanagement.exception.UsernameAlreadyExistsException;
import gr.uni.cinema.cinemamanagement.repository.AuthTokenRepository;
import gr.uni.cinema.cinemamanagement.repository.ProgramRoleRepository;
import gr.uni.cinema.cinemamanagement.repository.ScreeningRepository;
import gr.uni.cinema.cinemamanagement.repository.UserRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final ProgramRoleRepository programRoleRepository;
    private final ScreeningRepository screeningRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UserServiceImpl(
            UserRepository userRepository,
            AuthTokenRepository authTokenRepository,
            ProgramRoleRepository programRoleRepository,
            ScreeningRepository screeningRepository) {

        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.programRoleRepository = programRoleRepository;
        this.screeningRepository = screeningRepository;
    }


    // Eggrafi neou user.

    @Override
    @Transactional
    public User register(
            String username,
            String fullName,
            String password) {

        if (username == null || username.isBlank()) {
            throw new InvalidUserDataException(
                    "Username is required"
            );
        }

        // Elegxos morfis tou username.
        if (!username.matches("^[A-Za-z][A-Za-z0-9_]{4,}$")) {
            throw new InvalidUserDataException(
                    "Username must start with a letter, contain only letters, numbers or underscore, and be at least 5 characters long"
            );
        }

        if (fullName == null || fullName.isBlank()) {
            throw new InvalidUserDataException(
                    "Full name is required"
            );
        }

        if (password == null || password.isBlank()) {
            throw new InvalidUserDataException(
                    "Password is required"
            );
        }

        // To password prepei na pliroi tous kanones asfaleias.
        if (!password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
        )) {
            throw new InvalidUserDataException(
                    "Password must be at least 8 characters long and contain uppercase, lowercase, digit and special character"
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(
                    "Username already exists"
            );
        }

        User user = new User();

        user.setUsername(username);
        user.setFullName(fullName);

        // To password apothikeuetai encrypted me BCrypt.
        user.setPassword(passwordEncoder.encode(password));

        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setFailedLoginAttempts(0);

        return userRepository.save(user);
    }


    // Login kai dimiourgia token.

    @Override
    public LoginResponse login(
            String username,
            String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid username or password"
                        )
                );

        if (!user.isActive()) {
            throw new InvalidCredentialsException(
                    "User account is inactive"
            );
        }

        // Elegxos tou password me BCrypt.
        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            int attempts =
                    user.getFailedLoginAttempts() + 1;

            user.setFailedLoginAttempts(attempts);

            // Meta apo 3 apotyximenes prospatheies o user apenergopoieitai.
            if (attempts >= 3) {
                user.setActive(false);
            }

            userRepository.save(user);

            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }

        // Epityximeno login midenizei ta failed attempts.
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        AuthToken authToken = new AuthToken(
                token,
                user
        );

        authTokenRepository.save(authToken);

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                token
        );
    }


    // Energopoiisi user apo ADMIN.

    @Override
    public User activateUser(
            Long userId,
            User admin) {

        if (admin.getRole() != UserRole.ADMIN) {
            throw new ForbiddenOperationException(
                    "Only ADMIN can activate users"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        )
                );

        user.setActive(true);

        // Me tin energopoiisi midenizontai ta failed attempts.
        user.setFailedLoginAttempts(0);

        return userRepository.save(user);
    }


    // Apenergopoiisi user apo ADMIN.

    @Override
    public User deactivateUser(
            Long userId,
            User admin) {

        if (admin.getRole() != UserRole.ADMIN) {
            throw new ForbiddenOperationException(
                    "Only ADMIN can deactivate users"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        )
                );

        user.setActive(false);

        // Me tin apenergopoiisi akyronontai ola ta active tokens.
        List<AuthToken> activeTokens =
                authTokenRepository.findByUserAndActiveTrue(user);

        for (AuthToken token : activeTokens) {
            token.setActive(false);
        }

        authTokenRepository.saveAll(activeTokens);

        return userRepository.save(user);
    }


    // Allagi password.

    @Override
    public User changePassword(
            Long userId,
            String oldPassword,
            String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        )
                );

        if (!user.isActive()) {
            throw new ForbiddenOperationException(
                    "Inactive user cannot change password"
            );
        }

        // Elegxos tou trexontos password.
        if (oldPassword == null
                || !passwordEncoder.matches(
                oldPassword,
                user.getPassword())) {

            int attempts =
                    user.getFailedLoginAttempts() + 1;

            user.setFailedLoginAttempts(attempts);

            // Meta apo 3 lathos prospatheies o user apenergopoieitai.
            if (attempts >= 3) {
                user.setActive(false);
            }

            userRepository.save(user);

            throw new InvalidCredentialsException(
                    "Current password is incorrect"
            );
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidUserDataException(
                    "New password is required"
            );
        }

        // Elegxos ton kanonon asfaleias tou neou password.
        if (!newPassword.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
        )) {
            throw new InvalidUserDataException(
                    "New password must be at least 8 characters long and contain uppercase, lowercase, digit and special character"
            );
        }

        if (newPassword.equals(oldPassword)) {
            throw new InvalidUserDataException(
                    "New password must be different from current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        user.setFailedLoginAttempts(0);

        // Meta tin allagi password akyronontai ola ta active tokens.
        List<AuthToken> activeTokens =
                authTokenRepository.findByUserAndActiveTrue(user);

        for (AuthToken token : activeTokens) {
            token.setActive(false);
        }

        authTokenRepository.saveAll(activeTokens);

        return userRepository.save(user);
    }


    // Enimerosi stoixeion user.

    @Override
    public User updateUser(
            Long userId,
            String username,
            String fullName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        )
                );

        if (!user.isActive()) {
            throw new ForbiddenOperationException(
                    "Inactive user cannot update profile"
            );
        }

        if (username == null || username.isBlank()) {
            throw new InvalidUserDataException(
                    "Username is required"
            );
        }

        if (fullName == null || fullName.isBlank()) {
            throw new InvalidUserDataException(
                    "Full name is required"
            );
        }

        // To neo username prepei na einai monodiko.
        if (!user.getUsername().equals(username)
                && userRepository.existsByUsername(username)) {

            throw new UsernameAlreadyExistsException(
                    "Username already exists"
            );
        }

        user.setUsername(username);
        user.setFullName(fullName);

        return userRepository.save(user);
    }


    // Diagrafi user apo ADMIN.

    @Override
    @Transactional
    public void deleteUser(
            Long userId,
            User admin) {

        if (admin.getRole() != UserRole.ADMIN) {
            throw new ForbiddenOperationException(
                    "Only ADMIN can delete users"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        )
                );

        // User me program role den mporei na diagrafei.
        if (programRoleRepository.existsByUser(user)) {
            throw new UserCannotBeDeletedException(
                    "User cannot be deleted because they have a program role"
            );
        }

        // Submitter screening den mporei na diagrafei.
        if (screeningRepository.existsBySubmitter(user)) {
            throw new UserCannotBeDeletedException(
                    "User cannot be deleted because they are the submitter of a screening"
            );
        }

        // Assigned STAFF screening den mporei na diagrafei.
        if (screeningRepository.existsByAssignedStaff(user)) {
            throw new UserCannotBeDeletedException(
                    "User cannot be deleted because they are assigned to a screening"
            );
        }

        // Diagrafontai prota ta tokens tou user.
        authTokenRepository.deleteByUser(user);

        userRepository.delete(user);
    }


    // Elegxos token.

    @Override
    public User validateToken(String token) {

        if (token == null || token.isBlank()) {
            throw new InvalidCredentialsException(
                    "Token is required"
            );
        }

        // To token prepei na yparxei kai na einai active.
        AuthToken authToken =
                authTokenRepository
                        .findByTokenAndActiveTrue(token)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid or inactive token"
                                )
                        );

        User user = authToken.getUser();

        if (!user.isActive()) {
            throw new InvalidCredentialsException(
                    "User account is inactive"
            );
        }

        return user;
    }


    // Logout kai akyrosi token.

    @Override
    public void logout(String token) {

        if (token == null || token.isBlank()) {
            throw new InvalidCredentialsException(
                    "Token is required"
            );
        }

        AuthToken authToken =
                authTokenRepository
                        .findByTokenAndActiveTrue(token)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid or inactive token"
                                )
                        );

        // To token den mporei na xrisimopoiithei meta to logout.
        authToken.setActive(false);

        authTokenRepository.save(authToken);
    }
}