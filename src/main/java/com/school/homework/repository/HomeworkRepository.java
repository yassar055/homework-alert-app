package com.school.homework.repository;

import com.school.homework.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findByTeacherEmailOrderByDueDateDesc(String teacherEmail);
    List<Homework> findByTeacherEmailAndSubjectNameOrderByDueDateDesc(String teacherEmail, String subjectName);
    List<Homework> findAllByOrderByDueDateDesc();
    List<Homework> findBySubjectNameOrderByDueDateDesc(String subjectName);
    long countByTeacherEmailAndDueDate(String teacherEmail, LocalDate dueDate);
    long countByDueDate(LocalDate dueDate);

    // Class-based filtering (for student/parent scoping)
    List<Homework> findByClassNameOrderByDueDateDesc(String className);
    List<Homework> findByClassNameAndSubjectNameOrderByDueDateDesc(String className, String subjectName);
}
