package com.vti.service;

import com.vti.model.Notification;
import com.vti.model.ProgressLog;
import com.vti.model.Task;
import com.vti.model.User;
import com.vti.repository.ProgressLogRepository;
import com.vti.repository.TaskRepository;
import com.vti.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgressLogService {

    @Autowired
    private ProgressLogRepository progressLogRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public ProgressLog addProgress(Integer taskId, Integer userId, Integer percent, String note) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Phân quyền: chỉ cho phép user được assigned hoặc admin mới được cập nhật
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isAssigned = task.getAssignedUsers().contains(user);

        if (!isAdmin && !isAssigned) {
            throw new AccessDeniedException("Bạn không có quyền cập nhật tiến độ task này.");
        }

        ProgressLog log = new ProgressLog();
        log.setTask(task);
        log.setUser(user);
        log.setPercent(percent);
        log.setNote(note);

        ProgressLog savedLog = progressLogRepository.save(log);

        // Trigger thông báo đến tất cả user được assign vào task (trừ người cập nhật)
        String message = user.getFullName() + " đã cập nhật tiến độ task '" + task.getName() + "' lên " + percent + "%";
        for (User u : task.getAssignedUsers()) {
            if (!u.getId().equals(userId)) {
                notificationService.createNotification(u, message);
            }
        }

        return savedLog;
    }

    public List<ProgressLog> getProgressByTask(Integer taskId) {
        return progressLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    public List<ProgressLog> getProgressByUser(Integer userId) {
        return progressLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}