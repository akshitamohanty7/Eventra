# 🗓️ Eventra — Plan of Action
### Java Web Project · 2 Developers · 12-Hour Sprint
> **Project:** Eventra — Intelligent Event Management, Personalization & Networking Platform
> **Stack:** Java Servlets · JDBC · MySQL · HTML5/CSS3/Bootstrap 5 · Apache Tomcat · Maven
> **Review Focus:** Phase 1 (Foundation) must be 100% complete; Phase 2 partially complete for max marks.

---

## 📊 Marking Rubric Alignment

| Area | Marks | Key Proof Points |
|---|---|---|
| Problem Understanding & Solution Design | 8 | Clear domain model, layered architecture (Servlet → Service → DAO → JDBC → MySQL), OOP used meaningfully |
| Core Java Concepts | 10 | OOP (encapsulation, polymorphism, inheritance), Collections, Exception handling, Interface-driven DAOs, custom exceptions |
| Database Integration (JDBC) | 8 | 13 tables, parameterized queries only, transactions, DAO pattern, zero SQL injection |
| Servlets & Web Integration | 7 | Servlet-per-feature, doGet/doPost separation, session management, redirect/forward, filter for auth |
| **Total** | **33** | |

---

## 👥 Role Split — Zero Overlap Guarantee

| | **Dev A — Akshita (Backend Architect)** | **Dev B — Akriti (Frontend + Wiring)** |
|---|---|---|
| **Domain** | Database schema · JDBC DAOs · Service layer · Business logic · Java model classes · JUnit tests | HTML/JSP pages · Servlet controllers · Session management · Auth filter · CSS/Bootstrap · web.xml |
| **Handoff Contract** | Exposes typed Java interfaces (`IEventService`, `IUserService`, etc.) + model POJOs | Consumes those interfaces; never touches DAO or SQL directly |
| **Git Branch** | `feature/backend-core` | `feature/frontend-servlets` |
| **Never touches** | JSP/HTML/CSS files | DAO implementations, SQL queries |

> **Golden Rule:** Dev A owns everything **below** the Servlet layer. Dev B owns everything **at and above** the Servlet layer.
> They meet at the Java service interface — a contract committed in Phase 0 so neither blocks the other.

---

## 🏗️ Project File Structure

```
eventra/
├── pom.xml                                    ← Maven build; all dependencies declared here
├── AGENTS.md                                  ← AI guardrails (do not modify without consensus)
├── PLAN_OF_ACTION.md                          ← This file
├── db/
│   ├── schema.sql                             ← [A] Authoritative DDL — all 13 tables
│   └── seed_data.sql                          ← [A] Sample data for demo (10 events, 5 users)
└── src/
    ├── main/
    │   ├── java/com/eventra/
    │   │   ├── model/                         ← [A] POJOs only — User, Event, Registration, etc.
    │   │   ├── exception/                     ← [A] Custom exceptions only
    │   │   ├── dao/
    │   │   │   ├── interfaces/                ← [A] IUserDAO, IEventDAO, IRegistrationDAO, etc.
    │   │   │   └── impl/                      ← [A] JDBC implementations — SQL lives ONLY here
    │   │   ├── service/
    │   │   │   ├── interfaces/                ← [A] IUserService, IEventService, IRegistrationService
    │   │   │   └── impl/                      ← [A] Business logic — calls DAOs, never SQL
    │   │   ├── util/                          ← [A] DBConnection, PasswordUtil, ValidationUtil
    │   │   └── servlet/                       ← [B] HTTP layer — calls services, never DAOs
    │   └── webapp/
    │       ├── WEB-INF/
    │       │   └── web.xml                    ← [B] All servlet mappings + filter config
    │       ├── assets/
    │       │   ├── css/
    │       │   │   └── eventra.css            ← [B] ALL custom styles — no inline style="" ever
    │       │   └── js/
    │       │       └── main.js                ← [B] Search filter, form validation, seat polling
    │       ├── login.jsp                      ← [B] Split-layout login page
    │       ├── register.jsp                   ← [B] Sign-up + interest selection
    │       ├── error.jsp                      ← [B] User-friendly error page
    │       ├── attendee/
    │       │   ├── dashboard.jsp              ← [B] Recommendations + personal schedule
    │       │   ├── events.jsp                 ← [B] Event discovery + category filter
    │       │   ├── my-bookings.jsp            ← [B] Upcoming/past bookings
    │       │   └── profile.jsp                ← [B] Interest profile editor
    │       └── admin/
    │           ├── dashboard.jsp              ← [B] Stats cards + event management table
    │           ├── create-event.jsp           ← [B] Create / edit event form
    │           ├── registrations.jsp          ← [B] All registrations per event + check-in
    │           └── event-report.jsp           ← [B] Post-event analytics
    └── test/java/com/eventra/
        ├── service/                           ← [A] JUnit + Mockito service tests
        └── dao/                               ← [A] DAO tests (mocked connection)
```

