package gr.uni.cinema.cinemamanagement.controller;

import gr.uni.cinema.cinemamanagement.dto.ChangeProgramStateRequest;
import gr.uni.cinema.cinemamanagement.dto.CreateProgramRequest;
import gr.uni.cinema.cinemamanagement.dto.ProgramResponse;
import gr.uni.cinema.cinemamanagement.entity.Program;
import gr.uni.cinema.cinemamanagement.entity.User;
import gr.uni.cinema.cinemamanagement.repository.UserRepository;
import gr.uni.cinema.cinemamanagement.service.ProgramMapper;
import gr.uni.cinema.cinemamanagement.service.ProgramService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramService programService;
    private final UserRepository userRepository;

    public ProgramController(
            ProgramService programService,
            UserRepository userRepository
    ) {
        this.programService = programService;
        this.userRepository = userRepository;
    }

    // TEMPORARY: fake user until auth is implemented
    private User mockProgrammer() {
        return userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Mock user not found"));
    }


    @PostMapping
    public Program createProgram(@RequestBody CreateProgramRequest request) {
        return programService.createProgram(
                request.getName(),
                request.getDescription(),
                mockProgrammer()
        );
    }

    @GetMapping("/{id}")
    public Program getProgram(@PathVariable Long id) {
        return programService.getProgramById(id);
    }

    @GetMapping
    public List<ProgramResponse> getAllPrograms() {
        return programService.getAllPrograms()
                .stream()
                .map(ProgramMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/state")
    public ProgramResponse changeState(
            @PathVariable Long id,
            @RequestBody ChangeProgramStateRequest request
    ) {
        Program updated = programService.changeState(
                id,
                request.newState(),
                mockProgrammer()
        );

        return ProgramMapper.toResponse(updated);
    }



}
