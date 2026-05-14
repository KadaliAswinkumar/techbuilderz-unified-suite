-- Clears seeded business data in tenant DB `t_demo` while keeping app_users (e.g. demoadmin).
-- Run: psql -h localhost -p 5433 -U vidyalaya -d t_demo -f scripts/reset-demo-tenant-data.sql

BEGIN;

DELETE FROM fee_payments;
DELETE FROM exam_invigilations;
DELETE FROM exam_results;
DELETE FROM exams;
DELETE FROM attendance_records;
DELETE FROM extracurriculars;
DELETE FROM parent_students;
DELETE FROM parent_communities;
DELETE FROM salary_payments;
DELETE FROM timetable_slots;
DELETE FROM teacher_assignments;
DELETE FROM sections;
DELETE FROM students;
DELETE FROM parents;
DELETE FROM teachers;
DELETE FROM school_classes;
DELETE FROM subjects;
DELETE FROM fee_structures;
DELETE FROM expenses;
DELETE FROM notices;
DELETE FROM events;
DELETE FROM refresh_tokens;
DELETE FROM chat_messages;
DELETE FROM faqs;
DELETE FROM public_pages;
DELETE FROM audit_logs;
DELETE FROM transport_routes;

COMMIT;
