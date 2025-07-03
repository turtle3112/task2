package com.vti.service;

import com.vti.model.Attachment;
import com.vti.model.Task;
import com.vti.model.User;
import com.vti.repository.AttachmentRepository;
import com.vti.repository.TaskRepository;
import com.vti.repository.UserRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final String uploadDir = "uploads";

    public AttachmentService(AttachmentRepository attachmentRepository,
                             TaskRepository taskRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Attachment upload(Integer taskId, MultipartFile file, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            Files.createDirectories(Paths.get(uploadDir));
            String filePath = uploadDir + File.separator + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            Attachment attachment = new Attachment();
            attachment.setTask(task);
            attachment.setUploadedBy(user);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(filePath);

            Attachment savedAttachment = attachmentRepository.save(attachment);

            // Trigger: Notify assigned users
            notificationService.notifyAttachmentAdded(task, user);

            return savedAttachment;

        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    public List<Attachment> getByTask(Integer taskId) {
        return attachmentRepository.findByTaskId(taskId);
    }

    public Resource download(Integer id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        return new FileSystemResource(attachment.getFilePath());
    }

    public Attachment getMetadata(Integer id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }

    public void deleteAttachment(Integer id, String username) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isUploader = attachment.getUploadedBy().getId().equals(user.getId());

        if (!isAdmin && !isUploader) {
            throw new RuntimeException("You are not authorized to delete this attachment.");
        }

        File file = new File(attachment.getFilePath());
        if (file.exists()) {
            file.delete();
        }

        attachmentRepository.delete(attachment);
    }
}
