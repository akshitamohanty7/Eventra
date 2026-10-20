-- =====================================================================
-- Eventra - Seed Data
-- Run AFTER schema.sql
-- Passwords are SHA-256 hashes:
--   admin123  → 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a
--   attend123 → <see below>
-- To generate: echo -n "yourpassword" | sha256sum
-- admin123  SHA-256 = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a
-- attend123 SHA-256 = 5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8
-- pass1234  SHA-256 = b9c950640e1b3740743070b19de03c6ec3b2a2dc24f9756869ccb06b70bcbefa
-- =====================================================================

USE eventra_db;

-- ─── Clear existing seed data (safe for dev restarts) ───────────────────────
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE connection_requests;
TRUNCATE TABLE user_skills;
TRUNCATE TABLE skill_profiles;
TRUNCATE TABLE recommendations;
TRUNCATE TABLE schedules;
TRUNCATE TABLE check_ins;
TRUNCATE TABLE registrations;
TRUNCATE TABLE event_interests;
TRUNCATE TABLE user_interests;
TRUNCATE TABLE interests;
TRUNCATE TABLE events;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ─── 1. Interests (Master data) ──────────────────────────────────────────────
INSERT INTO interests (id, label) VALUES
(1, 'Workshop'),
(2, 'Hackathon'),
(3, 'Seminar'),
(4, 'Cultural'),
(5, 'Tech Talk');

-- ─── 2. Users ────────────────────────────────────────────────────────────────
-- Admin: password = admin123
INSERT INTO users (id, name, email, password_hash, role) VALUES
(1, 'Akshita Admin', 'admin@eventra.com',
 '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a',
 'ADMIN');

-- Attendees: password = attend123
INSERT INTO users (id, name, email, password_hash, role) VALUES
(2, 'Rahul Sharma', 'rahul@example.com',
 '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
 'ATTENDEE'),
(3, 'Priya Mehta', 'priya@example.com',
 '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
 'ATTENDEE');

-- ─── 3. User Interests ───────────────────────────────────────────────────────
INSERT INTO user_interests (user_id, interest_id) VALUES
(2, 1), -- Rahul → Workshop
(2, 2), -- Rahul → Hackathon
(3, 3), -- Priya → Seminar
(3, 4); -- Priya → Cultural

-- ─── 4. Events (5 across 4 categories) ───────────────────────────────────────
INSERT INTO events (id, title, description, category, venue, start_time, end_time,
                    capacity, seats_left, status, organizer_id) VALUES
(1,
 'Full Stack Web Dev Workshop',
 'Hands-on workshop covering React, Node.js, and REST API design patterns.',
 'Workshop',
 'Lab Block 3, Room 204',
 '2026-11-10 10:00:00',
 '2026-11-10 17:00:00',
 50, 47, 'UPCOMING', 1),

(2,
 'National Hackathon 2026',
 '24-hour hackathon focused on solving real-world social challenges with tech.',
 'Hackathon',
 'Main Auditorium',
 '2026-11-15 09:00:00',
 '2026-11-16 09:00:00',
 100, 96, 'UPCOMING', 1),

(3,
 'AI Ethics & Society Seminar',
 'Expert panel discussion on responsible AI, bias, and governance frameworks.',
 'Seminar',
 'Conference Hall B',
 '2026-11-12 14:00:00',
 '2026-11-12 17:00:00',
 30, 29, 'UPCOMING', 1),

(4,
 'Annual Cultural Fest — Kaleidoscope',
 'A celebration of music, dance, art, and drama from across the country.',
 'Cultural',
 'Open Air Amphitheatre',
 '2026-11-20 16:00:00',
 '2026-11-20 22:00:00',
 200, 199, 'UPCOMING', 1),

(5,
 'Cloud Computing Tech Talk',
 'Industry talk by senior engineers from AWS on microservices and serverless.',
 'Tech Talk',
 'Seminar Hall A',
 '2026-11-08 11:00:00',
 '2026-11-08 13:00:00',
 5, 1, 'ALMOST_FULL', 1);

-- ─── 5. Event Interests ───────────────────────────────────────────────────────
INSERT INTO event_interests (event_id, interest_id) VALUES
(1, 1), -- Workshop event → Workshop interest
(2, 2), -- Hackathon event → Hackathon interest
(3, 3), -- Seminar event → Seminar interest
(4, 4), -- Cultural event → Cultural interest
(5, 5); -- Tech Talk event → Tech Talk interest

-- ─── 6. Registrations (3 sample bookings) ────────────────────────────────────
INSERT INTO registrations (id, user_id, event_id, booking_ref, seat_number, status) VALUES
(1, 2, 1, 'EVT-SEED0001', 1, 'CONFIRMED'), -- Rahul @ Workshop
(2, 2, 2, 'EVT-SEED0002', 1, 'CONFIRMED'), -- Rahul @ Hackathon
(3, 3, 3, 'EVT-SEED0003', 1, 'CONFIRMED'); -- Priya @ Seminar

-- ─── Done ────────────────────────────────────────────────────────────────────
SELECT 'Seed data loaded successfully.' AS status;
