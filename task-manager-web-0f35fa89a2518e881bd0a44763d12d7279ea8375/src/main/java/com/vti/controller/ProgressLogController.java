package com.vti.controller;

import com.vti.model.ProgressLog;
import com.vti.model.Task;
import com.vti.model.User;
import com.vti.repository.TaskRepository;
import com.vti.repository.UserRepository;
import com.vti.service.AuditLogService;
import com.vti.service.ProgressLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/progress")
public class ProgressLogController {

    private final ProgressLogService progressLogService;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuditLogService auditLogService;

    public ProgressLogController(ProgressLogService progressLogService,
                                 UserRepository userRepository,
                                 TaskRepository taskRepository,
                                 AuditLogService auditLogService) {
        this.progressLogService = progressLogService;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.auditLogService = auditLogService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @PostMapping("/task/{taskId}")
    public ResponseEntity<ProgressLog> addProgress(
            @PathVariable Integer taskId,
            @RequestBody ProgressRequest request,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Task task = taskRepository.findById(taskId).orElseThrow();

        // Chỉ cho phép user được giao task mới được cập nhật tiến độ
        if (!task.getAssignedUsers().contains(user) && !user.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        ProgressLog log = progressLogService.addProgress(
                taskId,
                user.getId(),
                request.getPercent(),
                request.getNote()
        );

        String desc = "Cập nhật tiến độ task ID " + taskId + ": " +
                      request.getPercent() + "% – \"" + request.getNote() + "\"";

        auditLogService.log(principal.getName(), "CREATE", "ProgressLog", log.getId(), desc);

        return ResponseEntity.ok(log);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<ProgressLog>> getProgressByTask(@PathVariable Integer taskId) {
        return ResponseEntity.ok(progressLogService.getProgressByTask(taskId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProgressLog>> getProgressByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(progressLogService.getProgressByUser(userId));
    }

    public static class ProgressRequest {
        private Integer percent;
        private String note;

        public Integer getPercent() {
            return percent;
        }

        public void setPercent(Integer percent) {
            this.percent = percent;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}