---

## 🗃️ Database Schema — All 13 Tables

```sql
-- ═══════════════════════════════════════════
-- PHASE 1 — Foundation (must have)
-- ═══════════════════════════════════════════

users              → id, name, email, password_hash, role (ATTENDEE/ADMIN), created_at
events             → id, title, description, category, venue, start_time, end_time,
                     capacity, seats_left, status (UPCOMING/ALMOST_FULL/FULL/COMPLETED/CANCELLED),
                     organizer_id (FK → users)
registrations      → id, user_id (FK), event_id (FK), booking_ref (UNIQUE), seat_number,
                     status (CONFIRMED/CANCELLED/WAITLIST), registered_at
interests          → id, label (Workshop/Hackathon/Seminar/Cultural/Tech Talk/...)
user_interests     → user_id (FK), interest_id (FK)  [many-to-many]
event_interests    → event_id (FK), interest_id (FK)  [many-to-many]

-- ═══════════════════════════════════════════
-- PHASE 2 — Primary USP
-- ═══════════════════════════════════════════

check_ins          → id, registration_id (FK), checked_in_at, method (QR/MANUAL)
schedules          → id, user_id (FK), event_id (FK), added_at
recommendations    → id, user_id (FK), event_id (FK), score (0-100), generated_at

-- ═══════════════════════════════════════════
-- PHASE 3 — Networking
-- ═══════════════════════════════════════════

skill_profiles     → id, user_id (FK UNIQUE), bio, collaboration_goal
skills             → id, label
user_skills        → user_id (FK), skill_id (FK)
connection_requests → id, requester_id (FK → users), receiver_id (FK → users),
                      status (PENDING/ACCEPTED/DECLINED), created_at
```

---

## ⏱️ Phase-by-Phase Breakdown

---

### ⚡ PHASE 0 — Setup & Interface Contract `[0:00 – 0:45]`
> **Both devs together.** Nothing else starts until this is done.

**Goal:** One repo, one database, agreed interface contract. Neither dev blocks the other from compiling.

#### Shared Checklist
- [ ] MySQL database `eventra_db` created; `db.properties` file written (gitignored)
- [ ] Maven WAR project initialized (`mvn archetype:generate -DarchetypeArtifactId=maven-archetype-webapp`)
- [ ] `pom.xml` finalized with all dependencies (see pom.xml section below)
- [ ] Tomcat 9 configured in IDE OR `tomcat7-maven-plugin` added for `mvn tomcat7:run`
- [ ] GitHub repo initialized, `.gitignore` updated (`.class`, `target/`, `*.war`, `db.properties`, `*.iml`)
- [ ] Dev A pushes to `main`: all model POJOs + all DAO/Service **interfaces** + `DBConnection.java` stub
- [ ] Dev B pulls `main` and confirms the project compiles with no errors before branching

**Dev A Phase 0 Deliverable (push to main before Dev B branches):**
- `com.eventra.model` — `User`, `Event`, `Registration`, `Interest`, `CheckIn`, `SkillProfile`, `ConnectionRequest`
- `com.eventra.exception` — `EventFullException`, `DuplicateRegistrationException`, `AuthenticationException`
- `com.eventra.dao.interfaces` — `IUserDAO`, `IEventDAO`, `IRegistrationDAO`, `IInterestDAO`
- `com.eventra.service.interfaces` — `IUserService`, `IEventService`, `IRegistrationService`
- `com.eventra.util.DBConnection` — skeleton with `getConnection()` method
- `db/schema.sql` — at minimum: agreed table names and column names

**Dev B Phase 0 Deliverable:**
- `src/main/webapp/WEB-INF/web.xml` — skeleton with servlet stubs mapped
- `src/main/webapp/assets/css/eventra.css` — CSS variables + base reset imported
- `src/main/webapp/login.jsp` — static HTML (no servlet wiring yet)

