package gr.uni.cinema.cinemamanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "programs")
@Getter
@Setter
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    // To state apothikeuetai sti vasi os String.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgramState state = ProgramState.CREATED;

    // O user pou dimiourgise to program.
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User creator;

    private LocalDateTime createdAt = LocalDateTime.now();
}