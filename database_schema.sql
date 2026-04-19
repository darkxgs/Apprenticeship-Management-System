-- =========================================================================
-- PVTD Students Management System - Oracle Database Export
-- This script contains the pure SQL commands to create all tables,
-- sequences, triggers, and insert basic mock data.
-- Run this directly in Oracle SQL Developer / SQL*Plus.
-- =========================================================================

-- -------------------------------------------------------------------------
-- 1. DROP EXISTING CONSTRAINTS AND TABLES (Optional / Safety)
-- -------------------------------------------------------------------------
/*
DROP TABLE student_grades CASCADE CONSTRAINTS;
DROP TABLE students CASCADE CONSTRAINTS;
DROP TABLE subjects CASCADE CONSTRAINTS;
DROP TABLE specializations CASCADE CONSTRAINTS;
DROP TABLE departments CASCADE CONSTRAINTS;
DROP TABLE users CASCADE CONSTRAINTS;
DROP TABLE logs CASCADE CONSTRAINTS;
DROP TABLE student_statuses CASCADE CONSTRAINTS;

DROP SEQUENCE users_seq;
DROP SEQUENCE departments_seq;
DROP SEQUENCE specializations_seq;
DROP SEQUENCE subjects_seq;
DROP SEQUENCE students_seq;
DROP SEQUENCE logs_seq;
DROP SEQUENCE student_statuses_seq;
*/

-- -------------------------------------------------------------------------
-- 2. SEQUENCES
-- -------------------------------------------------------------------------
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE departments_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE specializations_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE subjects_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE students_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE logs_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE student_statuses_seq START WITH 1 INCREMENT BY 1;


-- -------------------------------------------------------------------------
-- 3. TABLES
-- -------------------------------------------------------------------------

-- USERS TABLE
CREATE TABLE users (
    id NUMBER PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(100) NOT NULL,
    role VARCHAR2(50) DEFAULT 'user',
    full_name VARCHAR2(100)
);

CREATE OR REPLACE TRIGGER trg_users_seq 
BEFORE INSERT ON users FOR EACH ROW 
BEGIN :new.id := users_seq.nextval; END;
/

-- DEPARTMENTS TABLE
CREATE TABLE departments (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    description VARCHAR2(255)
);

CREATE OR REPLACE TRIGGER trg_departments_seq 
BEFORE INSERT ON departments FOR EACH ROW 
BEGIN :new.id := departments_seq.nextval; END;
/

