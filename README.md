# 📚 Homework Alert App

A homework management app backed by **Google Sheets**. Teachers add homework through the app (writes to Google Sheets), and parents get view access to see their children's assignments.

## How It Works

```
Teacher (Admin) ──→ App ──→ Google Sheets ──→ App ──→ Parent (View Only)
                                  ↕
                              Student (Mark Done)
```

## Tech Stack

- **Backend:** Java 17 + Spring Boot 3.3
- **Frontend:** Thymeleaf + Tailwind CSS (CDN)
- **Database:** H2 (users & notifications) + Google Sheets (homework data)
- **Auth:** Spring Security with form login
- **Build:** Maven (no Node.js required)

## Quick Start

```bash
cd homework-alert-app
mvn spring-boot:run
```

Open http://localhost:8080

## Demo Accounts (password: password123)

| Role    | Email                    | Access |
|---------|--------------------------|--------|
| Teacher | teacher@school.com       | Admin — add/delete homework, manage subjects |
| Student | aarav@school.com         | View homework, mark as completed |
| Parent  | parent.patel@email.com   | View-only access to child's homework |

## Google Sheets Setup (Optional)

The app works in demo mode without Google Sheets. To connect:

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a project → enable **Google Sheets API**
3. Create a **Service Account** → download JSON key
4. Save as `credentials.json` in project root
5. Create a Google Sheet → share with service account email (Editor)
6. Update `application.properties`:
   ```
   google.sheets.id=YOUR_SHEET_ID_HERE
   ```
7. Restart the app
