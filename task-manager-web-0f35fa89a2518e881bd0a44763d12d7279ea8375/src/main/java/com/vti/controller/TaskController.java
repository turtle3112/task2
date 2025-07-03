package com.vti.controller;

import com.vti.model.Project;
import com.vti.model.Task;
import com.vti.model.User;
import com.vti.repository.ProjectRepository;
import com.vti.repository.UserRepository;
import com.vti.service.AuditLogService;
import com.vti.service.ProjectMemberService;
import com.vti.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberService projectMemberService;
    private final AuditLogService auditLogService;

    public TaskController(TaskService taskService,
                          UserRepository userRepository,
                          ProjectRepository projectRepository,
                          ProjectMemberService projectMemberService,
                          AuditLogService auditLogService) {
        this.taskService = taskService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectMemberService = projectMemberService;
        this.auditLogService = auditLogService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Map<String, Object> request, Principal principal) {
        Task task = parseTaskFromRequest(request);
        Task created = taskService.createTask(task);

        String desc = "Tạo task ID " + created.getId() + ": " + created.getName()
                + " trong project ID " + created.getProject().getId();
        auditLogService.log(principal.getName(), "CREATE", "Task", created.getId(), desc);

        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Integer id, @RequestBody Map<String, Object> request, Principal principal) {
        Task task = parseTaskFromRequest(request);
        Task updated = taskService.updateTask(id, task);

        String desc = "Cập nhật task ID " + id + ": " + updated.getName();
        auditLogService.log(principal.getName(), "UPDATE", "Task", id, desc);

        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, Principal principal) {
        taskService.deleteTask(id);

        String desc = "Xoá task ID " + id;
        auditLogService.log(principal.getName(), "DELETE", "Task", id, desc);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getByProject(@PathVariable Integer projectId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!user.getRole().equals(User.Role.ADMIN)) {
            boolean isInProject = projectMemberService.isUserInProject(projectId, user.getId());
            if (!isInProject) {
                return ResponseEntity.status(403).build();
            }
        }

        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable Integer id,
                                             @RequestBody Task request,
                                             Principal principal) {
        Task updated = taskService.updateStatus(id, request.getStatus(), principal.getName());

        String desc = "Cập nhật trạng thái task ID " + id + " thành " + updated.getStatus();
        auditLogService.log(principal.getName(), "STATUS_CHANGE", "Task", id, desc);

        return ResponseEntity.ok(updated);
    }

    private Task parseTaskFromRequest(Map<String, Object> request) {
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        String status = (String) request.get("status");
        String deadlineStr = (String) request.get("deadline");

        Integer projectId = (Integer) request.get("project");
        List<Integer> assignedUserIds = (List<Integer>) request.get("assignedUsers");

        Project project = projectRepository.findById(projectId).orElseThrow();
        Set<User> assignedUsers = assignedUserIds.stream()
                .map(id -> userRepository.findById(id).orElseThrow())
                .collect(Collectors.toSet());

        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setStatus(Task.Status.valueOf(status));
        task.setDeadline(LocalDate.parse(deadlineStr));
        task.setProject(project);
        task.setAssignedUsers(assignedUsers);

        return task;
    }
}