---

### 🧱 PHASE 1 — Foundation `[0:45 – 4:00]`
> **This phase = ~80% of all rubric marks. Do not move to Phase 2 until every item is checked.**

---

#### 🅰️ Dev A — Backend Core `[0:45 – 4:00]`

**Prerequisites:** Phase 0 complete, MySQL running locally, JDBC driver on classpath

| # | Task | Details | Est. |
|---|---|---|---|
| A1 | **`db/schema.sql`** | All 6 Phase-1 tables. Constraints: `UNIQUE(email)`, `FK ON DELETE CASCADE`, `CHECK (capacity > 0)`, `CHECK (seats_left >= 0)`, charset `utf8mb4`. Run and verify with `SHOW CREATE TABLE`. | 30 min |
| A2 | **`DBConnection.java`** | Singleton pattern. Reads `db.properties` from classpath (`getResourceAsStream`). JDBC URL must include `?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`. Exposes `getConnection()`. | 20 min |
| A3 | **Model POJOs** | `User`, `Event`, `Registration`, `Interest`. All fields `private`. Getters/setters. `toString()`. `equals()`/`hashCode()` on ID. `AdminUser extends User` (adds `canManageEvents()` method). | 30 min |
| A4 | **`AbstractDAO`** | Abstract class. `protected Connection getConnection()` delegates to `DBConnection`. All DAO impls extend this. | 10 min |
| A5 | **`UserDAOImpl`** | `findByEmail(String)`, `findById(int)`, `save(User)`, `update(User)`. SHA-256 password hash in `PasswordUtil`, NOT in DAO. PreparedStatement everywhere. | 30 min |
| A6 | **`EventDAOImpl`** | `findAll()`, `findById(int)`, `findByCategory(String)`, `findByStatus(String)`, `save(Event)`, `update(Event)`, `delete(int)`. Returns `List<Event>`. Auto-updates status based on `seats_left` in service layer. | 40 min |
| A7 | **`RegistrationDAOImpl`** | `register(int userId, int eventId)` in transaction (check seats_left → insert → decrement → update status if needed), `cancel(int registrationId)` in transaction (cancel → increment seats_left → promote waitlist), `findByUserId(int)`, `findByEventId(int)`, `existsByUserAndEvent(int, int)`. | 45 min |
| A8 | **`UserServiceImpl`** | `login(email, password)` → hash password → findByEmail → compare → throw `AuthenticationException` if fail. `register(User, List<Integer> interestIds)` → check duplicate → save user → save interests. `getUserProfile(int userId)`. | 25 min |
| A9 | **`EventServiceImpl`** | `getAllEvents()`, `getEventsByCategory(String)`, `createEvent(Event, List<Integer> interestIds)`, `updateEvent(Event)`, `deleteEvent(int)`, `getEventWithDetails(int)`. Updates event status (ALMOST_FULL when < 10% seats remain). | 30 min |
| A10 | **`RegistrationServiceImpl`** | `registerForEvent(int userId, int eventId)` → check duplicate → check capacity → call DAO in transaction → throw `EventFullException` if full. `cancelRegistration(int registrationId)` → cancel + promote waitlist. `getUserBookings(int userId)`. | 30 min |
| A11 | **`db/seed_data.sql`** | 1 admin user (password: `admin123`), 2 attendee users, 5 events across 4 categories, 3 registrations, 4 interests. | 15 min |
| A12 | **JUnit Tests** | `EventServiceImplTest` + `RegistrationServiceImplTest`. Minimum 5 tests: happy path, `EventFullException`, `DuplicateRegistrationException`, cancel, waitlist promotion. All use `Mockito.mock()`. | 35 min |

**Phase 1 Dev A Deliverable:** All DAO impls + Service impls compile. `mvn test` passes. DB seeded. Dev B can call any service method without touching SQL.

---

#### 🅱️ Dev B — Frontend + Servlet Wiring `[0:45 – 4:00]`

**Prerequisites:** Dev A's Phase 0 commit is pulled and project compiles

