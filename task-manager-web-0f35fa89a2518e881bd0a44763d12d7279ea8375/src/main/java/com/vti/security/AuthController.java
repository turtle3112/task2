package com.vti.security;

import com.vti.model.User;
import com.vti.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	

	public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
			UserRepository userRepository, PasswordEncoder passwordEncoder) {
		super();
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
		String username = loginRequest.get("username");
		String password = loginRequest.get("password");

		Authentication auth = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));

		String token = jwtTokenProvider.generateToken(username);
		User user = userRepository.findByUsername(username).orElseThrow();

		return ResponseEntity.ok(Map.of("token", token, "role", user.getRole().name()));

	}
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/v2/register")
	public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
	    String username = request.get("username");
	    String password = request.get("password");
	    String fullName = request.get("fullName");
	    String roleStr = request.get("role");
	    String employeeId = request.get("employeeId");

	    // Validate: tất cả các trường bắt buộc
	    if (username == null || username.isBlank()) {
	        return ResponseEntity.badRequest().body("Username không được để trống");
	    }
	    if (password == null || password.isBlank()) {
	        return ResponseEntity.badRequest().body("Password không được để trống");
	    }
	    if (password.length() < 6) {
	        return ResponseEntity.badRequest().body("Password phải có ít nhất 6 ký tự");
	    }
	    if (fullName == null || fullName.isBlank()) {
	        return ResponseEntity.badRequest().body("Full name không được để trống");
	    }
	    if (roleStr == null || roleStr.isBlank()) {
	        return ResponseEntity.badRequest().body("Role không được để trống");
	    }
	    if (!roleStr.equalsIgnoreCase("ADMIN") && !roleStr.equalsIgnoreCase("EMPLOYEE")) {
	        return ResponseEntity.badRequest().body("Role không hợp lệ. Chỉ chấp nhận ADMIN hoặc EMPLOYEE");
	    }
	    if (employeeId == null || employeeId.isBlank()) {
	        return ResponseEntity.badRequest().body("Employee ID không được để trống");
	    }

	    // Kiểm tra trùng
	    if (userRepository.findByUsername(username).isPresent()) {
	        return ResponseEntity.badRequest().body("Username đã tồn tại");
	    }
	    if (userRepository.findByEmployeeId(employeeId).isPresent()) {
	        return ResponseEntity.badRequest().body("Employee ID đã tồn tại");
	    }

	    // Tạo user mới
	    User user = new User();
	    user.setUsername(username);
	    user.setPassword(passwordEncoder.encode(password));
	    user.setFullName(fullName);
	    user.setRole(User.Role.valueOf(roleStr.toUpperCase()));
	    user.setEmployeeId(employeeId);

	    userRepository.save(user);
	    return ResponseEntity.ok("Đăng ký thành công");
	}


}