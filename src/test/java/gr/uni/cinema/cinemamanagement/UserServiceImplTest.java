package gr.uni.cinema.cinemamanagement;

import gr.uni.cinema.cinemamanagement.dto.LoginResponse;
import gr.uni.cinema.cinemamanagement.entity.AuthToken;
import gr.uni.cinema.cinemamanagement.entity.User;
import gr.uni.cinema.cinemamanagement.entity.UserRole;
import gr.uni.cinema.cinemamanagement.exception.InvalidCredentialsException;
import gr.uni.cinema.cinemamanagement.exception.InvalidUserDataException;
import gr.uni.cinema.cinemamanagement.exception.UsernameAlreadyExistsException;
import gr.uni.cinema.cinemamanagement.repository.AuthTokenRepository;
import gr.uni.cinema.cinemamanagement.repository.ProgramRoleRepository;
import gr.uni.cinema.cinemamanagement.repository.ScreeningRepository;
import gr.uni.cinema.cinemamanagement.repository.UserRepository;
import gr.uni.cinema.cinemamanagement.service.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private ProgramRoleRepository programRoleRepository;

    @Mock
    private ScreeningRepository screeningRepository;

    private UserServiceImpl userService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {

        userService = new UserServiceImpl(
                userRepository,
                authTokenRepository,
                programRoleRepository,
                screeningRepository
        );
    }


    // =========================================================
    // REGISTER
    // =========================================================

    @Test
    void registerCreatesActiveUser() {

        when(userRepository.existsByUsername("testuser"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.register(
                "testuser",
                "Test User",
                "Test123!"
        );

        assertEquals("testuser", user.getUsername());
        assertEquals("Test User", user.getFullName());
        assertEquals(UserRole.USER, user.getRole());
        assertTrue(user.isActive());
        assertEquals(0, user.getFailedLoginAttempts());

        assertNotNull(user.getPassword());

        assertTrue(
                passwordEncoder.matches(
                        "Test123!",
                        user.getPassword()
                )
        );

        verify(userRepository)
                .save(any(User.class));
    }


    @Test
    void registerRejectsDuplicateUsername() {

        when(userRepository.existsByUsername("existing"))
                .thenReturn(true);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.register(
                        "existing",
                        "Existing User",
                        "Test123!"
                )
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    @Test
    void registerRejectsBlankUsername() {

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.register(
                        "",
                        "Test User",
                        "Test123!"
                )
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    @Test
    void registerRejectsBlankFullName() {

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.register(
                        "testuser",
                        "",
                        "Test123!"
                )
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    @Test
    void registerRejectsBlankPassword() {

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.register(
                        "testuser",
                        "Test User",
                        ""
                )
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    @Test
    void registerRejectsInvalidUsernameFormat() {

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.register(
                        "1user",
                        "Test User",
                        "Test123!"
                )
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    @Test
    void registerRejectsUsernameShorterThanFiveCharacters() {

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.register(
                        "user",
                        "Test User",
                        "Test123!"
                )
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    @Test
    void registerRejectsWeakPassword() {

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.register(
                        "testuser",
                        "Test User",
                        "password"
                )
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @Test
    void loginRejectsUnknownUsername() {

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(
                        "unknown",
                        "1234"
                )
        );

        verify(authTokenRepository, never())
                .save(any(AuthToken.class));
    }


    @Test
    void loginRejectsInactiveUser() {

        User user = new User();

        user.setUsername("inactive");
        user.setFullName("Inactive User");
        user.setPassword(
                passwordEncoder.encode("Test123!")
        );
        user.setRole(UserRole.USER);
        user.setActive(false);

        when(userRepository.findByUsername("inactive"))
                .thenReturn(Optional.of(user));

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(
                        "inactive",
                        "Test123!"
                )
        );

        verify(authTokenRepository, never())
                .save(any(AuthToken.class));
    }


    @Test
    void loginWithCorrectPasswordCreatesToken() {

        User user = createActiveUser(
                1L,
                "testuser",
                "Test123!"
        );

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(authTokenRepository.save(any(AuthToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = userService.login(
                "testuser",
                "Test123!"
        );

        assertNotNull(response);
        assertEquals(0, user.getFailedLoginAttempts());

        verify(authTokenRepository)
                .save(any(AuthToken.class));
    }


    @Test
    void threeFailedLoginAttemptsDeactivateUser() {

        User user = createActiveUser(
                1L,
                "testuser",
                "Test123!"
        );

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(
                        "testuser",
                        "Wrong123!"
                )
        );

        assertEquals(1, user.getFailedLoginAttempts());
        assertTrue(user.isActive());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(
                        "testuser",
                        "Wrong123!"
                )
        );

        assertEquals(2, user.getFailedLoginAttempts());
        assertTrue(user.isActive());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(
                        "testuser",
                        "Wrong123!"
                )
        );

        assertEquals(3, user.getFailedLoginAttempts());
        assertFalse(user.isActive());

        verify(userRepository, times(3))
                .save(user);
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @Test
    void changePasswordRejectsWeakNewPassword() {

        User user = createActiveUser(
                1L,
                "testuser",
                "OldPass1!"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.changePassword(
                        1L,
                        "OldPass1!",
                        "weak"
                )
        );

        verify(authTokenRepository, never())
                .saveAll(any());
    }


    @Test
    void changePasswordRejectsSamePassword() {

        User user = createActiveUser(
                1L,
                "testuser",
                "Test123!"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.changePassword(
                        1L,
                        "Test123!",
                        "Test123!"
                )
        );

        verify(authTokenRepository, never())
                .saveAll(any());
    }


    @Test
    void changePasswordWithWrongCurrentPasswordIncrementsAttempts() {

        User user = createActiveUser(
                1L,
                "testuser",
                "OldPass1!"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.changePassword(
                        1L,
                        "WrongPass1!",
                        "NewPass1!"
                )
        );

        assertEquals(
                1,
                user.getFailedLoginAttempts()
        );

        assertTrue(user.isActive());

        verify(userRepository)
                .save(user);
    }


    @Test
    void threeWrongCurrentPasswordAttemptsDeactivateUser() {

        User user = createActiveUser(
                1L,
                "testuser",
                "OldPass1!"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.changePassword(
                        1L,
                        "WrongPass1!",
                        "NewPass1!"
                )
        );

        assertEquals(1, user.getFailedLoginAttempts());
        assertTrue(user.isActive());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.changePassword(
                        1L,
                        "WrongPass1!",
                        "NewPass1!"
                )
        );

        assertEquals(2, user.getFailedLoginAttempts());
        assertTrue(user.isActive());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.changePassword(
                        1L,
                        "WrongPass1!",
                        "NewPass1!"
                )
        );

        assertEquals(3, user.getFailedLoginAttempts());
        assertFalse(user.isActive());

        verify(userRepository, times(3))
                .save(user);
    }


    @Test
    void successfulPasswordChangeInvalidatesActiveTokens() {

        User user = createActiveUser(
                1L,
                "testuser",
                "OldPass1!"
        );

        user.setFailedLoginAttempts(2);

        AuthToken token1 =
                new AuthToken("token-1", user);

        AuthToken token2 =
                new AuthToken("token-2", user);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(authTokenRepository.findByUserAndActiveTrue(user))
                .thenReturn(List.of(
                        token1,
                        token2
                ));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.changePassword(
                1L,
                "OldPass1!",
                "NewPass1!"
        );

        assertTrue(
                passwordEncoder.matches(
                        "NewPass1!",
                        result.getPassword()
                )
        );

        assertEquals(
                0,
                result.getFailedLoginAttempts()
        );

        assertFalse(token1.isActive());
        assertFalse(token2.isActive());

        verify(authTokenRepository)
                .saveAll(any());
    }


    // =========================================================
    // TOKEN
    // =========================================================

    @Test
    void validateTokenRejectsInvalidToken() {

        when(authTokenRepository
                .findByTokenAndActiveTrue("bad-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.validateToken(
                        "bad-token"
                )
        );
    }


    @Test
    void validateTokenReturnsActiveUser() {

        User user = new User();

        user.setUsername("testuser");
        user.setFullName("Test User");
        user.setRole(UserRole.USER);
        user.setActive(true);

        AuthToken authToken =
                new AuthToken(
                        "valid-token",
                        user
                );

        when(authTokenRepository
                .findByTokenAndActiveTrue("valid-token"))
                .thenReturn(
                        Optional.of(authToken)
                );

        User result =
                userService.validateToken(
                        "valid-token"
                );

        assertSame(user, result);
    }


    // =========================================================
    // HELPER
    // =========================================================

    private User createActiveUser(
            Long id,
            String username,
            String rawPassword) {

        User user = new User();

        user.setId(id);
        user.setUsername(username);
        user.setFullName("Test User");
        user.setPassword(
                passwordEncoder.encode(rawPassword)
        );
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setFailedLoginAttempts(0);

        return user;
    }
}