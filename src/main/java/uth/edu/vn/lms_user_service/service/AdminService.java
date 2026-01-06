package uth.edu.vn.lms_user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uth.edu.vn.lms_user_service.dto.AdminUserDTO;
import uth.edu.vn.lms_user_service.entity.Role;
import uth.edu.vn.lms_user_service.entity.User;
import uth.edu.vn.lms_user_service.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service cho Admin quản lý users
 */
@Service
@Transactional
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    
    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Lấy danh sách tất cả users (có phân trang)
     */
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllUsers(Pageable pageable) {
        log.info("Admin: Lấy danh sách users, page: {}", pageable.getPageNumber());
        return userRepository.findAll(pageable).map(AdminUserDTO::fromUser);
    }

    /**
     * Lấy danh sách users theo role
     */
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getUsersByRole(Role role) {
        log.info("Admin: Lấy danh sách users theo role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(AdminUserDTO::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm users theo keyword (username, email, fullName)
     */
    @Transactional(readOnly = true)
    public List<AdminUserDTO> searchUsers(String keyword) {
        log.info("Admin: Tìm kiếm users với keyword: {}", keyword);
        return userRepository.searchByKeyword(keyword).stream()
                .map(AdminUserDTO::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin chi tiết user theo ID
     */
    @Transactional(readOnly = true)
    public AdminUserDTO getUserById(Long id) {
        log.info("Admin: Lấy thông tin user ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));
        return AdminUserDTO.fromUser(user);
    }

    /**
     * Cập nhật role của user
     */
    public AdminUserDTO updateUserRole(Long userId, Role newRole) {
        log.info("Admin: Cập nhật role user {} thành {}", userId, newRole);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        Role oldRole = user.getRole();
        user.setRole(newRole);
        user = userRepository.save(user);

        log.info("Admin: Đã cập nhật role user {} từ {} thành {}", userId, oldRole, newRole);
        return AdminUserDTO.fromUser(user);
    }

    /**
     * Kích hoạt/vô hiệu hóa user
     */
    public AdminUserDTO toggleUserEnabled(Long userId) {
        log.info("Admin: Toggle enabled user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        user.setEnabled(!user.isEnabled());
        user = userRepository.save(user);

        log.info("Admin: User {} giờ {}", userId, user.isEnabled() ? "enabled" : "disabled");
        return AdminUserDTO.fromUser(user);
    }

    /**
     * Xóa user (soft delete - chỉ disable)
     */
    public void deleteUser(Long userId) {
        log.info("Admin: Xóa user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        // Không cho phép xóa ADMIN
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể xóa tài khoản ADMIN");
        }

        user.setEnabled(false);
        userRepository.save(user);
        
        log.info("Admin: Đã vô hiệu hóa user ID: {}", userId);
    }

    /**
     * Thống kê số lượng users theo role
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.count();
        long adminCount = userRepository.countByRole(Role.ADMIN);
        long teacherCount = userRepository.countByRole(Role.TEACHER);
        long studentCount = userRepository.countByRole(Role.STUDENT);
        long enabledCount = userRepository.countByEnabled(true);
        long disabledCount = userRepository.countByEnabled(false);

        return new UserStatistics(totalUsers, adminCount, teacherCount, studentCount, enabledCount, disabledCount);
    }

    public record UserStatistics(
        long totalUsers,
        long adminCount,
        long teacherCount,
        long studentCount,
        long enabledCount,
        long disabledCount
    ) {}
}
