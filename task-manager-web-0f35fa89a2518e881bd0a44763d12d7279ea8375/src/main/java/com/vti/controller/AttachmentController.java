package com.vti.controller;

import com.vti.model.Attachment;
import com.vti.service.AttachmentService;
import com.vti.service.AuditLogService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final AuditLogService auditLogService;

    public AttachmentController(AttachmentService attachmentService, AuditLogService auditLogService) {
        this.attachmentService = attachmentService;
        this.auditLogService = auditLogService;
    }

    // Upload file
    @PostMapping("/task/{taskId}")
    public ResponseEntity<Attachment> upload(@PathVariable Integer taskId,
                                             @RequestParam("file") MultipartFile file,
                                             Principal principal) {
        Attachment attachment = attachmentService.upload(taskId, file, principal.getName());

        String desc = "Upload file \"" + attachment.getFileName() + "\" vào task ID " + taskId;
        auditLogService.log(principal.getName(), "CREATE", "Attachment", attachment.getId(), desc);

        return ResponseEntity.ok(attachment);
    }

    // Danh sách file theo task
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Attachment>> getByTask(@PathVariable Integer taskId) {
        return ResponseEntity.ok(attachmentService.getByTask(taskId));
    }

    // Tải file
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Integer id) {
        Resource file = attachmentService.download(id);
        Attachment attachment = attachmentService.getMetadata(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(file);
    }

    // Xem file
    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource> preview(@PathVariable Integer id) {
        Resource file = attachmentService.download(id);
        return ResponseEntity.ok().body(file);
    }

    // Xoá file (admin hoặc người tạo)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, Principal principal) {
        attachmentService.deleteAttachment(id, principal.getName());

        String desc = "Xoá file đính kèm ID " + id;
        auditLogService.log(principal.getName(), "DELETE", "Attachment", id, desc);

        return ResponseEntity.noContent().build();
    }
}