| # | Task | Details | Est. |
|---|---|---|---|
| B1 | **`eventra.css`** | CSS variables from design system. `@import` Google Font Inter. Base reset. `.navbar`, `.event-card`, `.event-card__banner`, `.stat-card`, `.status-badge`, `.booking-card` styles. All hover transitions (`0.2s ease`). | 35 min |
| B2 | **`login.jsp`** | Two-panel layout: left dark brand panel (`#0D1117`) with `eventra` logo + tagline + feature bullets. Right panel: white-ish card with Login/Signup toggle tabs, Attendee/Admin role toggle buttons, email + password fields, "Log In" button, "New to Eventra? Create an account" link. JSTL for error message display. | 45 min |
| B3 | **`register.jsp`** | Name, email, password, confirm password fields. Interest checkboxes (Workshop, Hackathon, Seminar, Cultural, Tech Talk). "Sign Up" button. Links back to login. | 25 min |
| B4 | **`AuthFilter.java`** | Implements `javax.servlet.Filter`. `doFilter()` checks `session.getAttribute("user")` for `/attendee/*` and `/admin/*`. Redirects to `/login` with `?error=session_expired` if null. Also checks role: attendee hitting `/admin/*` redirected to `/attendee/dashboard`. | 25 min |
| B5 | **`LoginServlet.java`** | `doGet` → forward to `login.jsp`. `doPost` → `UserService.login()` → set session attrs (`user`, `userId`, `role`, `userName`) → `request.changeSessionId()` (session fixation prevention) → redirect by role. Catch `AuthenticationException` → redirect back with error param. | 30 min |
| B6 | **`RegisterServlet.java`** | `doGet` → forward to `register.jsp`. `doPost` → validate inputs (server-side: non-empty, email format, password match) → `UserService.register()` → catch `DuplicateRegistrationException` → redirect to `/login?registered=true`. | 20 min |
| B7 | **`LogoutServlet.java`** | `doGet` → `session.invalidate()` → redirect to `/login`. Mapped to `/logout`. | 10 min |
| B8 | **`attendee/events.jsp`** | Top nav with logo + links + avatar bubble. Category filter tabs (All / Workshops / Seminars / Hackathons / Cultural). Event cards: gradient banner by category, category badge, title, date icon + value, venue icon + value, seats-left progress bar (`seats_left/capacity`), "Register Now" button OR green "✓ Registered | View booking" if already registered. Search bar (JS filter). | 55 min |
| B9 | **`EventsServlet.java`** | `doGet` → `EventService.getAllEvents()` + `RegistrationService.getUserRegisteredEventIds(userId)` → put in request → forward to `events.jsp`. Filter by `?category=` param if present. `doPost` → `RegistrationService.registerForEvent()` → catch `EventFullException` (add to waitlist message) → catch `DuplicateRegistrationException` → PRG redirect to `/attendee/events`. | 30 min |
| B10 | **`attendee/my-bookings.jsp`** | Upcoming / Past tab toggle (JS). Each booking: date badge (day + month blocks), booking ref number, venue, seat number, status badge (CONFIRMED=green, WAITLIST=yellow, CANCELLED=grey), "View" + "Cancel" buttons. Empty state message if no bookings. | 35 min |
| B11 | **`MyBookingsServlet.java`** | `doGet` → `RegistrationService.getUserBookings(userId)` → split into upcoming/past → forward to `my-bookings.jsp`. `doPost` → `RegistrationService.cancelRegistration(registrationId)` → PRG redirect. | 20 min |
| B12 | **`admin/dashboard.jsp`** | Sidebar nav (Dashboard / Manage Events / Registrations / Attendees). Top stats row: 4 metric cards (Total Events, Total Registrations, Upcoming Events, Seats Left). Event table: Event name, Date, Registered/Capacity progress, Status badge (color-coded), Edit + Delete action buttons. "+ Create Event" button top right. | 50 min |
| B13 | **`AdminDashboardServlet.java`** | `doGet` → `AdminAnalyticsService` for 4 stats + `EventService.getAllEvents()` → put in request → forward to `admin/dashboard.jsp`. `doPost` → handle event delete → PRG redirect. | 20 min |

**Phase 1 Dev B Deliverable:** Full app navigable in browser. Login/logout works for both roles. Attendee can browse + register. Admin sees dashboard. AuthFilter blocks unauthorized access.

---

### 🚀 PHASE 2 — Primary USP `[4:00 – 7:30]`
> Recommendation engine + conflict detection = full marks on Problem Understanding & Solution Design.

---

#### 🅰️ Dev A — Intelligence Layer `[4:00 – 7:30]`

