package uth.edu.vn.lms_user_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uth.edu.vn.lms_user_service.dto.AdminUserDTO;
import uth.edu.vn.lms_user_service.dto.UserStatisticsResponse;
import uth.edu.vn.lms_user_service.dto.AdminUpdateUserRequest;
import uth.edu.vn.lms_user_service.entity.Role;
import uth.edu.vn.lms_user_service.entity.User;
import uth.edu.vn.lms_user_service.exception.ApiException;
import uth.edu.vn.lms_user_service.exception.ResourceNotFoundException;
import uth.edu.vn.lms_user_service.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService implements IAdminService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;

    public AdminService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserDTO::fromUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(AdminUserDTO::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDTO> searchUsers(String keyword) {
        return userRepository.searchByKeyword(keyword).stream()
                .map(AdminUserDTO::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return AdminUserDTO.fromUser(user);
    }

    @Override
    public AdminUserDTO updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setRole(newRole);
        user = userRepository.save(user);
        return AdminUserDTO.fromUser(user);
    }

    @Override
    public AdminUserDTO toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(!user.isEnabled());
        user = userRepository.save(user);
        return AdminUserDTO.fromUser(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == Role.ADMIN) {
            throw ApiException.forbidden("Cannot delete admin account");
        }

        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics() {
        long totalUsers = userRepository.count();
        long adminCount = userRepository.countByRole(Role.ADMIN);
        long teacherCount = userRepository.countByRole(Role.TEACHER);
        long studentCount = userRepository.countByRole(Role.STUDENT);
        long enabledCount = userRepository.countByEnabled(true);
        long disabledCount = userRepository.countByEnabled(false);

        return new UserStatisticsResponse(totalUsers, adminCount, teacherCount, studentCount, enabledCount, disabledCount);
    }

    @Override
    public AdminUserDTO updateUser(Long userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName());
        }
        if (request.email() != null && !request.email().isBlank()) {
            if (userRepository.existsByEmailAndIdNot(request.email(), userId)) {
                throw ApiException.badRequest("Email already in use");
            }
            user.setEmail(request.email());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        if (request.emailVerified() != null) {
            user.setEmailVerified(request.emailVerified());
        }

        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        return AdminUserDTO.fromUser(user);
    }

    @Override
    public AdminUserDTO lockUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == Role.ADMIN) {
            throw ApiException.forbidden("Cannot lock admin account");
        }

        user.setEnabled(false);
        user.setAccountLocked(true);
        user.setLockReason(reason);
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        emailService.sendAccountLockedEmail(user.getEmail(), user.getFullName(), reason);

        return AdminUserDTO.fromUser(user);
    }

    @Override
    public AdminUserDTO unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setLockReason(null);
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        return AdminUserDTO.fromUser(user);
    }

    @Override
    public void hardDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == Role.ADMIN) {
            throw ApiException.forbidden("Cannot delete admin account");
        }

        userRepository.delete(user);
    }
}
