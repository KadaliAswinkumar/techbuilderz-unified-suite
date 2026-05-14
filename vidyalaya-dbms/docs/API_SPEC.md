# Vidyalaya HTTP API Specification

Base URL: `http://localhost:8080`  
Tenant header (when not implied by JWT): `X-Tenant-Slug: <school-slug>`  
Auth header: `Authorization: Bearer <access_token>`

Errors return JSON: `{ "error": { "code": "STRING", "message": "..." } }`  
Authoritative OpenAPI: `/swagger-ui.html` (springdoc).

---

## Auth

### POST /api/auth/login

**Description:** Authenticate super admin (omit `tenantSlug`) or tenant user.

**Request:**
```json
{
  "tenantSlug": "string | null",
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "string",
  "refreshToken": "string",
  "role": "ADMIN | TEACHER | STUDENT | PARENT | SUPER_ADMIN",
  "expiresIn": 900,
  "tenantSlug": "string | null"
}
```

### POST /api/auth/refresh

**Description:** Rotate refresh token; tenant users must send `X-Tenant-Slug`.

**Request:**
```json
{ "refreshToken": "string" }
```

**Response:** same shape as login.

### POST /api/auth/logout

**Description:** Revoke a refresh token (authenticated).

**Request:** `{ "refreshToken": "string" }`  
**Response:** `204 No Content`

### GET /api/auth/me

**Description:** Current principal.

**Response:**
```json
{
  "userId": "uuid | null",
  "username": "string",
  "role": "string",
  "tenantSlug": "string | null"
}
```

### POST /api/auth/change-password

**Request:** `{ "currentPassword": "string", "newPassword": "string" }`  
**Response:** `204`

### POST /api/auth/forgot-password

**Response:** `202` `{ "message": "..." }`

### POST /api/auth/reset-password

**Response:** `501` (placeholder)

### POST /api/auth/register-tenant

**Description:** Super admin only — creates DB + Flyway + tenant row + school admin user.

**Request:**
```json
{
  "slug": "string",
  "schoolName": "string",
  "adminUsername": "string",
  "adminPassword": "string"
}
```

**Response:** Tenant registry row (id, slug, name, dbName, dbHost, dbPort, …).

---

## Tenant branding

### GET /api/tenant/branding

**Response:** `{ "slug", "name", "primaryColor", "logoUrl" }`

### PUT /api/tenant/branding

**Request:** `{ "primaryColor": "string", "logoUrl": "string" }`  
**Response:** same as GET.

### PUT /api/tenant/openai-key

**Request:** `{ "apiKey": "string" }`  
**Response:** `204`

---

## Students (`/api/students`)

| Method | Path | Body / Notes |
|--------|------|----------------|
| GET | `/api/students` | List (role-filtered) |
| GET | `/api/students/{id}` | StudentResponse |
| POST | `/api/students` | StudentUpsertRequest |
| PUT | `/api/students/{id}` | StudentUpsertRequest |
| DELETE | `/api/students/{id}` | |
| GET | `/api/students/{id}/academic-record` | `{ examResults, extracurriculars }` |
| GET | `/api/students/{id}/attendance` | array |
| POST | `/api/students/import` | `text/plain` CSV body; response `{ "imported": number }` |

**StudentUpsertRequest:** `fullName` (required), `email`, `phone`, `gender`, `fatherName`, `motherName`, `dateOfBirth`, `religion`, `caste`, `address`, `className`, `section`, `admissionDate`, `photoUrl`, `socialLinks`.

---

## Teachers (`/api/teachers`)

| GET | `/api/teachers` | list |
| GET | `/api/teachers/{id}` | detail |
| POST | `/api/teachers` | TeacherUpsertRequest |
| PUT | `/api/teachers/{id}` | TeacherUpsertRequest |
| DELETE | `/api/teachers/{id}` | |
| GET | `/api/teachers/{id}/assignments` | |
| GET | `/api/teachers/{id}/salary` | |
| GET | `/api/teachers/{id}/invigilations` | |
| GET | `/api/teachers/{id}/timetable` | |