| # | Task | Details | Est. |
|---|---|---|---|
| A13 | **Schema migration** | Add `check_ins`, `schedules`, `recommendations` tables to `schema.sql`. Run `ALTER` statements (additive only). | 20 min |
| A14 | **`InterestDAOImpl`** | `findAll()`, `findByUserId(int)`, `findByEventId(int)`, `saveUserInterests(int userId, List<Integer> interestIds)`, `saveEventInterests(int eventId, List<Integer> interestIds)`. | 25 min |
| A15 | **`RecommendationServiceImpl`** | Core algorithm: `List<Event> getRecommendations(int userId)` → load user interests → for each available event, calculate `score = (matchingInterests / userInterests.size()) * 100` → return sorted by score descending. Store scores in `Map<Integer, Integer>` (eventId → score). Put top-5 in request. | 40 min |
| A16 | **`ScheduleConflictDetector`** | Utility class (not a service — stateless). `List<Event> detectConflicts(List<Event> registeredEvents)` → for each pair, check `a.startTime.before(b.endTime) && a.endTime.after(b.startTime)` → return list of conflicting events. `String getSuggestedAlternative(Event conflicting, List<Event> allEvents)` → find same-category event with no conflict. | 30 min |
| A17 | **`CheckInDAOImpl`** | `checkIn(int registrationId)` → insert into `check_ins`. `isCheckedIn(int registrationId)` → boolean. `getCheckInsByEvent(int eventId)` → `List<CheckIn>`. | 25 min |
| A18 | **`AttendanceAnalyticsService`** | `getAttendanceRate(int eventId)` = `checkIns / confirmedRegistrations * 100`. `getCapacityUtilization(int eventId)`. `getWaitlistConversionRate(int eventId)`. Returns `Map<String, Object>` of stats. | 30 min |
| A19 | **`AdminAnalyticsService`** | `getTotalEventCount()`, `getTotalRegistrationCount()`, `getUpcomingEventCount()`, `getTotalSeatsLeft()`. Each is a single COUNT/SUM query via DAO. | 20 min |
| A20 | **JUnit Tests** | `RecommendationServiceImplTest` — test 0% match, 100% match, partial match, empty interests. `ScheduleConflictDetectorTest` — exact overlap, partial overlap, no overlap, back-to-back (edge case: not a conflict). | 30 min |

**Phase 2 Dev A Deliverable:** Recommendation scores computable, conflict pairs detectable, check-in persisted, analytics queryable. New JUnit tests all green.

---

#### 🅱️ Dev B — USP Pages `[4:00 – 7:30]`

| # | Task | Details | Est. |
|---|---|---|---|
| B14 | **`attendee/dashboard.jsp`** | "Good morning, [Name]" greeting. "Recommended for You" section — event cards with blue `% match` badge in top-right corner. "Your Schedule" section — compact timeline list of registered events, with `⚠ Conflict` warning badge + conflicting event name if detected. Empty states for both sections. | 55 min |
| B15 | **`DashboardServlet.java`** | `doGet` → `RecommendationService.getRecommendations(userId)` → `RegistrationService.getUserBookings(userId)` → `ConflictDetector.detectConflicts(events)` → put all in request → forward to `attendee/dashboard.jsp`. | 25 min |
| B16 | **`attendee/profile.jsp`** | Interest tags as checkbox chip-style buttons (selected = blue filled, unselected = bordered). "Save Interests" button. Success/error flash message at top. | 25 min |
| B17 | **`ProfileServlet.java`** | `doGet` → `InterestService.findAll()` + `InterestService.findByUserId(userId)` → forward to `profile.jsp`. `doPost` → `InterestService.saveUserInterests(userId, selectedIds)` → PRG redirect with `?saved=true`. | 20 min |
| B18 | **`admin/create-event.jsp`** | Full form: Title, Description (textarea), Category (dropdown: Workshop/Hackathon/Seminar/Cultural), Venue, Start datetime, End datetime, Capacity (number). Interest tag checkboxes. "Create Event" / "Update Event" button. Pre-fills for edit mode via request attributes. | 35 min |
| B19 | **`CreateEventServlet.java`** | `doGet` → load interests → forward to form (edit: also load event by `?id=`). `doPost` → server-side validate (title non-empty, end > start, capacity > 0) → `EventService.createEvent()` or `updateEvent()` → PRG redirect to `/admin/dashboard`. | 30 min |
| B20 | **`admin/registrations.jsp`** | Event selector dropdown at top. Table: attendee name, email, booking ref, seat, status badge, registered-at, "Check In" button (disabled if already checked in, green if done). | 35 min |
| B21 | **`CheckInServlet.java`** | `doPost` → `CheckInService.checkIn(registrationId)` → respond with `application/json` `{"success":true,"message":"Checked in"}`. AJAX-friendly. Handles already-checked-in case gracefully. | 20 min |
| B22 | **Conflict UI in `events.jsp`** | On registration attempt, if conflict detected server-side, redirect back with `?conflict=EventTitle`. JavaScript reads URL param on page load and shows dismissible warning banner: `⚠ Schedule conflict with [EventTitle]. Register anyway?` with a confirm link. | 25 min |

