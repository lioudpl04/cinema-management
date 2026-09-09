package gr.uni.cinema.cinemamanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Monodiko username gia kathe user.
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String password;

    // Rolos tou user sto systima.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // Deixnei an o logariasmos einai energos.
    @Column(nullable = false)
    private boolean active = false;

    // Metritis apotyximenon login attempts.
    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
}