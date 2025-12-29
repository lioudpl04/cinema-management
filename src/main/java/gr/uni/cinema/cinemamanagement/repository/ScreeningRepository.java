package gr.uni.cinema.cinemamanagement.repository;

import gr.uni.cinema.cinemamanagement.entity.Screening;
import gr.uni.cinema.cinemamanagement.entity.ScreeningState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    List<Screening> findByState(ScreeningState state);

    List<Screening> findByProgramId(Long programId);
}