---

## Parents (`/api/parents`)

| GET | `/api/parents` | admin list |
| GET | `/api/parents/{id}` | |
| POST | `/api/parents` | ParentUpsertRequest |
| PUT | `/api/parents/{id}` | ParentUpsertRequest |
| DELETE | `/api/parents/{id}` | |
| GET | `/api/parents/{id}/children` | |
| GET | `/api/parents/{id}/exam-results` | |
| GET | `/api/parents/{id}/fee-dues` | |
| GET | `/api/parents/{id}/communities` | |
| POST | `/api/parents/{id}/communities` | `{ "name": "string" }` |
| DELETE | `/api/parents/communities/{communityId}` | |
| POST | `/api/parents/{parentId}/students/{studentId}` | link (admin) |

---

## Academics (`/api/...`)

- `GET/POST /api/school-classes` — `{ "name" }` on POST  
- `GET /api/sections?classId=` — `POST { name, schoolClassId }`  
- `GET/POST /api/subjects`  
- `GET /api/teacher-assignments?teacherId=` — `POST { teacherId, schoolClassId, subjectId? }`  
- `GET /api/timetable?teacherId=` — `POST { teacherId, dayOfWeek, startTime, endTime, title, schoolClassId? }` — `DELETE /api/timetable/{id}`  
- `GET/POST /api/exams` — `POST { name, examType, examDate, schoolClassId?, subjectId? }`  
- `POST /api/exams/{examId}/invigilators` — `{ teacherId }`  
- `GET /api/exams/{examId}/results`  
- `POST /api/exam-results` — `{ examId, studentId, subjectId?, grade, percentage, status }`  
- `POST /api/attendance` — `{ studentId, recordDate, status }`

---

## Finance (`/api/...`)

- `GET/POST/PUT/DELETE /api/fee-structures` (+ body on write)  
- `GET /api/fees/student/{studentId}/dues`  
- `GET /api/fee-payments` — `POST { studentId, feeStructureId, amount, status }`  
- `GET/POST /api/salary-payments` — `{ teacherId, amount, monthYear, status }`  
- `GET/POST /api/expenses` — `{ category, amount, description?, expenseDate }`

---

## Dashboard

- `GET /api/dashboard/admin` — KPI + `genderBreakdown`, `monthlySeries`, `notices`, `events`  
- `GET /api/dashboard/teacher` | `/student` | `/parent` — lightweight placeholders

---

## Notices & events

- CRUD `/api/notices` — POST/PUT `{ title, body?, imageUrl? }`  
- `GET /api/events?from=&to=` (ISO-8601)  
- `POST/PUT /api/events` — `{ title, startTime, endTime, eventType? }`  
- `DELETE /api/events/{id}`

---

## Transport

- `GET/POST/PUT/DELETE /api/transport/routes` — `{ name, description? }`

---

## FAQs & chatbot

- `GET /api/faqs` (auth) — full list  
- `GET /api/faqs/active` — **public**; requires `X-Tenant-Slug` for tenant DB  
- CRUD `/api/faqs` admin — `{ question, answer, active }`  
- `POST /api/chatbot/ask` — `{ "question": "string" }` → `{ "answer": "string" }`  
- `GET /api/chatbot/history`

---

## Public CMS (tenant DB)

- `GET /api/public-pages` (admin)  
- `PUT /api/public-pages/{pageKey}` — `{ title?, contentHtml?, metaDescription? }`  
- `DELETE /api/public-pages/{id}`

## Public website (Thymeleaf, no JWT)

- `GET /public/{slug}`  
- `GET /public/{slug}/about`  
- `GET /public/{slug}/announcements`  
- `GET /public/{slug}/contact`  
- `GET /public/{slug}/sitemap.xml`

---

## Standard HTTP

- `401` bad credentials / expired JWT  
- `403` forbidden (RBAC)  
- `400` validation (`VALIDATION_ERROR`)  
- `429` login / chatbot rate limit
