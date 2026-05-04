package com.school.homework.config;

import com.school.homework.entity.*;
import com.school.homework.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedData(UserRepository userRepo, SubjectRepository subjectRepo,
                               HomeworkRepository hwRepo, PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() > 0) return;

            String pwd = encoder.encode("password123");

            // ─── Master Admin (all access) ───────────────────
            AppUser admin = new AppUser("Principal Kumar", "admin@school.com", pwd, AppUser.Role.ADMIN);
            userRepo.save(admin);

            // ─── Teachers (each owns specific subjects) ──────
            AppUser mathTeacher = new AppUser("Mrs. Sharma", "math.teacher@school.com", pwd, AppUser.Role.TEACHER);
            userRepo.save(mathTeacher);

            AppUser sciTeacher = new AppUser("Mr. Verma", "science.teacher@school.com", pwd, AppUser.Role.TEACHER);
            userRepo.save(sciTeacher);

            AppUser engTeacher = new AppUser("Ms. Gupta", "english.teacher@school.com", pwd, AppUser.Role.TEACHER);
            userRepo.save(engTeacher);

            // ─── Students ────────────────────────────────────
            AppUser s1 = new AppUser("Aarav Patel", "aarav@school.com", pwd, AppUser.Role.STUDENT);
            s1.setClassName("Class 8-A");
            userRepo.save(s1);

            AppUser s2 = new AppUser("Priya Singh", "priya@school.com", pwd, AppUser.Role.STUDENT);
            s2.setClassName("Class 8-A");
            userRepo.save(s2);

            AppUser s3 = new AppUser("Rohan Mehta", "rohan@school.com", pwd, AppUser.Role.STUDENT);
            s3.setClassName("Class 9-A");
            userRepo.save(s3);

            // ─── Parents ─────────────────────────────────────
            // Mr. Patel → Aarav (Class 8-A) — should only see Class 8-A homework
            AppUser p1 = new AppUser("Mr. Patel", "parent.patel@email.com", pwd, AppUser.Role.PARENT);
            p1.setStudentEmail("aarav@school.com");
            userRepo.save(p1);

            // Mrs. Singh → Priya (Class 8-A)
            AppUser p2 = new AppUser("Mrs. Singh", "parent.singh@email.com", pwd, AppUser.Role.PARENT);
            p2.setStudentEmail("priya@school.com");
            userRepo.save(p2);

            // Mr. Mehta → Rohan (Class 9-A) — should only see Class 9-A homework
            AppUser p3 = new AppUser("Mr. Mehta", "parent.mehta@email.com", pwd, AppUser.Role.PARENT);
            p3.setStudentEmail("rohan@school.com");
            userRepo.save(p3);

            // ─── Subjects (owned by specific teachers) ───────
            Subject math8a = new Subject();
            math8a.setName("Mathematics"); math8a.setClassName("Class 8-A");
            math8a.setTeacherEmail(mathTeacher.getEmail()); math8a.setTeacherName(mathTeacher.getName());
            subjectRepo.save(math8a);

            Subject math9a = new Subject();
            math9a.setName("Mathematics"); math9a.setClassName("Class 9-A");
            math9a.setTeacherEmail(mathTeacher.getEmail()); math9a.setTeacherName(mathTeacher.getName());
            subjectRepo.save(math9a);

            Subject sci8a = new Subject();
            sci8a.setName("Science"); sci8a.setClassName("Class 8-A");
            sci8a.setTeacherEmail(sciTeacher.getEmail()); sci8a.setTeacherName(sciTeacher.getName());
            subjectRepo.save(sci8a);

            Subject eng8a = new Subject();
            eng8a.setName("English"); eng8a.setClassName("Class 8-A");
            eng8a.setTeacherEmail(engTeacher.getEmail()); eng8a.setTeacherName(engTeacher.getName());
            subjectRepo.save(eng8a);

            Subject eng9a = new Subject();
            eng9a.setName("English"); eng9a.setClassName("Class 9-A");
            eng9a.setTeacherEmail(engTeacher.getEmail()); eng9a.setTeacherName(engTeacher.getName());
            subjectRepo.save(eng9a);

            // ─── Sample homework (each by their teacher) ─────
            Homework hw1 = new Homework();
            hw1.setSubjectName("Mathematics"); hw1.setClassName("Class 8-A");
            hw1.setTitle("Algebra Practice"); hw1.setDescription("Complete exercises on quadratic equations");
            hw1.setBookName("NCERT Mathematics"); hw1.setPageFrom(45); hw1.setPageTo(48);
            hw1.setDueDate(LocalDate.now().plusDays(1));
            hw1.setTeacherEmail(mathTeacher.getEmail()); hw1.setTeacherName(mathTeacher.getName());
            hwRepo.save(hw1);

            Homework hw2 = new Homework();
            hw2.setSubjectName("Science"); hw2.setClassName("Class 8-A");
            hw2.setTitle("Chemical Reactions"); hw2.setDescription("Read and answer end-of-chapter questions");
            hw2.setBookName("NCERT Science"); hw2.setPageFrom(22); hw2.setPageTo(25);
            hw2.setDueDate(LocalDate.now().plusDays(1));
            hw2.setTeacherEmail(sciTeacher.getEmail()); hw2.setTeacherName(sciTeacher.getName());
            hwRepo.save(hw2);

            Homework hw3 = new Homework();
            hw3.setSubjectName("English"); hw3.setClassName("Class 8-A");
            hw3.setTitle("Essay Writing"); hw3.setDescription("Write a 500-word essay on 'My Favourite Season'");
            hw3.setBookName("English Workbook"); hw3.setPageFrom(30); hw3.setPageTo(31);
            hw3.setDueDate(LocalDate.now().plusDays(2));
            hw3.setTeacherEmail(engTeacher.getEmail()); hw3.setTeacherName(engTeacher.getName());
            hwRepo.save(hw3);

            Homework hw4 = new Homework();
            hw4.setSubjectName("English"); hw4.setClassName("Class 9-A");
            hw4.setTitle("Grammar Exercises"); hw4.setDescription("Complete tenses worksheet pages");
            hw4.setBookName("English Grammar Book"); hw4.setPageFrom(55); hw4.setPageTo(58);
            hw4.setDueDate(LocalDate.now().plusDays(2));
            hw4.setTeacherEmail(engTeacher.getEmail()); hw4.setTeacherName(engTeacher.getName());
            hwRepo.save(hw4);

            Homework hw5 = new Homework();
            hw5.setSubjectName("Mathematics"); hw5.setClassName("Class 9-A");
            hw5.setTitle("Trigonometry Basics"); hw5.setDescription("Solve exercises 1-20");
            hw5.setBookName("NCERT Mathematics"); hw5.setPageFrom(102); hw5.setPageTo(105);
            hw5.setDueDate(LocalDate.now().plusDays(1));
            hw5.setTeacherEmail(mathTeacher.getEmail()); hw5.setTeacherName(mathTeacher.getName());
            hwRepo.save(hw5);

            log.info("✅ Demo data seeded (password: password123)");
            log.info("   Admin:           admin@school.com (full access)");
            log.info("   Math Teacher:    math.teacher@school.com (Math 8-A & 9-A)");
            log.info("   Science Teacher: science.teacher@school.com (Science 8-A)");
            log.info("   English Teacher: english.teacher@school.com (English 8-A & 9-A)");
            log.info("   Student 8-A:     aarav@school.com / priya@school.com");
            log.info("   Student 9-A:     rohan@school.com");
            log.info("   Parent (8-A):    parent.patel@email.com → sees only Class 8-A");
            log.info("   Parent (9-A):    parent.mehta@email.com → sees only Class 9-A");
        };
    }
}
