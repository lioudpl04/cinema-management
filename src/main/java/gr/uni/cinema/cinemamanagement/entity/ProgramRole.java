package gr.uni.cinema.cinemamanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "program_roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "program_id"})
        }
)
public class ProgramRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // O user pou exei ton rolo sto sygkekrimeno program.
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    // To program sto opoio isxyei o rolos.
    @ManyToOne(optional = false)
    @JoinColumn(name = "program_id")
    private Program program;

    // O rolos tou user mesa sto program.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgramRoleType role;

    public ProgramRole() {
    }

    public ProgramRole(
            User user,
            Program program,
            ProgramRoleType role) {
        this.user = user;
        this.program = program;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public ProgramRoleType getRole() {
        return role;
    }

    public void setRole(ProgramRoleType role) {
        this.role = role;
    }
}