package com.vti.controller;

import com.vti.model.Project;
import com.vti.model.User;
import com.vti.repository.UserRepository;
import com.vti.service.AuditLogService;
import com.vti.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ProjectController(ProjectService projectService, UserRepository userRepository, AuditLogService auditLogService) {
        this.projectService = projectService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Project> create(@RequestBody Project project, Principal principal) {
        User admin = userRepository.findByUsername(principal.getName()).orElseThrow();
        project.setCreatedBy(admin);
        Project created = projectService.createProject(project);

        String desc = "Tạo mới project ID " + created.getId() + ": " + created.getName();
        auditLogService.log(principal.getName(), "CREATE", "Project", created.getId(), desc);

        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Project> update(@PathVariable Integer id, @RequestBody Project project, Principal principal) {
        Project updated = projectService.updateProject(id, project);

        String desc = "Cập nhật project ID " + id + ": " + updated.getName();
        auditLogService.log(principal.getName(), "UPDATE", "Project", id, desc);

        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, Principal principal) {
        projectService.deleteProject(id);

        String desc = "Xoá project ID " + id;
        auditLogService.log(principal.getName(), "DELETE", "Project", id, desc);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAll() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }
}
