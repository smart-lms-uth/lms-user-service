package uth.edu.vn.lms_user_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uth.edu.vn.lms_user_service.dto.AdminUserDTO;
import uth.edu.vn.lms_user_service.dto.UserStatisticsResponse;
import uth.edu.vn.lms_user_service.dto.AdminUpdateUserRequest;
import uth.edu.vn.lms_user_service.entity.Role;

import java.util.List;

public interface IAdminService {
    
    Page<AdminUserDTO> getAllUsers(Pageable pageable);
    
    List<AdminUserDTO> getUsersByRole(Role role);
    
    List<AdminUserDTO> searchUsers(String keyword);
    
    AdminUserDTO getUserById(Long id);
    
    AdminUserDTO updateUserRole(Long id, Role newRole);
    
    AdminUserDTO toggleUserEnabled(Long id);
    
    void deleteUser(Long id);
    
    UserStatisticsResponse getUserStatistics();

    AdminUserDTO updateUser(Long userId, AdminUpdateUserRequest request);

    AdminUserDTO lockUser(Long userId, String reason);

    AdminUserDTO unlockUser(Long userId);

    void hardDeleteUser(Long userId);
}