**Phase 2 Dev B Deliverable:** Dashboard shows recommendations with % scores, conflict warnings appear, admin creates/edits events, check-in button works via AJAX.

---

### ✨ PHASE 3 — Networking & Polish `[7:30 – 10:00]`
> Build if time permits. Even partial implementation demonstrates breadth. Prioritise polish over new features if Phase 2 has rough edges.

---

#### 🅰️ Dev A `[7:30 – 10:00]`

| # | Task | Est. |
|---|---|---|
| A21 | Schema migration — add `skill_profiles`, `skills`, `user_skills`, `connection_requests` | 20 min |
| A22 | `SkillProfileDAOImpl` + `ConnectionRequestDAOImpl` | 40 min |
| A23 | `NetworkingService.getTeammateRecommendations(int userId, int eventId)` — weighted score: shared interests 40% + complementary skills 40% + compatible collab goals 20% | 40 min |
| A24 | Waitlist automation — `RegistrationServiceImpl.cancelRegistration()` auto-promotes first `WAITLIST` entry to `CONFIRMED`, decrements nothing (seat count already freed by cancellation) | 30 min |
| A25 | `PostEventReportService` — attendance rate, capacity utilization, cancellation count, top-3 interest groups, waitlist conversion | 25 min |

#### 🅱️ Dev B `[7:30 – 10:00]`

| # | Task | Est. |
|---|---|---|
| B23 | `attendee/networking.jsp` — Event selector, list of recommended teammates: avatar bubble (initials), name, match score, skills chips, "Connect" button | 50 min |
| B24 | `NetworkingServlet.java` — `doGet` loads recommendations; `doPost` sends connection request | 25 min |
| B25 | `admin/event-report.jsp` — Attendance rate gauge (CSS only), capacity bar, waitlist conversion %, cancellation count | 35 min |
| B26 | Responsive polish — media queries at 375px + 768px, card grid collapses to 1 col on mobile | 25 min |
| B27 | `error.jsp` + HTTP 404/500 mapped in `web.xml` — friendly message + "Go back home" button | 20 min |

---

### 🧪 PHASE 4 — Integration, Testing & Demo Prep `[10:00 – 12:00]`

| # | Task | Who | Time |
|---|---|---|---|
| T1 | End-to-end smoke: register → browse → register event → check booking → admin check-in | Both | 30 min |
| T2 | Validate DB constraints fire: duplicate reg, capacity overflow, FK cascade on event delete | Dev A | 20 min |
| T3 | Audit all DAO files — confirm zero `Statement` usage, zero string SQL concatenation | Dev A | 15 min |
| T4 | Auth filter edge cases: no session → redirect, attendee → admin URL → redirect | Dev B | 15 min |
| T5 | Seed DB: 10 events (4 categories), 5 users, 15+ registrations, 5+ check-ins, varied statuses | Dev A | 20 min |
| T6 | UI pass: all pages on Chrome at 1280px and 375px, event cards match mock UI | Dev B | 20 min |
| T7 | Rehearse demo script (see below) — time it, fix any broken redirects | Both | 20 min |

---

## 🔑 Critical Technical Decisions

### JDBC Transaction Pattern (8 marks)
```java
// RegistrationDAOImpl.register() — the most important method in the project
public Registration register(int userId, int eventId) throws EventFullException, DuplicateRegistrationException, SQLException {
    Connection conn = null;
    try {
        conn = getConnection();
        conn.setAutoCommit(false);

        // 1. Check for duplicate registration
        // 2. Check seats_left > 0 (or add to WAITLIST)
        // 3. INSERT into registrations
        // 4. UPDATE events SET seats_left = seats_left - 1
        // 5. UPDATE events SET status = 'ALMOST_FULL' or 'FULL' as needed

        conn.commit();
        return registration;
    } catch (Exception e) {
        if (conn != null) conn.rollback();
        throw e;
    } finally {
        if (conn != null) { conn.setAutoCommit(true); conn.close(); }
    }
}
```

