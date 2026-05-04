package com.school.homework.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "homework_status", uniqueConstraints = @UniqueConstraint(columnNames = {"homeworkId", "studentEmail"}))
public class HomeworkStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long homeworkId;

    @Column(nullable = false)
    private String studentEmail;

    private String studentName;

    @Column(nullable = false)
    private String status = "pending"; // pending, completed

    private LocalDateTime completedAt;

    public HomeworkStatus() {}

    public Long getId() { return id; }
    public Long getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
