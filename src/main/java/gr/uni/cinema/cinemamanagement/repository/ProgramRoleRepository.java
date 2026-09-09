package gr.uni.cinema.cinemamanagement.repository;

import gr.uni.cinema.cinemamanagement.entity.Program;
import gr.uni.cinema.cinemamanagement.entity.ProgramRole;
import gr.uni.cinema.cinemamanagement.entity.ProgramRoleType;
import gr.uni.cinema.cinemamanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRoleRepository
        extends JpaRepository<ProgramRole, Long> {

    Optional<ProgramRole> findByUserAndProgram(
            User user,
            Program program
    );

    boolean existsByUserAndProgramAndRole(
            User user,
            Program program,
            ProgramRoleType role
    );

    boolean existsByUser(
            User user
    );

    void deleteByProgram(
            Program program
    );
}