### OOP Requirements Checklist (10 marks)
```
✅ Interfaces    : IUserDAO, IEventDAO, IRegistrationDAO, IInterestDAO, ICheckInDAO
                   IUserService, IEventService, IRegistrationService
✅ Abstract class: AbstractDAO (protected getConnection())
✅ Inheritance   : AdminUser extends User (canManageEvents(), canDeleteEvent())
✅ Exceptions    : EventFullException, DuplicateRegistrationException, AuthenticationException
✅ Enums         : EventStatus {UPCOMING, ALMOST_FULL, FULL, COMPLETED, CANCELLED}
                   UserRole {ATTENDEE, ADMIN}
                   RegistrationStatus {CONFIRMED, CANCELLED, WAITLIST}
✅ Collections   : List<Event>, Map<Integer, Integer> (eventId → score)
✅ Polymorphism  : IEventService service = new EventServiceImpl()
```

### Session Management (7 marks)
```java
// LoginServlet.doPost — correct and complete
HttpSession oldSession = request.getSession(false);
if (oldSession != null) oldSession.invalidate(); // prevent session fixation
HttpSession session = request.getSession(true);
session.setAttribute("user", user);              // full POJO for JSP access
session.setAttribute("userId", user.getId());
session.setAttribute("role", user.getRole().name());
session.setAttribute("userName", user.getName());
session.setMaxInactiveInterval(1800);            // 30 min timeout
response.sendRedirect(contextPath + (user.getRole() == UserRole.ADMIN
    ? "/admin/dashboard" : "/attendee/events"));
```

---

## ⚠️ Mistakes to Avoid (Hard-Won Experience)

| Mistake | Consequence | Fix |
|---|---|---|
| String concatenation in SQL | SQL injection — instant rubric fail | `PreparedStatement` with `?` always |
| Not closing ResultSet before PreparedStatement | Connection leak → Tomcat crash after ~50 requests | try-with-resources on all 3: conn, ps, rs |
| `forward()` after POST | Double-submit on browser refresh | `sendRedirect()` after every `doPost` (PRG pattern) |
| Business logic in `doPost` | Breaks layering rubric | Move to `ServiceImpl`, servlet only calls service |
| `getSession(true)` without invalidating old session | Session fixation vulnerability | `invalidate()` old session first, then create new |
| `seats_left` not updated atomically | Race condition — oversell | Single transaction: check + insert + decrement |
| JDBC URL missing `serverTimezone=UTC` | `SQLException: The server time zone value is unrecognized` on MySQL 8 | Always append timezone to URL |
| JSP scriptlets `<% %>` | Examiner marks it wrong immediately | JSTL `<c:forEach>` and `<c:if>` only |
| Inline `style=""` on elements | Breaks design system | Only `eventra.css` classes |
| Storing password as plain text | Critical security fail | SHA-256 via `PasswordUtil`, compare hashes |

---

## 📦 `pom.xml` — Complete Dependency List

