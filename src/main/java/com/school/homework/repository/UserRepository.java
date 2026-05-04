package com.school.homework.repository;

import com.school.homework.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AppUser> findByRole(AppUser.Role role);
    List<AppUser> findByStudentEmail(String studentEmail);
}
