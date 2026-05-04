package com.school.homework.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "homework")
public class Homework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subjectName;

    private String className;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String bookName;

    @Column(nullable = false)
    private int pageFrom;

    private int pageTo;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate assignedDate = LocalDate.now();

    @Column(nullable = false)
    private String teacherEmail;

    private String teacherName;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Homework() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public int getPageFrom() { return pageFrom; }
    public void setPageFrom(int pageFrom) { this.pageFrom = pageFrom; }
    public int getPageTo() { return pageTo; }
    public void setPageTo(int pageTo) { this.pageTo = pageTo; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
    public String getTeacherEmail() { return teacherEmail; }
    public void setTeacherEmail(String teacherEmail) { this.teacherEmail = teacherEmail; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getPageRange() {
        return pageTo > 0 && pageTo != pageFrom ? pageFrom + "-" + pageTo : String.valueOf(pageFrom);
    }
}