```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.eventra</groupId>
  <artifactId>eventra</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>war</packaging>

  <properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <!-- Servlet API — provided by Tomcat, NEVER change scope -->
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <version>4.0.1</version>
      <scope>provided</scope>
    </dependency>
    <!-- JSP API — provided by Tomcat -->
    <dependency>
      <groupId>javax.servlet.jsp</groupId>
      <artifactId>javax.servlet.jsp-api</artifactId>
      <version>2.3.3</version>
      <scope>provided</scope>
    </dependency>
    <!-- JSTL — for JSP tags (c:forEach, c:if, fn:escapeXml) -->
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>jstl</artifactId>
      <version>1.2</version>
    </dependency>
    <!-- MySQL JDBC Driver -->
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>8.0.33</version>
    </dependency>
    <!-- JUnit 4 -->
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.13.2</version>
      <scope>test</scope>
    </dependency>
    <!-- Mockito -->
    <dependency>
      <groupId>org.mockito</groupId>
      <artifactId>mockito-core</artifactId>
      <version>4.11.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <finalName>eventra</finalName>
    <plugins>
      <!-- Embedded Tomcat for mvn tomcat7:run -->
      <plugin>
        <groupId>org.apache.tomcat.maven</groupId>
        <artifactId>tomcat7-maven-plugin</artifactId>
        <version>2.2</version>
        <configuration>
          <port>8080</port>
          <path>/eventra</path>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

---

## ✅ Master Review Checklist

### Problem Understanding & Design — 8 marks
- [ ] 3-layer architecture can be demonstrated (Servlet → Service → DAO)
- [ ] All 3 Eventra pillars present (Operations, Personalization, Networking)
- [ ] Recommendation algorithm coded with explainable score formula
- [ ] Conflict detection coded and warning visible in UI

### Core Java Concepts — 10 marks
- [ ] Minimum 8 interfaces used (5 DAO + 3 Service)
- [ ] Abstract class `AbstractDAO` exists and is extended by all DAO impls
- [ ] `AdminUser extends User` exists with at least 1 overriding/added method
- [ ] Minimum 3 custom exception classes
- [ ] Minimum 2 enums (`EventStatus`, `UserRole`)
- [ ] `List<Event>` and `Map<Integer, Integer>` used in recommendation service
- [ ] All exceptions handled with user-facing messages — no silent swallows

### Database Integration / JDBC — 8 marks
- [ ] All 13 tables in `schema.sql` with constraints
- [ ] Every SQL statement uses `PreparedStatement` (audit with grep before demo)
- [ ] At least 1 multi-statement transaction (registration flow)
- [ ] DAO pattern strictly followed — one impl per entity group
- [ ] JUnit tests pass: `mvn test` → BUILD SUCCESS

### Servlets & Web Integration — 7 marks
- [ ] `doGet` and `doPost` separated correctly in every servlet
- [ ] `AuthFilter` active on `/attendee/*` and `/admin/*`
- [ ] Session created on login, invalidated on logout
- [ ] PRG pattern: every `doPost` ends in `sendRedirect()`, never `forward()`
- [ ] `web.xml` has correct mapping for every servlet + filter
- [ ] At least 1 JSON response (check-in endpoint)
- [ ] `response.setContentType("text/html; charset=UTF-8")` in every doGet/doPost

---

## 🎯 Phase 1 Demo Script (Minimum Viable)

```
1.  Open http://localhost:8080/eventra
    → Lands on login page — dark split layout (matches pitch deck)

2.  Click "Create an account"
    → Register page: fill name, email, password, select "Workshop" + "Hackathon" interests

3.  Submit → redirected to login with success message

4.  Login as attendee (aarav@college.edu / password)
    → Redirected to events page — event cards with gradient banners, seats left progress bars

5.  Click "Register Now" on "Intro to Web Dev Workshop"
    → Page refreshes — button now shows "✓ Registered | View booking"

6.  Try registering for same event again
    → Inline error: "You are already registered for this event"

7.  Click "My Bookings" in navbar
    → Booking card: date badge (8 NOV), booking ref, seat #43, CONFIRMED badge (green)

8.  Click "Cancel" on booking
    → Booking moves to Past tab, status = CANCELLED

9.  Open new tab, login as admin (admin@eventra.com / admin123)
    → Admin dashboard: stat cards (Total Events: 5, Registrations: 12, Upcoming: 4, Seats Left: 138)

10. Click "+ Create Event"
    → Fill form → Submit → New event appears in dashboard table

11. Click "Registrations" in sidebar
    → Select event → see attendee table → click "Check In" button → turns green
```

---

## 🔥 Novelty Add-ons (If Ahead of Schedule)

| Feature | Impact on Examiner | Effort |
|---|---|---|
| Live seat counter via JS polling (`/api/seats?eventId=X`) | Shows async Servlet + AJAX | 30 min |
| Event status auto-badge (UPCOMING → ALMOST_FULL → FULL) | Shows business rule depth | 20 min |
| Export registrations to CSV (`/admin/export?eventId=X`) | Enterprise feel, demonstrates response streaming | 25 min |
| Interest tag cloud on attendee dashboard (size by frequency) | Visual wow, shows Map usage | 20 min |
| Booking confirmation "QR" display (CSS-styled barcode decoration) | Impressive to non-technical evaluators | 35 min |

---

*Team: The Third Commit — Akshita · Akriti · Anshiv*
*Sprint start: Phase 0 · Reference: AGENTS.md for all code guardrails*
