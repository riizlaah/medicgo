-- ================================================
-- Medic-Go Telemedicine Platform
-- Phase 1: REST API – Complete Database Script
-- Includes schema creation and seed data
-- ================================================

-- ================================================
-- Create (or recreate) the MedicGo database
-- ================================================
IF DB_ID('MedicGo') IS NOT NULL
BEGIN
    DROP DATABASE MedicGo;
END
GO

CREATE DATABASE MedicGo;
GO

USE MedicGo;
GO

-- ================================================
-- 1. Users
-- ================================================
IF OBJECT_ID('dbo.Users','U') IS NOT NULL DROP TABLE dbo.Users;
GO

CREATE TABLE dbo.Users (
    id             INT IDENTITY(1,1)    PRIMARY KEY,
    name           NVARCHAR(255)        NOT NULL,
    username       NVARCHAR(100)        NOT NULL UNIQUE,
    email          NVARCHAR(255)        NOT NULL UNIQUE,
    password_hash  NVARCHAR(255)        NOT NULL,
    phone          NVARCHAR(20)         NOT NULL,
    role           NVARCHAR(10)         NOT NULL
        CONSTRAINT CK_Users_Role CHECK (role IN ('admin','patient')),
    created_at     DATETIME2            NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at     DATETIME2            NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

-- ================================================
-- 2. Doctors
-- ================================================
IF OBJECT_ID('dbo.Doctors','U') IS NOT NULL DROP TABLE dbo.Doctors;
GO

CREATE TABLE dbo.Doctors (
    id            INT IDENTITY(1,1)   PRIMARY KEY,
    name          NVARCHAR(255)       NOT NULL,
    specialty     NVARCHAR(255)       NOT NULL,
    description   NVARCHAR(MAX)       NOT NULL,
    experience    NVARCHAR(100)       NOT NULL,   -- e.g. 'Min. 8 years'
    location      NVARCHAR(255)       NOT NULL,
    price         DECIMAL(10,2)       NOT NULL
        CONSTRAINT CK_Doctors_Price CHECK (price > 0),
    duration      INT                 NOT NULL    -- consultation duration in minutes
        CONSTRAINT CK_Doctors_Duration CHECK (duration > 0),
    created_at    DATETIME2           NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at    DATETIME2           NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

-- ================================================
-- 3. Expertise
-- ================================================
IF OBJECT_ID('dbo.Expertise','U') IS NOT NULL DROP TABLE dbo.Expertise;
GO

CREATE TABLE dbo.Expertise (
    id         INT IDENTITY(1,1)  PRIMARY KEY,
    doctor_id  INT                NOT NULL
        CONSTRAINT FK_Expertise_Doctors FOREIGN KEY REFERENCES dbo.Doctors(id) ON DELETE CASCADE,
    title      NVARCHAR(255)      NOT NULL,
    content    NVARCHAR(MAX)      NOT NULL
);
GO

-- ================================================
-- 4. PromoCodes
-- ================================================
IF OBJECT_ID('dbo.PromoCodes','U') IS NOT NULL DROP TABLE dbo.PromoCodes;
GO

CREATE TABLE dbo.PromoCodes (
    id            INT IDENTITY(1,1)   PRIMARY KEY,
    code          NVARCHAR(50)        NOT NULL UNIQUE,
    discount_pct  DECIMAL(5,2)        NOT NULL     -- e.g. 20.00 = 20%
        CONSTRAINT CK_PromoCodes_DiscountPct CHECK (discount_pct > 0),
    quota         INT                 NOT NULL
        CONSTRAINT CK_PromoCodes_Quota CHECK (quota >= 0),
    expiry_date   DATETIME2           NOT NULL,
    created_at    DATETIME2           NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

-- ================================================
-- 5. Appointments
-- ================================================
IF OBJECT_ID('dbo.Appointments','U') IS NOT NULL DROP TABLE dbo.Appointments;
GO

CREATE TABLE dbo.Appointments (
    id              INT IDENTITY(1,1)    PRIMARY KEY,
    patient_id      INT                  NOT NULL
        CONSTRAINT FK_Appointments_Users   FOREIGN KEY REFERENCES dbo.Users(id),
    doctor_id       INT                  NOT NULL
        CONSTRAINT FK_Appointments_Doctors FOREIGN KEY REFERENCES dbo.Doctors(id),
    payment_method  NVARCHAR(50)         NOT NULL
        CONSTRAINT CK_Appointments_PaymentMethod CHECK (payment_method IN ('debit_card','credit_card','paypal')),
    coupon_code     NVARCHAR(50)         NULL,      -- stores the applied promo code string
    price_paid      DECIMAL(10,2)        NOT NULL,
    status          NVARCHAR(50)         NOT NULL DEFAULT 'Waiting for Confirmation',
    created_at      DATETIME2            NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at      DATETIME2            NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

-- ================================================
-- 6. SavedDoctors
-- ================================================
IF OBJECT_ID('dbo.SavedDoctors','U') IS NOT NULL DROP TABLE dbo.SavedDoctors;
GO

CREATE TABLE dbo.SavedDoctors (
    id          INT IDENTITY(1,1)    PRIMARY KEY,
    patient_id  INT                  NOT NULL
        CONSTRAINT FK_SavedDoctors_Users   FOREIGN KEY REFERENCES dbo.Users(id),
    doctor_id   INT                  NOT NULL
        CONSTRAINT FK_SavedDoctors_Doctors FOREIGN KEY REFERENCES dbo.Doctors(id),
    created_at  DATETIME2            NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_SavedDoctors UNIQUE (patient_id, doctor_id)
);
GO

-- ================================================
-- 7. TokenBlacklist  (JWT logout invalidation)
-- ================================================
IF OBJECT_ID('dbo.TokenBlacklist','U') IS NOT NULL DROP TABLE dbo.TokenBlacklist;
GO

CREATE TABLE dbo.TokenBlacklist (
    id             INT IDENTITY(1,1)   PRIMARY KEY,
    token          NVARCHAR(MAX)       NOT NULL,
    invalidated_at DATETIME2           NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

USE MedicGo;
-- ================================================
-- Sample Users
-- password_hash: SHA256('SecurePass123!') – demo value, all users share this hash
-- ================================================
INSERT INTO dbo.Users (name, username, email, password_hash, phone, role) VALUES ('John Doe', 'patient_01', 'john@example.com', '46708f23d682fef9aa996ecbb139bfb6c9ffdc039905ad6ad5c85a88b9411d97', '08123456789', 'patient');
INSERT INTO dbo.Users (name, username, email, password_hash, phone, role) VALUES ('Jane Smith', 'janesmith', 'jane@example.com', '46708f23d682fef9aa996ecbb139bfb6c9ffdc039905ad6ad5c85a88b9411d97', '08234567890', 'patient');
INSERT INTO dbo.Users (name, username, email, password_hash, phone, role) VALUES ('Michael Santos', 'michaels', 'michael@example.com', '46708f23d682fef9aa996ecbb139bfb6c9ffdc039905ad6ad5c85a88b9411d97', '08345678901', 'patient');
INSERT INTO dbo.Users (name, username, email, password_hash, phone, role) VALUES ('Emily Brown', 'emilyb', 'emily@example.com', '46708f23d682fef9aa996ecbb139bfb6c9ffdc039905ad6ad5c85a88b9411d97', '08456789012', 'patient');
INSERT INTO dbo.Users (name, username, email, password_hash, phone, role) VALUES ('David Johnson', 'davidj', 'david@example.com', '46708f23d682fef9aa996ecbb139bfb6c9ffdc039905ad6ad5c85a88b9411d97', '08567890123', 'patient');
INSERT INTO dbo.Users (name, username, email, password_hash, phone, role) VALUES ('Admin User', 'admin', 'admin@medicgo.com', '46708f23d682fef9aa996ecbb139bfb6c9ffdc039905ad6ad5c85a88b9411d97', '08000000000', 'admin');

USE MedicGo;
-- ================================================
-- Sample Doctors
-- ================================================
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. Sarah Chen', 'Cardiology', 'Dr. Sarah Chen is a board-certified cardiologist with over 8 years of clinical experience. She specializes in the diagnosis and management of cardiovascular diseases, including coronary artery disease, heart failure, and arrhythmias. Dr. Chen is committed to evidence-based medicine and patient education to promote long-term heart health.', 'Min. 8 years', 'Jakarta Selatan', 150.00, 30);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. James Wilson', 'General Practitioner', 'Dr. James Wilson is a highly capable general practitioner with 5 years of experience in primary care. He provides comprehensive healthcare services including routine check-ups, preventive care, and management of common acute and chronic illnesses. Dr. Wilson believes in holistic care and building lasting relationships with his patients.', 'Min. 5 years', 'Jakarta Pusat', 80.00, 20);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. Emily Rodriguez', 'Neurology', 'Dr. Emily Rodriguez is a renowned neurologist with over 10 years of expertise in treating disorders of the nervous system. Her areas of focus include epilepsy, migraine management, and neurodegenerative diseases. She is known for her meticulous diagnostic approach and compassionate patient care.', 'Min. 10 years', 'Bandung', 175.00, 45);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. Michael Tan', 'Orthopedics', 'Dr. Michael Tan is a skilled orthopedic surgeon with more than 12 years of experience in bone and joint surgery. He specializes in sports medicine, joint replacement, and spinal disorders. Dr. Tan employs the latest minimally invasive surgical techniques to ensure faster patient recovery.', 'Min. 12 years', 'Surabaya', 200.00, 60);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. Aisha Putri', 'Dermatology', 'Dr. Aisha Putri is a certified dermatologist with 6 years of experience in medical and cosmetic dermatology. She treats a wide range of skin conditions including acne, eczema, psoriasis, and skin cancer screening. Dr. Putri is passionate about helping patients achieve healthy, radiant skin.', 'Min. 6 years', 'Jakarta Selatan', 120.00, 30);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. Robert Kumar', 'Pulmonology', 'Dr. Robert Kumar is a pulmonologist with 9 years of experience in diagnosing and treating respiratory diseases. His expertise covers asthma, chronic obstructive pulmonary disease (COPD), lung cancer screening, and sleep apnea. Dr. Kumar is dedicated to improving the quality of life for patients with breathing difficulties.', 'Min. 9 years', 'Jakarta Barat', 160.00, 45);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. Linda Susanto', 'Pediatrics', 'Dr. Linda Susanto is a compassionate pediatrician with 7 years of experience in child healthcare. She provides preventive and therapeutic care for infants, children, and adolescents. Dr. Susanto emphasizes family-centered care and works closely with parents to support the healthy development of their children.', 'Min. 7 years', 'Yogyakarta', 100.00, 30);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. David Hartono', 'Ophthalmology', 'Dr. David Hartono is an ophthalmologist with 8 years of experience in eye care and vision correction. He specializes in cataract surgery, glaucoma management, and retinal disorders. Dr. Hartono is committed to preserving and restoring vision through advanced diagnostic and surgical techniques.', 'Min. 8 years', 'Jakarta Timur', 140.00, 30);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. Nadia Wijaya', 'Psychiatry', 'Dr. Nadia Wijaya is a psychiatrist with over 11 years of experience in mental health care. She specializes in the treatment of depression, anxiety disorders, bipolar disorder, and trauma-related conditions. Dr. Wijaya takes an integrative approach, combining medication management with evidence-based psychotherapy.', 'Min. 11 years', 'Medan', 180.00, 60);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. Steven Halim', 'Urology', 'Dr. Steven Halim is a urologist with 9 years of experience in diagnosing and treating conditions of the urinary tract and male reproductive system. He specializes in kidney stones, urinary incontinence, prostate health, and minimally invasive urological procedures. Dr. Halim is dedicated to providing personalized urologic care.', 'Min. 9 years', 'Semarang', 150.00, 45);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. Grace Santoso', 'Gynecology', 'Dr. Grace Santoso is a gynecologist with 8 years of experience in women''s health and reproductive medicine. She provides comprehensive care including prenatal care, fertility assessment, and management of gynecological conditions such as endometriosis and polycystic ovary syndrome. Dr. Santoso is a strong advocate for women''s health education.', 'Min. 8 years', 'Jakarta Selatan', 155.00, 30);
INSERT INTO dbo.Doctors (name, specialty, description, experience, location, price, duration) VALUES ('Dr. Anthony Lim', 'ENT (Ear, Nose & Throat)', 'Dr. Anthony Lim is an ENT specialist with 7 years of experience in diagnosing and treating disorders of the ear, nose, and throat. He manages conditions including hearing loss, sinusitis, tonsillitis, and head and neck surgeries. Dr. Lim is known for his thorough approach to ENT care and his commitment to improving patients'' quality of life.', 'Min. 7 years', 'Surabaya', 130.00, 30);

