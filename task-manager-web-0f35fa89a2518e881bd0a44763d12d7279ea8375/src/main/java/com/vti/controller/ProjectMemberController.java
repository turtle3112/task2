package com.vti.controller;

import com.vti.model.ProjectMember;
import com.vti.model.User;
import com.vti.repository.UserRepository;
import com.vti.service.AuditLogService;
import com.vti.service.ProjectMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/project-members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public ProjectMemberController(ProjectMemberService projectMemberService, AuditLogService auditLogService, UserRepository userRepository) {
        this.projectMemberService = projectMemberService;
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    // ADMIN thêm member vào project
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> addMember(@RequestBody Map<String, Object> body, Principal principal) {
        Integer projectId = (Integer) body.get("projectId");
        Integer userId = (Integer) body.get("userId");

        if (projectId == null || userId == null) {
            return ResponseEntity.badRequest().body("projectId và userId không được để trống");
        }

        ProjectMember member = projectMemberService.addMember(projectId, userId);

        String desc = "Thêm user ID " + userId + " vào project ID " + projectId;
        auditLogService.log(principal.getName(), "CREATE", "ProjectMember", null, desc);

        return ResponseEntity.ok(member);
    }

    // ADMIN xoá member khỏi project
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeMember(@RequestBody Map<String, Object> body, Principal principal) {
        Integer projectId = (Integer) body.get("projectId");
        Integer userId = (Integer) body.get("userId");

        if (projectId == null || userId == null) {
            return ResponseEntity.badRequest().body("projectId và userId không được để trống");
        }

        projectMemberService.removeMember(projectId, userId);

        String desc = "Xoá user ID " + userId + " khỏi project ID " + projectId;
        auditLogService.log(principal.getName(), "DELETE", "ProjectMember", null, desc);

        return ResponseEntity.noContent().build();
    }

    // ADMIN hoặc người trong project mới được xem danh sách thành viên của project
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProjectMember>> getMembersByProject(@PathVariable Integer projectId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!user.getRole().equals(User.Role.ADMIN)) {
            boolean isInProject = projectMemberService.isUserInProject(projectId, user.getId());
            if (!isInProject) {
                return ResponseEntity.status(403).build();
            }
        }

        return ResponseEntity.ok(projectMemberService.getMembersByProject(projectId));
    }

    // ADMIN hoặc chính user đó mới được xem danh sách project của user
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProjectMember>> getProjectsByUser(@PathVariable Integer userId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!user.getRole().equals(User.Role.ADMIN) && !user.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(projectMemberService.getProjectsByUser(userId));
    }
}
