package com.school.homework.controller;

import com.school.homework.entity.*;
import com.school.homework.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserRepository userRepo;
    private final NotificationRepository notifRepo;
    private final SubjectRepository subjectRepo;
    private final HomeworkRepository hwRepo;
    private final HomeworkStatusRepository statusRepo;

    public ApiController(UserRepository userRepo, NotificationRepository notifRepo,
                         SubjectRepository subjectRepo, HomeworkRepository hwRepo,
                         HomeworkStatusRepository statusRepo) {
        this.userRepo = userRepo;
        this.notifRepo = notifRepo;
        this.subjectRepo = subjectRepo;
        this.hwRepo = hwRepo;
        this.statusRepo = statusRepo;
    }

    private AppUser getUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName()).orElseThrow();
    }

    private boolean isAdmin(AppUser user) {
        return user.getRole() == AppUser.Role.ADMIN;
    }

    /**
     * Resolve the class name for a student or parent.
     * - Student: use their own className
     * - Parent: look up linked child's className
     * Returns null if not resolvable.
     */
    private String resolveClassName(AppUser user) {
        if (user.getRole() == AppUser.Role.STUDENT) {
            return user.getClassName();
        } else if (user.getRole() == AppUser.Role.PARENT && user.getStudentEmail() != null) {
            AppUser child = userRepo.findByEmail(user.getStudentEmail()).orElse(null);
            return child != null ? child.getClassName() : null;
        }
        return null;
    }

    /**
     * Get homework filtered by class. If className is set, only return homework for that class.
     * Optionally also filter by subject name.
     */
    private List<Homework> getHomeworkForClass(String className, String subjectFilter) {
        if (className != null && !className.isEmpty()) {
            if (subjectFilter != null && !subjectFilter.isEmpty()) {
                return hwRepo.findByClassNameAndSubjectNameOrderByDueDateDesc(className, subjectFilter);
            }
            return hwRepo.findByClassNameOrderByDueDateDesc(className);
        }
        // Fallback: no class filter
        if (subjectFilter != null && !subjectFilter.isEmpty()) {
            return hwRepo.findBySubjectNameOrderByDueDateDesc(subjectFilter);
        }
        return hwRepo.findAllByOrderByDueDateDesc();
    }

    // ─── Dashboard ───────────────────────────────────────────

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication auth) {
        AppUser user = getUser(auth);
        LocalDate today = LocalDate.now();
        Map<String, Object> data = new HashMap<>();
        data.put("role", user.getRole().name());

        if (isAdmin(user)) {
            List<Subject> allSubjects = subjectRepo.findAll();
            List<Homework> allHomework = hwRepo.findAllByOrderByDueDateDesc();
            List<AppUser> allTeachers = userRepo.findByRole(AppUser.Role.TEACHER);
            data.put("totalSubjects", allSubjects.size());
            data.put("totalHomework", allHomework.size());
            data.put("totalTeachers", allTeachers.size());
            data.put("dueToday", allHomework.stream().filter(h -> today.equals(h.getDueDate())).count());
            data.put("recentHomework", allHomework.stream().limit(5).collect(Collectors.toList()));

        } else if (user.getRole() == AppUser.Role.TEACHER) {
            List<Subject> mySubjects = subjectRepo.findByTeacherEmail(user.getEmail());
            List<Homework> myHomework = hwRepo.findByTeacherEmailOrderByDueDateDesc(user.getEmail());
            data.put("totalSubjects", mySubjects.size());
            data.put("totalHomework", myHomework.size());
            data.put("dueToday", hwRepo.countByTeacherEmailAndDueDate(user.getEmail(), today));
            data.put("recentHomework", myHomework.stream().limit(5).collect(Collectors.toList()));

        } else if (user.getRole() == AppUser.Role.STUDENT) {
            String className = resolveClassName(user);
            List<Homework> myHw = getHomeworkForClass(className, null);
            Set<Long> completedIds = statusRepo.findByStudentEmail(user.getEmail()).stream()
                .filter(s -> "completed".equals(s.getStatus()))
                .map(HomeworkStatus::getHomeworkId).collect(Collectors.toSet());
            data.put("className", className != null ? className : "");
            data.put("totalPending", myHw.stream().filter(h -> !completedIds.contains(h.getId())).count());
            data.put("totalCompleted", completedIds.size());
            data.put("dueToday", myHw.stream().filter(h -> !completedIds.contains(h.getId()) && today.equals(h.getDueDate())).count());

        } else { // PARENT
            AppUser child = user.getStudentEmail() != null ? userRepo.findByEmail(user.getStudentEmail()).orElse(null) : null;
            if (child != null) {
                String className = child.getClassName();
                List<Homework> childHw = getHomeworkForClass(className, null);
                Set<Long> completedIds = statusRepo.findByStudentEmail(child.getEmail()).stream()
                    .filter(s -> "completed".equals(s.getStatus()))
                    .map(HomeworkStatus::getHomeworkId).collect(Collectors.toSet());
                data.put("childName", child.getName());
                data.put("childClass", className != null ? className : "");
                data.put("childPending", childHw.stream().filter(h -> !completedIds.contains(h.getId())).count());
                data.put("childCompleted", completedIds.size());
                data.put("childDueToday", childHw.stream().filter(h -> !completedIds.contains(h.getId()) && today.equals(h.getDueDate())).count());
            }
        }
        return data;
    }

    // ─── Subjects ────────────────────────────────────────────

    @GetMapping("/subjects")
    public List<Subject> getSubjects(Authentication auth) {
        AppUser user = getUser(auth);
        if (isAdmin(user)) {
            return subjectRepo.findAll();
        } else if (user.getRole() == AppUser.Role.TEACHER) {
            return subjectRepo.findByTeacherEmailOrderByNameAsc(user.getEmail());
        } else {
            // Student/Parent: only subjects for their class
            String className = resolveClassName(user);
            if (className != null && !className.isEmpty()) {
                return subjectRepo.findByClassNameOrderByNameAsc(className);
            }
            return subjectRepo.findAll();
        }
    }

    @PostMapping("/subjects")
    public ResponseEntity<?> createSubject(@RequestBody Map<String, String> body, Authentication auth) {
        AppUser user = getUser(auth);
        if (user.getRole() != AppUser.Role.TEACHER && !isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers and admin can create subjects"));
        }

        Subject s = new Subject();
        s.setName(body.get("name"));
        s.setClassName(body.get("className"));

        if (isAdmin(user) && body.containsKey("teacherEmail") && !body.get("teacherEmail").isEmpty()) {
            String teacherEmail = body.get("teacherEmail");
            AppUser teacher = userRepo.findByEmail(teacherEmail).orElse(null);
            if (teacher == null || teacher.getRole() != AppUser.Role.TEACHER) {
                return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found: " + teacherEmail));
            }
            s.setTeacherEmail(teacher.getEmail());
            s.setTeacherName(teacher.getName());
        } else {
            s.setTeacherEmail(user.getEmail());
            s.setTeacherName(user.getName());
        }

        return ResponseEntity.ok(subjectRepo.save(s));
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long id, Authentication auth) {
        AppUser user = getUser(auth);
        Subject subject = subjectRepo.findById(id).orElse(null);
        if (subject == null) return ResponseEntity.notFound().build();
        if (!isAdmin(user) && !subject.getTeacherEmail().equals(user.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only delete your own subjects"));
        }
        subjectRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ─── Teachers list (admin only) ──────────────────────────

    @GetMapping("/teachers")
    public ResponseEntity<?> getTeachers(Authentication auth) {
        AppUser user = getUser(auth);
        if (!isAdmin(user)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
        return ResponseEntity.ok(userRepo.findByRole(AppUser.Role.TEACHER).stream()
            .map(t -> Map.of("id", t.getId(), "name", t.getName(), "email", t.getEmail()))
            .collect(Collectors.toList()));
    }

    // ─── Homework ────────────────────────────────────────────

    @GetMapping("/homework")
    public List<Map<String, Object>> getHomework(@RequestParam(required = false) String subject, Authentication auth) {
        AppUser user = getUser(auth);
        List<Homework> homework;

        if (isAdmin(user)) {
            homework = (subject != null && !subject.isEmpty())
                ? hwRepo.findBySubjectNameOrderByDueDateDesc(subject)
                : hwRepo.findAllByOrderByDueDateDesc();
        } else if (user.getRole() == AppUser.Role.TEACHER) {
            homework = (subject != null && !subject.isEmpty())
                ? hwRepo.findByTeacherEmailAndSubjectNameOrderByDueDateDesc(user.getEmail(), subject)
                : hwRepo.findByTeacherEmailOrderByDueDateDesc(user.getEmail());
        } else {
            // Student/Parent: scoped to their class
            String className = resolveClassName(user);
            homework = getHomeworkForClass(className, subject);
        }

        // Get statuses for student/parent
        String statusEmail = null;
        if (user.getRole() == AppUser.Role.STUDENT) statusEmail = user.getEmail();
        else if (user.getRole() == AppUser.Role.PARENT) statusEmail = user.getStudentEmail();

        Set<Long> completedIds = Collections.emptySet();
        if (statusEmail != null) {
            completedIds = statusRepo.findByStudentEmail(statusEmail).stream()
                .filter(s -> "completed".equals(s.getStatus()))
                .map(HomeworkStatus::getHomeworkId).collect(Collectors.toSet());
        }

        Set<Long> finalCompletedIds = completedIds;
        return homework.stream().map(hw -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", hw.getId());
            m.put("subjectName", hw.getSubjectName());
            m.put("className", hw.getClassName());
            m.put("title", hw.getTitle());
            m.put("description", hw.getDescription());
            m.put("bookName", hw.getBookName());
            m.put("pageFrom", hw.getPageFrom());
            m.put("pageTo", hw.getPageTo());
            m.put("pageRange", hw.getPageRange());
            m.put("dueDate", hw.getDueDate() != null ? hw.getDueDate().toString() : "");
            m.put("assignedDate", hw.getAssignedDate() != null ? hw.getAssignedDate().toString() : "");
            m.put("teacherName", hw.getTeacherName());
            m.put("teacherEmail", hw.getTeacherEmail());

            if (isAdmin(user) || user.getRole() == AppUser.Role.TEACHER) {
                m.put("completedCount", statusRepo.countByHomeworkIdAndStatus(hw.getId(), "completed"));
                m.put("totalTracked", statusRepo.findByHomeworkId(hw.getId()).size());
            } else {
                m.put("status", finalCompletedIds.contains(hw.getId()) ? "completed" : "pending");
            }
            return m;
        }).collect(Collectors.toList());
    }

    @PostMapping("/homework")
    public ResponseEntity<?> createHomework(@RequestBody Map<String, Object> body, Authentication auth) {
        AppUser user = getUser(auth);
        if (user.getRole() != AppUser.Role.TEACHER && !isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers and admin can assign homework"));
        }

        String subjectName = (String) body.get("subjectName");
        String className = (String) body.get("className");

        if (user.getRole() == AppUser.Role.TEACHER) {
            boolean ownsSubject = subjectRepo.findByTeacherEmail(user.getEmail()).stream()
                .anyMatch(s -> s.getName().equals(subjectName) && s.getClassName().equals(className));
            if (!ownsSubject) {
                return ResponseEntity.status(403).body(Map.of("error", "You can only assign homework for your own subjects"));
            }
        }

        Homework hw = new Homework();
        hw.setSubjectName(subjectName);
        hw.setClassName(className);
        hw.setTitle((String) body.get("title"));
        hw.setDescription((String) body.getOrDefault("description", ""));
        hw.setBookName((String) body.get("bookName"));
        hw.setPageFrom(toInt(body.get("pageFrom")));
        hw.setPageTo(body.get("pageTo") != null ? toInt(body.get("pageTo")) : toInt(body.get("pageFrom")));
        hw.setDueDate(LocalDate.parse((String) body.get("dueDate")));

        if (isAdmin(user) && body.containsKey("teacherEmail") && body.get("teacherEmail") != null) {
            String teacherEmail = (String) body.get("teacherEmail");
            AppUser teacher = userRepo.findByEmail(teacherEmail).orElse(null);
            hw.setTeacherEmail(teacherEmail);
            hw.setTeacherName(teacher != null ? teacher.getName() : user.getName());
        } else {
            hw.setTeacherEmail(user.getEmail());
            hw.setTeacherName(user.getName());
        }

        hwRepo.save(hw);

        // Notify only students in the same class + their parents
        String hwClass = hw.getClassName();
        String pageRange = hw.getPageRange();
        String msg = hw.getTitle() + " — " + hw.getBookName() + ", Pages " + pageRange + ". Due: " + hw.getDueDate();
        for (AppUser student : userRepo.findByRole(AppUser.Role.STUDENT)) {
            if (hwClass != null && !hwClass.equals(student.getClassName())) continue;
            notifRepo.save(new Notification(student.getId(), "📚 New: " + hw.getSubjectName(), msg));
            for (AppUser parent : userRepo.findByStudentEmail(student.getEmail())) {
                notifRepo.save(new Notification(parent.getId(), "📚 New: " + hw.getSubjectName(), student.getName() + ": " + msg));
            }
        }

        return ResponseEntity.ok(Map.of("success", true, "id", hw.getId()));
    }

    @DeleteMapping("/homework/{id}")
    public ResponseEntity<?> deleteHomework(@PathVariable Long id, Authentication auth) {
        AppUser user = getUser(auth);
        Homework hw = hwRepo.findById(id).orElse(null);
        if (hw == null) return ResponseEntity.notFound().build();
        if (!isAdmin(user) && !hw.getTeacherEmail().equals(user.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only delete your own homework"));
        }
        hwRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/homework/{id}")
    public ResponseEntity<?> editHomework(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        AppUser user = getUser(auth);
        Homework hw = hwRepo.findById(id).orElse(null);
        if (hw == null) return ResponseEntity.notFound().build();
        if (!isAdmin(user) && !hw.getTeacherEmail().equals(user.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only edit your own homework"));
        }
        if (body.containsKey("title")) hw.setTitle((String) body.get("title"));
        if (body.containsKey("description")) hw.setDescription((String) body.get("description"));
        if (body.containsKey("bookName")) hw.setBookName((String) body.get("bookName"));
        if (body.containsKey("pageFrom")) hw.setPageFrom(toInt(body.get("pageFrom")));
        if (body.containsKey("pageTo")) hw.setPageTo(toInt(body.get("pageTo")));
        if (body.containsKey("dueDate")) hw.setDueDate(LocalDate.parse((String) body.get("dueDate")));
        hwRepo.save(hw);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ─── Student status ──────────────────────────────────────

    @PostMapping("/homework/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        AppUser user = getUser(auth);
        String status = body.get("status");
        HomeworkStatus hs = statusRepo.findByHomeworkIdAndStudentEmail(id, user.getEmail())
            .orElseGet(() -> {
                HomeworkStatus n = new HomeworkStatus();
                n.setHomeworkId(id);
                n.setStudentEmail(user.getEmail());
                n.setStudentName(user.getName());
                return n;
            });
        hs.setStatus(status);
        hs.setCompletedAt("completed".equals(status) ? LocalDateTime.now() : null);
        statusRepo.save(hs);
        if ("completed".equals(status)) {
            for (AppUser parent : userRepo.findByStudentEmail(user.getEmail())) {
                notifRepo.save(new Notification(parent.getId(), "✅ Homework Done", user.getName() + " completed an assignment"));
            }
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ─── Export CSV ──────────────────────────────────────────

    @GetMapping("/homework/export")
    public void exportCsv(Authentication auth, HttpServletResponse response) throws Exception {
        AppUser user = getUser(auth);
        List<Homework> homework;
        if (isAdmin(user)) {
            homework = hwRepo.findAllByOrderByDueDateDesc();
        } else {
            homework = hwRepo.findByTeacherEmailOrderByDueDateDesc(user.getEmail());
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=homework-export.csv");
        PrintWriter w = response.getWriter();
        w.println("Subject,Class,Title,Description,Book Name,Page From,Page To,Due Date,Assigned Date,Teacher");
        for (Homework hw : homework) {
            w.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%s,%s,\"%s\"%n",
                esc(hw.getSubjectName()), esc(hw.getClassName()), esc(hw.getTitle()),
                esc(hw.getDescription()), esc(hw.getBookName()),
                hw.getPageFrom(), hw.getPageTo(), hw.getDueDate(), hw.getAssignedDate(), esc(hw.getTeacherName()));
        }
        w.flush();
    }

    // ─── Notifications ───────────────────────────────────────

    @GetMapping("/notifications")
    public Map<String, Object> getNotifications(Authentication auth) {
        AppUser user = getUser(auth);
        return Map.of(
            "notifications", notifRepo.findByUserIdOrderByCreatedAtDesc(user.getId()),
            "unreadCount", notifRepo.countByUserIdAndIsReadFalse(user.getId())
        );
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        AppUser user = getUser(auth);
        notifRepo.findByUserIdAndIsReadFalse(user.getId()).forEach(n -> { n.setRead(true); notifRepo.save(n); });
        return ResponseEntity.ok(Map.of("success", true));
    }

    private String esc(String s) { return s != null ? s.replace("\"", "\"\"") : ""; }
    private int toInt(Object o) {
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(o.toString());
    }
}
