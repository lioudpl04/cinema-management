package gr.uni.cinema.cinemamanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "screenings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stoixeia tainias
    private String movieTitle;

    @Column(name = "movie_cast")
    private String cast;

    private String genres;

    private Integer durationMinutes;


    // Stoixeia programmatismou
    private String auditorium;

    private LocalDateTime startTime;

    private LocalDateTime endTime;


    // Trexon state tou screening
    @Enumerated(EnumType.STRING)
    private ScreeningState state = ScreeningState.CREATED;


    // Program sto opoio anikei to screening
    @ManyToOne
    @JoinColumn(name = "program_id")
    private Program program;


    // User pou dimiourgise kai kanei submit to screening
    @ManyToOne
    @JoinColumn(name = "submitter_id")
    private User submitter;


    // STAFF pou exei analavei to review
    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;


    // Stoixeia review
    @Column(length = 2000)
    private String reviewComments;

    private LocalDateTime reviewedAt;

    private Integer reviewScore;

    @Column(length = 2000)
    private String approvalNotes;


    // Final submission
    private boolean finalSubmitted = false;

    private LocalDateTime finalSubmittedAt;


    // Logos aporripsis
    @Column(length = 2000)
    private String rejectionReason;
}