-- SPECIALIZATIONS TABLE
CREATE TABLE specializations (
    id NUMBER PRIMARY KEY,
    department_id NUMBER NOT NULL,
    name VARCHAR2(150) NOT NULL,
    description VARCHAR2(255),
    CONSTRAINT fk_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

CREATE OR REPLACE TRIGGER trg_specializations_seq 
BEFORE INSERT ON specializations FOR EACH ROW 
BEGIN :new.id := specializations_seq.nextval; END;
/

-- SUBJECTS TABLE
CREATE TABLE subjects (
    id NUMBER PRIMARY KEY,
    specialization_id NUMBER,
    name VARCHAR2(200) NOT NULL,
    type VARCHAR2(50) DEFAULT 'نظري',
    pass_mark NUMBER NOT NULL,
    max_mark NUMBER NOT NULL,
    subject_type VARCHAR2(50), 
    CONSTRAINT fk_spec FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE CASCADE
);

CREATE OR REPLACE TRIGGER trg_subjects_seq 
BEFORE INSERT ON subjects FOR EACH ROW 
BEGIN :new.id := subjects_seq.nextval; END;
/

-- STUDENTS TABLE
CREATE TABLE students (
    id NUMBER PRIMARY KEY,
    specialization_id NUMBER,
    serial VARCHAR2(50),
    name VARCHAR2(200),
    registration_no VARCHAR2(100),
    national_id VARCHAR2(100),
    region VARCHAR2(100),
    profession VARCHAR2(100),
    exam_system VARCHAR2(100),
    seat_no VARCHAR2(100) UNIQUE,
    secret_no VARCHAR2(100),
    professional_group VARCHAR2(100),
    coordination_no VARCHAR2(100),
    dob_day VARCHAR2(10),
    dob_month VARCHAR2(10),
    dob_year VARCHAR2(10),
    gender VARCHAR2(20),
    neighborhood VARCHAR2(100),
    governorate VARCHAR2(100),
    religion VARCHAR2(50),
    nationality VARCHAR2(50),
    address VARCHAR2(255),
    school VARCHAR2(255),
    academic_year VARCHAR2(50),
    other_notes VARCHAR2(255),
    image_path VARCHAR2(255),
    status VARCHAR2(50),
    center_name VARCHAR2(150),
    id_front_path VARCHAR2(255),
    id_back_path VARCHAR2(255),
    CONSTRAINT fk_student_spec FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE SET NULL
);

CREATE OR REPLACE TRIGGER trg_students_seq 
BEFORE INSERT ON students FOR EACH ROW 
BEGIN :new.id := students_seq.nextval; END;
/

-- STUDENT GRADES TABLE
CREATE TABLE student_grades (
    student_id NUMBER NOT NULL,
    subject_id NUMBER NOT NULL,
    obtained_mark NUMBER NOT NULL,
    PRIMARY KEY (student_id, subject_id),
    CONSTRAINT fk_sg_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_sg_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);


-- LOGS TABLE
CREATE TABLE logs (
    id NUMBER PRIMARY KEY,
    username VARCHAR2(50) NOT NULL,
    action VARCHAR2(100) NOT NULL,
    details VARCHAR2(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE TRIGGER trg_logs_seq 
BEFORE INSERT ON logs FOR EACH ROW 
BEGIN :new.id := logs_seq.nextval; END;
/

-- STUDENT STATUSES TABLE
CREATE TABLE student_statuses (
    id NUMBER PRIMARY KEY,
    status_name VARCHAR2(100) UNIQUE NOT NULL
);

CREATE OR REPLACE TRIGGER trg_student_statuses_seq 
BEFORE INSERT ON student_statuses FOR EACH ROW 
BEGIN :new.id := student_statuses_seq.nextval; END;
/


-- -------------------------------------------------------------------------
-- 4. BASIC SEED DATA & MOCK DATA
-- -------------------------------------------------------------------------

-- 4.1. Admin User
INSERT INTO users (username, password, role, full_name) 
VALUES ('admin', 'admin123', 'admin', 'مدير النظام');

-- 4.2. Default Departments
INSERT INTO departments (name, description) VALUES ('القسم الرئيسي', 'القسم الافتراضي للنظام');
INSERT INTO departments (name, description) VALUES ('قسم الميكانيكا', 'تخصصات الميكانيكا العامة والمركبات');

-- 4.3. Default Statuses
INSERT INTO student_statuses (status_name) VALUES ('غائب');
INSERT INTO student_statuses (status_name) VALUES ('محروم');
INSERT INTO student_statuses (status_name) VALUES ('مفصول');
INSERT INTO student_statuses (status_name) VALUES ('معتذر');
INSERT INTO student_statuses (status_name) VALUES ('مؤجل');

-- 4.4. Mock Specializations (Mapped to Dept ID 1 and 2)
INSERT INTO specializations (department_id, name, description) VALUES (1, 'ميكانيكا السيارات', 'تخصص إصلاح وصيانة محركات');
INSERT INTO specializations (department_id, name, description) VALUES (2, 'خراطة وتشكيل معادن', 'تخصص الخراطة الميكانيكية');
INSERT INTO specializations (department_id, name, description) VALUES (1, 'كهرباء سيارات', 'صيانة الأنظمة الكهربائية للمركبات');

-- 4.5. Mock Subjects 
-- Mapping to Specialization 1 (ميكانيكا السيارات)
INSERT INTO subjects (specialization_id, name, type, pass_mark, max_mark, subject_type) 
VALUES (1, 'تكنولوجيا المحركات', 'نظري', 20, 40, 'نظري');
INSERT INTO subjects (specialization_id, name, type, pass_mark, max_mark, subject_type) 
VALUES (1, 'صيانة المحركات - ورشة', 'عملي', 30, 60, 'عملي');
INSERT INTO subjects (specialization_id, name, type, pass_mark, max_mark, subject_type) 
VALUES (1, 'رسم هندسي', 'نظري', 15, 30, 'نظري');

-- Mapping to Specialization 2 (خراطة)
INSERT INTO subjects (specialization_id, name, type, pass_mark, max_mark, subject_type) 
VALUES (2, 'خراطة عامة', 'نظري', 25, 50, 'نظري');
INSERT INTO subjects (specialization_id, name, type, pass_mark, max_mark, subject_type) 
VALUES (2, 'تطبيقات الخراطة', 'عملي', 50, 100, 'عملي');

-- 4.6. Mock Students
INSERT INTO students (specialization_id, name, national_id, seat_no, gender, governorate, center_name, school, academic_year) 
VALUES (1, 'أحمد محمود إسماعيل', '29901010111222', 'S1001', 'ذكر', 'القاهرة', 'مركز المعادن', 'مدرسة التكنولوجيا التطبيقية', '2025/2026');

INSERT INTO students (specialization_id, name, national_id, seat_no, gender, governorate, center_name, school, academic_year) 
VALUES (2, 'محمد علي إبراهيم', '29812120111333', 'S1002', 'ذكر', 'الجيزة', 'مركز الجيزة الميكانيكي', 'مدرسة الصنايع الجيزة', '2025/2026');

INSERT INTO students (specialization_id, name, national_id, seat_no, gender, governorate, center_name, school, academic_year) 
VALUES (1, 'يوسف سعيد مصطفى', '30110100111555', 'S1003', 'ذكر', 'الإسكندرية', 'مركز المعادن', 'مدرسة التكنولوجيا التطبيقية', '2025/2026');

-- 4.7. Mock Grades for Student 1
INSERT INTO student_grades (student_id, subject_id, obtained_mark) VALUES (1, 1, 35);
INSERT INTO student_grades (student_id, subject_id, obtained_mark) VALUES (1, 2, 58);
INSERT INTO student_grades (student_id, subject_id, obtained_mark) VALUES (1, 3, 22);

-- Mock Grades for Student 2
INSERT INTO student_grades (student_id, subject_id, obtained_mark) VALUES (2, 4, 45);
INSERT INTO student_grades (student_id, subject_id, obtained_mark) VALUES (2, 5, 90);

-- Mock Grades for Student 3 (Failed one subject)
INSERT INTO student_grades (student_id, subject_id, obtained_mark) VALUES (3, 1, 15); -- Failed
INSERT INTO student_grades (student_id, subject_id, obtained_mark) VALUES (3, 2, 40);
INSERT INTO student_grades (student_id, subject_id, obtained_mark) VALUES (3, 3, 20);

COMMIT;