USE MedicGo;
-- ================================================
-- Sample Expertise  (minimum 3 per doctor)
-- ================================================

-- Dr. Sarah Chen (doctor_id = 1) – Cardiology
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (1, 'Echocardiography', 'Non-invasive cardiac imaging technique used to assess heart structure and function through ultrasound technology.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (1, 'Coronary Angiography', 'Imaging procedure to visualize the coronary arteries and diagnose blockages or narrowing that may cause heart disease.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (1, 'Arrhythmia Management', 'Diagnosis and treatment of irregular heart rhythms using medications, cardioversion, or catheter ablation procedures.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (1, 'Hypertension Control', 'Evidence-based management of high blood pressure to reduce the risk of cardiovascular events such as stroke and heart attack.');

-- Dr. James Wilson (doctor_id = 2) – General Practitioner
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (2, 'Preventive Health Check-ups', 'Routine health screenings and assessments to detect and prevent disease before symptoms arise.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (2, 'Chronic Disease Management', 'Ongoing care and monitoring for long-term conditions including diabetes, hypertension, and asthma.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (2, 'Acute Illness Treatment', 'Diagnosis and treatment of common infections, influenza, and other short-term health issues.');

-- Dr. Emily Rodriguez (doctor_id = 3) – Neurology
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (3, 'Epilepsy Management', 'Comprehensive evaluation and pharmacological management of seizure disorders to improve daily functioning.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (3, 'Migraine and Headache Disorders', 'Advanced diagnosis and prevention strategies for chronic and complex headache conditions.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (3, 'Neurodegenerative Disease Care', 'Specialized management of Parkinson disease, Alzheimer disease, and related progressive neurological conditions.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (3, 'Stroke Rehabilitation', 'Assessment and long-term follow-up care for stroke survivors to maximize neurological recovery.');

-- Dr. Michael Tan (doctor_id = 4) – Orthopedics
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (4, 'Joint Replacement Surgery', 'Total and partial hip and knee replacement procedures using minimally invasive surgical techniques.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (4, 'Sports Injury Treatment', 'Diagnosis and surgical or conservative management of ligament tears, fractures, and other sports-related injuries.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (4, 'Spinal Disorder Management', 'Evaluation and treatment of back pain, herniated discs, and spinal stenosis through targeted therapies.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (4, 'Arthritis Treatment', 'Medical and surgical management of osteoarthritis and rheumatoid arthritis to restore joint function and mobility.');

-- Dr. Aisha Putri (doctor_id = 5) – Dermatology
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (5, 'Acne and Rosacea Treatment', 'Personalized treatment plans using topical and oral medications for acne vulgaris and rosacea.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (5, 'Eczema and Psoriasis Management', 'Chronic skin disease management through lifestyle counseling and advanced topical or systemic therapies.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (5, 'Skin Cancer Screening', 'Full-body dermoscopic examination for early detection of melanoma and non-melanoma skin cancers.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (5, 'Cosmetic Dermatology', 'Aesthetic procedures including chemical peels, laser therapy, and botulinum toxin injections for skin rejuvenation.');

-- Dr. Robert Kumar (doctor_id = 6) – Pulmonology
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (6, 'Asthma Management', 'Comprehensive asthma care including spirometry-guided therapy and allergen avoidance strategies.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (6, 'COPD Treatment', 'Diagnosis and long-term management of chronic obstructive pulmonary disease to slow disease progression.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (6, 'Sleep Apnea Evaluation', 'Polysomnography-based diagnosis and CPAP therapy management for obstructive sleep apnea.');

-- Dr. Linda Susanto (doctor_id = 7) – Pediatrics
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (7, 'Child Immunization', 'Administration and monitoring of the complete childhood vaccination schedule per national health guidelines.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (7, 'Growth and Development Assessment', 'Regular evaluation of physical, cognitive, and behavioral milestones in infants and children.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (7, 'Pediatric Acute Care', 'Management of common childhood illnesses including fevers, respiratory infections, and gastrointestinal ailments.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (7, 'Neonatal Care', 'Specialized healthcare for newborns in the first 28 days of life, including breastfeeding support and developmental monitoring.');

-- Dr. David Hartono (doctor_id = 8) – Ophthalmology
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (8, 'Cataract Surgery', 'Microsurgical removal of clouded lens and implantation of intraocular lens to restore clear, functional vision.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (8, 'Glaucoma Management', 'Intraocular pressure monitoring and treatment with drops, laser therapy, or surgery to prevent optic nerve damage.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (8, 'Retinal Disorder Treatment', 'Diagnosis and management of retinal diseases including diabetic retinopathy and age-related macular degeneration.');

-- Dr. Nadia Wijaya (doctor_id = 9) – Psychiatry
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (9, 'Depression Treatment', 'Evidence-based pharmacological and psychotherapeutic management of major depressive disorder.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (9, 'Anxiety Disorder Management', 'Comprehensive treatment of generalized anxiety, panic disorder, and social phobia using CBT and medication.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (9, 'Bipolar Disorder Care', 'Mood stabilization and long-term management of bipolar spectrum disorders with integrated therapy.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (9, 'Trauma-Informed Therapy', 'Specialized assessment and treatment of post-traumatic stress disorder (PTSD) and acute stress reactions.');

-- Dr. Steven Halim (doctor_id = 10) – Urology
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (10, 'Kidney Stone Treatment', 'Minimally invasive procedures including ureteroscopy and lithotripsy for the removal of renal calculi.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (10, 'Prostate Health Management', 'PSA monitoring, biopsy, and treatment of benign prostatic hyperplasia and prostate cancer through targeted therapies.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (10, 'Urinary Incontinence Solutions', 'Evaluation and treatment of stress, urge, and mixed urinary incontinence using conservative and surgical approaches.');

-- Dr. Grace Santoso (doctor_id = 11) – Gynecology
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (11, 'Prenatal Care', 'Comprehensive maternal health monitoring throughout pregnancy including ultrasound and high-risk assessment.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (11, 'Endometriosis Management', 'Medical and surgical treatment of endometriosis to relieve chronic pain and preserve fertility.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (11, 'PCOS Treatment', 'Hormonal and lifestyle management of polycystic ovary syndrome to address symptoms and fertility concerns.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (11, 'Cervical Cancer Screening', 'Pap smear and HPV testing for early detection of cervical cancer and precancerous lesions.');

-- Dr. Anthony Lim (doctor_id = 12) – ENT
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (12, 'Sinusitis Treatment', 'Medical and endoscopic surgical management of acute and chronic sinusitis to restore nasal drainage.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (12, 'Hearing Loss Assessment', 'Pure tone audiometry and tympanometry for accurate diagnosis of conductive and sensorineural hearing loss.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (12, 'Tonsillectomy and Adenoidectomy', 'Surgical removal of enlarged tonsils and adenoids indicated by recurrent infections or obstructive conditions.');
INSERT INTO dbo.Expertise (doctor_id, title, content) VALUES (12, 'Head and Neck Surgery', 'Management of thyroid nodules, salivary gland disorders, and neck masses through surgical and non-surgical approaches.');

USE MedicGo;
-- ================================================
-- Sample PromoCodes
-- Note: FLASH25 is expired (expiry_date < 2026-03-23)
--       LIMITED30 has quota = 0 (cannot be applied)
-- ================================================
INSERT INTO dbo.PromoCodes (code, discount_pct, quota, expiry_date) VALUES ('HEALTH20',    20.00, 100,  '2026-06-30T00:00:00Z');
INSERT INTO dbo.PromoCodes (code, discount_pct, quota, expiry_date) VALUES ('MEDIC15',     15.00,  50,  '2026-04-30T00:00:00Z');
INSERT INTO dbo.PromoCodes (code, discount_pct, quota, expiry_date) VALUES ('WELCOME10',   10.00, 1000, '2027-12-31T00:00:00Z');
INSERT INTO dbo.PromoCodes (code, discount_pct, quota, expiry_date) VALUES ('FLASH25',     25.00,  10,  '2026-03-01T00:00:00Z');
INSERT INTO dbo.PromoCodes (code, discount_pct, quota, expiry_date) VALUES ('FREECONSULT', 100.00,  2,  '2026-05-31T00:00:00Z');
INSERT INTO dbo.PromoCodes (code, discount_pct, quota, expiry_date) VALUES ('NEWPATIENT5',  5.00, 500,  '2027-01-01T00:00:00Z');
INSERT INTO dbo.PromoCodes (code, discount_pct, quota, expiry_date) VALUES ('LIMITED30',   30.00,   0,  '2026-12-31T00:00:00Z');

USE MedicGo;
-- ================================================
-- Sample Appointments
-- price_paid = doctor.price * (1 - discount_pct/100) when coupon applied
-- ================================================
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (1,  1,  'credit_card',  NULL,          150.00, 'Waiting for Confirmation', '2026-03-01T08:30:00Z', '2026-03-01T08:30:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (2,  3,  'paypal',        'HEALTH20',    140.00, 'Confirmed',               '2026-02-15T10:00:00Z', '2026-02-16T09:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (3,  2,  'debit_card',    'WELCOME10',    72.00, 'Completed',               '2026-01-20T14:00:00Z', '2026-01-21T10:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (1,  5,  'credit_card',   NULL,          120.00, 'Completed',               '2026-02-01T09:00:00Z', '2026-02-02T11:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (4,  7,  'paypal',        'FREECONSULT',   0.00, 'Confirmed',               '2026-03-10T11:00:00Z', '2026-03-11T08:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (2,  4,  'debit_card',    'MEDIC15',     170.00, 'Waiting for Confirmation', '2026-03-20T13:00:00Z', '2026-03-20T13:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (5,  9,  'credit_card',   NULL,          180.00, 'Waiting for Confirmation', '2026-03-22T16:00:00Z', '2026-03-22T16:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (3,  8,  'paypal',        NULL,          140.00, 'Confirmed',               '2026-03-05T09:30:00Z', '2026-03-06T10:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (4,  11, 'debit_card',    NULL,          155.00, 'Completed',               '2026-02-10T10:00:00Z', '2026-02-11T12:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (1,  6,  'credit_card',   NULL,          160.00, 'Confirmed',               '2026-03-15T15:00:00Z', '2026-03-15T17:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (5,  12, 'paypal',        'NEWPATIENT5', 123.50, 'Waiting for Confirmation', '2026-03-21T11:00:00Z', '2026-03-21T11:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (2,  10, 'debit_card',    NULL,          150.00, 'Completed',               '2026-01-30T08:00:00Z', '2026-01-31T09:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (3,  1,  'credit_card',   'HEALTH20',    120.00, 'Confirmed',               '2026-03-12T14:00:00Z', '2026-03-13T10:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (4,  3,  'paypal',        NULL,          175.00, 'Waiting for Confirmation', '2026-03-23T07:00:00Z', '2026-03-23T07:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (1,  7,  'debit_card',    NULL,          100.00, 'Completed',               '2025-12-15T10:00:00Z', '2025-12-16T11:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (5,  4,  'credit_card',   'MEDIC15',     170.00, 'Confirmed',               '2026-03-18T09:00:00Z', '2026-03-19T08:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (2,  5,  'paypal',        NULL,          120.00, 'Waiting for Confirmation', '2026-03-22T12:00:00Z', '2026-03-22T12:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (3,  9,  'debit_card',    'FREECONSULT',   0.00, 'Confirmed',               '2026-03-08T13:00:00Z', '2026-03-09T09:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (1,  11, 'credit_card',   NULL,          155.00, 'Waiting for Confirmation', '2026-03-23T06:00:00Z', '2026-03-23T06:00:00Z');
INSERT INTO dbo.Appointments (patient_id, doctor_id, payment_method, coupon_code, price_paid, status, created_at, updated_at) VALUES (4,  6,  'paypal',        'WELCOME10',   144.00, 'Completed',               '2026-02-25T10:00:00Z', '2026-02-26T11:00:00Z');

USE MedicGo;
-- ================================================
-- Sample SavedDoctors
-- ================================================
INSERT INTO dbo.SavedDoctors (patient_id, doctor_id) VALUES (1,  2);
INSERT INTO dbo.SavedDoctors (patient_id, doctor_id) VALUES (1,  4);
INSERT INTO dbo.SavedDoctors (patient_id, doctor_id) VALUES (2,  1);
INSERT INTO dbo.SavedDoctors (patient_id, doctor_id) VALUES (2,  6);
INSERT INTO dbo.SavedDoctors (patient_id, doctor_id) VALUES (3,  3);
INSERT INTO dbo.SavedDoctors (patient_id, doctor_id) VALUES (3,  7);
INSERT INTO dbo.SavedDoctors (patient_id, doctor_id) VALUES (4,  5);
INSERT INTO dbo.SavedDoctors (patient_id, doctor_id) VALUES (4,  10);
INSERT INTO dbo.SavedDoctors (patient_id, doctor_id) VALUES (5,  2);
INSERT INTO dbo.SavedDoctors (patient_id, doctor_id) VALUES (5,  8);
