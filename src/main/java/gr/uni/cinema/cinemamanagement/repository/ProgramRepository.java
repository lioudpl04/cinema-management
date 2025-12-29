package gr.uni.cinema.cinemamanagement.repository;

import gr.uni.cinema.cinemamanagement.entity.Program;
import gr.uni.cinema.cinemamanagement.entity.ProgramState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByName(String name);

    boolean existsByName(String name);

    List<Program> findByState(ProgramState state);
}
