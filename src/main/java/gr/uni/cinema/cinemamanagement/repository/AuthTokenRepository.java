package gr.uni.cinema.cinemamanagement.repository;

import gr.uni.cinema.cinemamanagement.entity.AuthToken;
import gr.uni.cinema.cinemamanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthTokenRepository
        extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTokenAndActiveTrue(String token);

    List<AuthToken> findByUserAndActiveTrue(User user);

    void deleteByUser(User user);
}