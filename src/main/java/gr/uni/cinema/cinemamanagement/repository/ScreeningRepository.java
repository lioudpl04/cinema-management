package gr.uni.cinema.cinemamanagement.repository;

import gr.uni.cinema.cinemamanagement.entity.Screening;
import gr.uni.cinema.cinemamanagement.entity.ScreeningState;
import gr.uni.cinema.cinemamanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningRepository
        extends JpaRepository<Screening, Long> {

    List<Screening> findByState(ScreeningState state);

    List<Screening> findByProgramId(Long programId);

    // Screenings created by a specific submitter
    List<Screening> findBySubmitter(User submitter);

    // Screenings assigned to a specific staff member
    List<Screening> findByAssignedStaff(User staff);

    boolean existsBySubmitter(User submitter);

    boolean existsByAssignedStaff(User staff);

    // Screenings of a program with a specific state
    List<Screening> findByProgramIdAndState(
            Long programId,
            ScreeningState state
    );

    List<Screening> findByProgramIdAndStateAndFinalSubmittedFalse(
            Long programId,
            ScreeningState state
    );
}