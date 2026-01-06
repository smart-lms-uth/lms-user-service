package uth.edu.vn.lms_user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uth.edu.vn.lms_user_service.dto.AdminUserDTO;
import uth.edu.vn.lms_user_service.dto.UpdateRoleRequest;
import uth.edu.vn.lms_user_service.entity.Role;
import uth.edu.vn.lms_user_service.service.AdminService;
import uth.edu.vn.lms_user_service.service.AdminService.UserStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho Admin quản lý users
 * Chỉ ADMIN mới có quyền truy cập
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "API quản lý users dành cho Admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "Lấy danh sách tất cả users (phân trang)")
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/users/role/{role}")
    @Operation(summary = "Lấy danh sách users theo role")
    public ResponseEntity<List<AdminUserDTO>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role));
    }

    @GetMapping("/users/search")
    @Operation(summary = "Tìm kiếm users theo keyword")
    public ResponseEntity<List<AdminUserDTO>> searchUsers(@RequestParam String keyword) {
        return ResponseEntity.ok(adminService.searchUsers(keyword));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Lấy thông tin chi tiết user")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Cập nhật role của user")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {
        AdminUserDTO updated = adminService.updateUserRole(id, request.role());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã cập nhật role thành " + request.role());
        response.put("user", updated);
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/{id}/toggle-enabled")
    @Operation(summary = "Bật/tắt trạng thái hoạt động của user")
    public ResponseEntity<Map<String, Object>> toggleUserEnabled(@PathVariable Long id) {
        AdminUserDTO updated = adminService.toggleUserEnabled(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", updated.enabled() ? "Đã kích hoạt user" : "Đã vô hiệu hóa user");
        response.put("user", updated);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Xóa user (soft delete)")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã vô hiệu hóa user");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Thống kê số lượng users")
    public ResponseEntity<UserStatistics> getUserStatistics() {
        return ResponseEntity.ok(adminService.getUserStatistics());
    }

    @GetMapping("/roles")
    @Operation(summary = "Lấy danh sách tất cả roles")
    public ResponseEntity<Role[]> getAllRoles() {
        return ResponseEntity.ok(Role.values());
    }
}
