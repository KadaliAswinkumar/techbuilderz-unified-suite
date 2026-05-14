CREATE TABLE teachers (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(64),
    gender VARCHAR(32),
    date_of_birth DATE,
    address TEXT,
    qualification VARCHAR(255),
    joining_date DATE,
    salary_amount NUMERIC(14, 2),
    photo_url VARCHAR(512),
    social_links TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE students (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(64),
    gender VARCHAR(255),
    father_name VARCHAR(255),
    mother_name VARCHAR(255),
    date_of_birth DATE,
    religion VARCHAR(128),
    caste VARCHAR(128),
    address TEXT,
    class_name VARCHAR(64),
    section VARCHAR(32),
    admission_date DATE,
    photo_url VARCHAR(512),
    social_links TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE parents (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(64),
    address TEXT,
    photo_url VARCHAR(512),
    social_links TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    username VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    student_id UUID,
    teacher_id UUID,
    parent_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_app_users_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_app_users_teacher FOREIGN KEY (teacher_id) REFERENCES teachers (id),
    CONSTRAINT fk_app_users_parent FOREIGN KEY (parent_id) REFERENCES parents (id)
);

CREATE TABLE parent_students (
    parent_id UUID NOT NULL REFERENCES parents (id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    PRIMARY KEY (parent_id, student_id)
);

CREATE TABLE school_classes (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sections (
    id UUID PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    school_class_id UUID NOT NULL REFERENCES school_classes (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE subjects (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE teacher_assignments (
    id UUID PRIMARY KEY,
    teacher_id UUID NOT NULL REFERENCES teachers (id) ON DELETE CASCADE,
    school_class_id UUID NOT NULL REFERENCES school_classes (id) ON DELETE CASCADE,
    subject_id UUID REFERENCES subjects (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE timetable_slots (
    id UUID PRIMARY KEY,
    teacher_id UUID NOT NULL REFERENCES teachers (id) ON DELETE CASCADE,
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    title VARCHAR(255) NOT NULL,
    school_class_id UUID REFERENCES school_classes (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE exams (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    exam_type VARCHAR(64) NOT NULL,
    exam_date DATE NOT NULL,
    school_class_id UUID REFERENCES school_classes (id) ON DELETE SET NULL,
    subject_id UUID REFERENCES subjects (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE exam_results (
    id UUID PRIMARY KEY,
    exam_id UUID NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    subject_id UUID REFERENCES subjects (id) ON DELETE SET NULL,
    grade VARCHAR(16),
    percentage NUMERIC(6, 2),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    submitted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE exam_invigilations (
    id UUID PRIMARY KEY,
    exam_id UUID NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    teacher_id UUID NOT NULL REFERENCES teachers (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (exam_id, teacher_id)
);

CREATE TABLE attendance_records (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    record_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, record_date)
);

CREATE TABLE extracurriculars (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    activity_name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE fee_structures (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    due_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE fee_payments (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    fee_structure_id UUID NOT NULL REFERENCES fee_structures (id) ON DELETE CASCADE,
    amount NUMERIC(14, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE salary_payments (
    id UUID PRIMARY KEY,
    teacher_id UUID NOT NULL REFERENCES teachers (id) ON DELETE CASCADE,
    amount NUMERIC(14, 2) NOT NULL,
    month_year VARCHAR(16) NOT NULL,
    paid_at TIMESTAMPTZ,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    category VARCHAR(128) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    description TEXT,
    expense_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE notices (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    image_url VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE events (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    event_type VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES app_users (id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    family_id UUID NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE faqs (
    id UUID PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES app_users (id) ON DELETE SET NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE parent_communities (
    id UUID PRIMARY KEY,
    parent_id UUID NOT NULL REFERENCES parents (id) ON DELETE CASCADE,
    name VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE transport_routes (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public_pages (
    id UUID PRIMARY KEY,
    page_key VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(255),
    content_html TEXT,
    meta_description VARCHAR(512),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    action VARCHAR(128) NOT NULL,
    detail TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_exam_results_student ON exam_results (student_id);
CREATE INDEX idx_fee_payments_student ON fee_payments (student_id);
CREATE INDEX idx_attendance_student_date ON attendance_records (student_id, record_date);
