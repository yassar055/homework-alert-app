package com.school.homework.repository;

import com.school.homework.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByTeacherEmail(String teacherEmail);
    List<Subject> findByTeacherEmailOrderByNameAsc(String teacherEmail);

    // Class-based filtering (for student/parent scoping)
    List<Subject> findByClassNameOrderByNameAsc(String className);
}
