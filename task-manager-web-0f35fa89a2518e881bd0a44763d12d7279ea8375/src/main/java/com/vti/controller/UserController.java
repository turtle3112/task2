package com.vti.controller;

import com.vti.model.User;
import com.vti.repository.UserRepository;
import com.vti.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        User user = userRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Integer id,
            @RequestBody User updatedUser,
            Principal principal
    ) {
        User user = userRepository.findById(id).orElseThrow();

        user.setFullName(updatedUser.getFullName());
        user.setRole(updatedUser.getRole());
        user.setEmployeeId(updatedUser.getEmployeeId());

        User saved = userRepository.save(user);

        String desc = "Cập nhật thông tin user ID " + id;
        auditLogService.log(principal.getName(), "UPDATE", "User", id, desc);

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findById(id).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        String desc = "Thay đổi mật khẩu user ID " + id;
        auditLogService.log(principal.getName(), "PASSWORD_CHANGE", "User", id, desc);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id, Principal principal) {
        userRepository.deleteById(id);

        String desc = "Xoá user ID " + id;
        auditLogService.log(principal.getName(), "DELETE", "User", id, desc);

        return ResponseEntity.noContent().build();
    }
}
