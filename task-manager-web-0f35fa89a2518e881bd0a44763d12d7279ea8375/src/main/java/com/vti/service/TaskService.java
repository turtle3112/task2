package com.vti.service;

import com.vti.model.Project;
import com.vti.model.Task;
import com.vti.model.User;
import com.vti.repository.ProjectRepository;
import com.vti.repository.TaskRepository;
import com.vti.repository.UserRepository;
import com.vti.service.ProjectMemberService;
import com.vti.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberService projectMemberService;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       ProjectMemberService projectMemberService,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberService = projectMemberService;
        this.notificationService = notificationService;
    }

    public Task createTask(Task task) {
        Task savedTask = taskRepository.save(task);
        List<String> usernames = task.getAssignedUsers().stream().map(User::getUsername).toList();
        notificationService.notifyTaskAssignment(savedTask.getId(), usernames);
        return savedTask;
    }

    public Task updateTask(Integer taskId, Task updatedTask) {
        Task task = taskRepository.findById(taskId).orElseThrow();

        Task.Status oldStatus = task.getStatus();
        Set<User> oldUsers = new HashSet<>(task.getAssignedUsers());

        task.setName(updatedTask.getName());
        task.setDescription(updatedTask.getDescription());
        task.setDeadline(updatedTask.getDeadline());
        task.setStatus(updatedTask.getStatus());
        task.setProject(updatedTask.getProject());
        task.setAssignedUsers(updatedTask.getAssignedUsers());

        Task savedTask = taskRepository.save(task);

        if (!oldStatus.equals(updatedTask.getStatus())) {
            notificationService.notifyTaskStatusChanged(task, oldStatus);
        }

        Set<User> newUsers = new HashSet<>(updatedTask.getAssignedUsers());
        newUsers.removeAll(oldUsers);
        List<String> newUsernames = newUsers.stream().map(User::getUsername).toList();
        if (!newUsernames.isEmpty()) {
            notificationService.notifyTaskAssignment(taskId, newUsernames);
        }

        return savedTask;
    }

    public void deleteTask(Integer taskId) {
        taskRepository.deleteById(taskId);
    }

    public List<Task> getTasksByProject(Integer projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        return taskRepository.findByProject(project);
    }

    public List<Task> getTasksAssignedToUser(User user) {
        return taskRepository.findByAssignedUsersContaining(user);
    }

    public Task updateStatus(Integer taskId, Task.Status status, String username) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();

        if (!task.getAssignedUsers().contains(user)) {
            throw new SecurityException("Không có quyền cập nhật task này");
        }

        Task.Status oldStatus = task.getStatus();
        task.setStatus(status);
        Task savedTask = taskRepository.save(task);
        if (!oldStatus.equals(status)) {
            notificationService.notifyTaskStatusChanged(savedTask, oldStatus);
        }
        return savedTask;
    }
}
