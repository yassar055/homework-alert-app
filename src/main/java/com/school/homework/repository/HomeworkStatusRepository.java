package com.school.homework.repository;

import com.school.homework.entity.HomeworkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HomeworkStatusRepository extends JpaRepository<HomeworkStatus, Long> {
    Optional<HomeworkStatus> findByHomeworkIdAndStudentEmail(Long homeworkId, String studentEmail);
    List<HomeworkStatus> findByStudentEmail(String studentEmail);
    List<HomeworkStatus> findByHomeworkId(Long homeworkId);
    long countByHomeworkIdAndStatus(Long homeworkId, String status);
}
