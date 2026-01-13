package uth.edu.vn.lms_user_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import uth.edu.vn.lms_user_service.entity.AuthProvider;
import uth.edu.vn.lms_user_service.entity.Role;
import uth.edu.vn.lms_user_service.entity.User;
import uth.edu.vn.lms_user_service.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                   PasswordEncoder passwordEncoder,
                                   Environment env) {
        return args -> {
            createDefaultAdmin(userRepository, passwordEncoder, env);
            
            if (isDevProfile(env)) {
                createSampleTeacher(userRepository, passwordEncoder);
                createSampleStudent(userRepository, passwordEncoder);
            }
        };
    }

    private void createDefaultAdmin(UserRepository userRepository, 
                                    PasswordEncoder passwordEncoder,
                                    Environment env) {
        String adminEmail = env.getProperty("app.admin.email", "admin@uth.edu.vn");
        String adminUsername = env.getProperty("app.admin.username", "admin");
        String adminPassword = env.getProperty("app.admin.password", "Admin@123");

        if (!userRepository.existsByEmail(adminEmail) && !userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFullName("System Administrator");
            admin.setRole(Role.ADMIN);
            admin.setAuthProvider(AuthProvider.LOCAL);
            admin.setEnabled(true);
            admin.setEmailVerified(true);
            admin.setProfileCompleted(true);

            userRepository.save(admin);
        }
    }

    private void createSampleTeacher(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        String teacherEmail = "teacher@uth.edu.vn";
        
        if (!userRepository.existsByEmail(teacherEmail)) {
            User teacher = new User();
            teacher.setUsername("teacher");
            teacher.setEmail(teacherEmail);
            teacher.setPassword(passwordEncoder.encode("Teacher@123"));
            teacher.setFullName("Giảng viên Mẫu");
            teacher.setRole(Role.TEACHER);
            teacher.setAuthProvider(AuthProvider.LOCAL);
            teacher.setEnabled(true);
            teacher.setEmailVerified(true);

            userRepository.save(teacher);
        }
    }

    private void createSampleStudent(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        String studentEmail = "student@uth.edu.vn";
        
        if (!userRepository.existsByEmail(studentEmail)) {
            User student = new User();
            student.setUsername("student");
            student.setEmail(studentEmail);
            student.setPassword(passwordEncoder.encode("Student@123"));
            student.setFullName("Sinh viên Mẫu");
            student.setRole(Role.STUDENT);
            student.setAuthProvider(AuthProvider.LOCAL);
            student.setEnabled(true);
            student.setEmailVerified(true);

            userRepository.save(student);
        }
    }

    private boolean isDevProfile(Environment env) {
        String[] activeProfiles = env.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("dev".equalsIgnoreCase(profile) || "docker".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return activeProfiles.length == 0;
    }
}
