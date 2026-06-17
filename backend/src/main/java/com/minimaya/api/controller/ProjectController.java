package com.minimaya.api.controller;

import com.minimaya.api.dto.CreateProjectRequest;
import com.minimaya.api.dto.ProjectResponse;
import com.minimaya.domain.model.AppUser;
import com.minimaya.domain.model.Project;
import com.minimaya.domain.repository.ProjectRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;

    @GetMapping
    public List<ProjectResponse> listProjects(@AuthenticationPrincipal AppUser user) {
        return projectRepository.findByOwnerIdOrderByUpdatedAtDesc(user.getId())
            .stream().map(ProjectResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest req,
            @AuthenticationPrincipal AppUser user) {
        final Project project = projectRepository.save(Project.builder()
            .name(req.name())
            .description(req.description())
            .owner(user)
            .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectResponse.from(project));
    }

    @GetMapping("/{id}")
    public ProjectResponse getProject(@PathVariable UUID id,
                                      @AuthenticationPrincipal AppUser user) {
        return projectRepository.findById(id)
            .filter(p -> p.getOwner().getId().equals(user.getId()))
            .map(ProjectResponse::from)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable UUID id,
                              @AuthenticationPrincipal AppUser user) {
        projectRepository.findById(id)
            .filter(p -> p.getOwner().getId().equals(user.getId()))
            .ifPresent(projectRepository::delete);
    }
}
