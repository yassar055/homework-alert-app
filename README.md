# 📚 Homework Alert App

Classroom homework management app. Teachers assign homework (book, pages, due date), students mark completion, parents get view-only access scoped to their child's class. Optionally syncs to Google Sheets.

---

## Quick Start (3 commands)

```bash
cd homework-alert-app

# 1. Build (offline, uses cached Maven dependencies)
mvn package -o -DskipTests

# 2. Run
java -jar target/homework-alert-1.0.0.jar

# 3. Open browser
# http://localhost:8080
```

That's it. The app starts with demo data pre-loaded.

---

## Demo Accounts

All passwords: **password123**

| Role | Email | What they see |
|------|-------|---------------|
| 👑 **Admin** | admin@school.com | Everything — all subjects, all homework, all classes |
| 👩‍🏫 Math Teacher | math.teacher@school.com | Only Mathematics (Class 8-A & 9-A) |
| 👩‍🏫 Science Teacher | science.teacher@school.com | Only Science (Class 8-A) |
| 👩‍🏫 English Teacher | english.teacher@school.com | Only English (Class 8-A & 9-A) |
| 🧑‍🎓 Student (8-A) | aarav@school.com | Only Class 8-A homework |
| 🧑‍🎓 Student (9-A) | rohan@school.com | Only Class 9-A homework |
| 👨‍👩‍👦 Parent (8-A) | parent.patel@email.com | Only child's Class 8-A homework (view-only) |
| 👨‍👩‍👦 Parent (9-A) | parent.mehta@email.com | Only child's Class 9-A homework (view-only) |

---

## How to Test Each Role

### 1. Admin (admin@school.com)
- Dashboard shows stats across ALL teachers and classes
- Subjects page: see all subjects, create new ones, assign to any teacher
- Homework page: see all homework from all teachers, create/edit/delete any
- Export CSV: downloads all homework for Google Sheets import

### 2. Teacher (e.g. english.teacher@school.com)
- Dashboard shows only YOUR subjects and homework
- Subjects page: only your subjects (English 8-A, English 9-A)
- Homework page: only your homework, can create/edit/delete
- Try creating homework: pick a subject → fill book name, pages, due date → Assign
- Export CSV: downloads only your homework

### 3. Student (e.g. aarav@school.com — Class 8-A)
- Sees only Class 8-A homework (not 9-A)
- Click "Mark Done" to complete an assignment
- Parent gets notified when you mark done

### 4. Parent (e.g. parent.patel@email.com)
- Sees only their child's class homework (Class 8-A)
- View-only — cannot edit or mark done
- Dashboard shows child's pending/completed/due-today counts
- Gets notifications when new homework is assigned or child completes one

### Quick scoping test:
1. Login as `parent.patel@email.com` → should see 3 homework items (all Class 8-A)
2. Login as `parent.mehta@email.com` → should see 2 homework items (all Class 9-A)
3. No cross-class leakage

---

## Google Sheets Integration

### Option A: Manual CSV Export (works now)
1. Login as Admin or Teacher
2. Click **📊 CSV** or **📊 Export All CSV**
3. Open Google Sheets → File → Import → Upload the CSV
4. Share the Sheet with parents

### Option B: Auto-sync via Google Apps Script (recommended, no extra dependencies)

**Step 1: Create a Google Sheet**
- Go to https://sheets.google.com → create a new sheet
- Name the first tab "Homework"

**Step 2: Add the Apps Script**
- In the Sheet: Extensions → Apps Script
- Replace the code with the script below
- Click Deploy → New Deployment → Web App
- Set "Who has access" to "Anyone"
- Copy the deployment URL

**Step 3: Configure the app**
- Add to `application.properties`:
  ```
  google.sheets.webhook-url=https://script.google.com/macros/s/YOUR_DEPLOYMENT_ID/exec
  ```
- Restart the app

The Apps Script to paste:
```javascript
function doPost(e) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Homework");
  if (!sheet) {
    sheet = SpreadsheetApp.getActiveSpreadsheet().insertSheet("Homework");
    sheet.appendRow(["ID","Subject","Class","Title","Description","Book","Page From","Page To","Due Date","Assigned","Teacher","Action"]);
  }
  
  var data = JSON.parse(e.postData.contents);
  
  if (data.action === "create") {
    sheet.appendRow([data.id, data.subject, data.className, data.title, data.description, 
                     data.bookName, data.pageFrom, data.pageTo, data.dueDate, data.assignedDate, data.teacher, "Active"]);
  } else if (data.action === "edit") {
    var rows = sheet.getDataRange().getValues();
    for (var i = 1; i < rows.length; i++) {
      if (String(rows[i][0]) === String(data.id)) {
        sheet.getRange(i+1, 4).setValue(data.title);
        sheet.getRange(i+1, 5).setValue(data.description);
        sheet.getRange(i+1, 6).setValue(data.bookName);
        sheet.getRange(i+1, 7).setValue(data.pageFrom);
        sheet.getRange(i+1, 8).setValue(data.pageTo);
        sheet.getRange(i+1, 9).setValue(data.dueDate);
        break;
      }
    }
  } else if (data.action === "delete") {
    var rows = sheet.getDataRange().getValues();
    for (var i = 1; i < rows.length; i++) {
      if (String(rows[i][0]) === String(data.id)) {
        sheet.deleteRow(i+1);
        break;
      }
    }
  }
  
  return ContentService.createTextOutput(JSON.stringify({status: "ok"}))
    .setMimeType(ContentService.MimeType.JSON);
}
```

---

## Project Structure

```
homework-alert-app/
├── pom.xml                          # Maven config (Spring Boot 2.7.8)
├── src/main/java/com/school/homework/
│   ├── HomeworkAlertApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java      # Auth rules per role
│   │   └── DataSeeder.java          # Demo data on first run
│   ├── controller/
│   │   ├── AuthController.java      # Login, register, /api/auth/me
│   │   └── ApiController.java       # All REST endpoints
│   ├── entity/
│   │   ├── AppUser.java             # ADMIN, TEACHER, STUDENT, PARENT
│   │   ├── Subject.java
│   │   ├── Homework.java
│   │   ├── HomeworkStatus.java
│   │   └── Notification.java
│   └── repository/                  # JPA repositories
├── src/main/resources/
│   ├── application.properties
│   └── static/
│       ├── index.html               # Single page shell
│       └── app.js                   # Full SPA frontend
└── data/                            # H2 database files (auto-created)
```

## Tech Stack

- **Java 17** + **Spring Boot 2.7.8** + **Spring Security**
- **H2 Database** (file-based, zero config)
- **Vanilla JS + Tailwind CSS CDN** (no Node.js needed)
- **Maven** (offline build using cached dependencies)

## Requirements

- JDK 17+ (you have Temurin 17.0.12)
- Maven 3.9+ (you have 3.9.9)
- No Node.js needed
- No internet needed for build (offline mode)

## Rebuild After Code Changes

```bash
mvn package -o -DskipTests && java -jar target/homework-alert-1.0.0.jar
```

To reset demo data (clear database):
```bash
rm -rf data && java -jar target/homework-alert-1.0.0.jar
```
