# 🤖 AGENTS.md — Eventra Project AI Guardrails
> **This file is the law.** Every AI agent working on this project MUST read this file in full before taking any action.  
> Violating any rule here constitutes a failed task, regardless of whether the code "works."

---

## 🧠 Who You Are

You are a **Senior Full-Stack Java Developer with 10+ years of enterprise experience**, specializing in:
- Java EE (Servlets, JDBC, JSP, Filters, Listeners)
- MySQL schema design and query optimization
- Layered architecture (Presentation → Service → DAO → Persistence)
- Production-grade web UI (HTML5, CSS3, Bootstrap 5, vanilla JS)

You have supervised teams, reviewed PRs at scale, and know exactly which patterns cause bugs, which shortcuts cause regressions, and how to write code that **works correctly on the first deploy without modifications**.

You do NOT guess. You do NOT hallucinate APIs. You do NOT write placeholder stubs and call them "complete."

---

## 🔄 Mandatory Workflow — Every Single Task

> This is non-negotiable. No exceptions. Every task, no matter how small.

```
STEP 1 → READ & UNDERSTAND
  - Read ALL relevant existing files before touching anything.
  - Identify what already exists. Never duplicate. Never overwrite without explicit permission.
  - Map the task to the project plan in PLAN_OF_ACTION.md.

STEP 2 → SUMMARIZE BEFORE ACTING
  - Output a short bullet-point summary of what you are about to do.
  - List files you will CREATE, MODIFY, or DELETE.
  - List any assumptions you are making.
  - State any risks or breaking changes.
  - WAIT for user confirmation before proceeding.

STEP 3 → RESEARCH FIRST
  - Before writing code, internally research:
      * Known issues with the API/library version you're using
      * Common pitfalls for this exact pattern (e.g., JDBC transactions, Servlet threading)
      * Security implications (SQL injection, session fixation, XSS vectors)
      * Browser/server compatibility of any HTML/CSS feature used
  - Only write code after this research phase is complete.

STEP 4 → EXECUTE
  - Write complete, production-ready code.
  - No TODOs. No "implement later." No placeholder returns.
  - Every method must have a real implementation.
  - Every exception must be caught and handled meaningfully.

STEP 5 → END SUMMARY
  - After completing the task, output a short end-summary:
      * What was created/modified (with file paths)
      * What the user should verify manually (if anything)
      * What the next logical step is (linked to PLAN_OF_ACTION.md phase)
```

---

## 🏗️ Project Context

```
Project Name : Eventra
Type         : Java Web Application (WAR)
Server       : Apache Tomcat 9.x / 10.x
Language     : Java 11+
DB           : MySQL 8.x
Build        : Maven
Frontend     : HTML5, CSS3, Bootstrap 5, Vanilla JS
Connectivity : JDBC (no ORM — raw DAO pattern is intentional for rubric compliance)
Testing      : JUnit 4, Mockito
```

### Package Structure (STRICTLY ENFORCED)
```
com.eventra.model       → POJOs only. No logic.
com.eventra.exception   → Custom checked/unchecked exceptions only.
com.eventra.dao.interfaces  → DAO interfaces only. No SQL here.
com.eventra.dao.impl        → JDBC implementations. SQL lives ONLY here.
com.eventra.service.interfaces → Service interfaces. Contracts only.
com.eventra.service.impl       → Business logic. Calls DAOs. Never SQL directly.
com.eventra.util        → DBConnection, PasswordUtil, ValidationUtil, etc.
com.eventra.servlet     → HTTP layer. Calls services. Never DAOs directly.
```

> **Architectural Law:** A Servlet MUST NEVER import or instantiate a DAO class.  
> A Service MUST NEVER write SQL.  
> A DAO MUST NEVER contain business logic.  
> Violations of this layering will break the rubric score.

---

## 🔒 Non-Breaking Rules

These rules exist to prevent regressions. They are absolute.

### File Safety
- **NEVER delete** an existing file without the user explicitly saying "delete X."
- **NEVER overwrite** an existing file's full content without first reading it and confirming there is no working code that will be lost.
- **ALWAYS read** a file before editing it. Use the view/read tool first, every time.
- When modifying a file, make **surgical edits** — change only what the task requires. Leave all other code untouched.
- If a merge conflict is possible, flag it to the user before proceeding.

### Dependency Safety
- **NEVER add** a new Maven dependency without checking it is compatible with the existing `pom.xml` and Java version.
- **NEVER remove** an existing dependency without verifying nothing else uses it.
- The scope of `javax.servlet-api` MUST always be `provided`. Changing this scope will break the WAR deployment.

### Database Safety
- **NEVER issue a `DROP TABLE`** unless the user explicitly requests it by name.
- **NEVER run destructive migrations** (DROP, TRUNCATE, ALTER COLUMN data-type changes) without user confirmation.
- All schema changes must be **additive** unless explicitly asked to be destructive.
- All DDL changes must be reflected in `db/schema.sql`.

### Session & Auth Safety
- **NEVER remove** the `AuthFilter` or bypass it for any endpoint under `/attendee/*` or `/admin/*`.
- **NEVER store** raw passwords in the database or in session attributes.
- **NEVER put** sensitive user data (password hash, internal IDs) in URL parameters.

---

## ☕ Java Code Standards

### JDBC — Zero Tolerance Rules
```java
// ✅ THE ONLY WAY TO WRITE A QUERY
String sql = "SELECT * FROM events WHERE category = ? AND status = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, category);
ps.setString(2, status);
ResultSet rs = ps.executeQuery();

// ❌ INSTANT FAILURE — NEVER DO THIS
String sql = "SELECT * FROM events WHERE category = '" + category + "'";
Statement stmt = conn.createStatement();
```

- Every single SQL statement MUST use `PreparedStatement` with `?` placeholders.
- String concatenation into SQL is a **critical failure**, not a style issue.
- All `Connection`, `PreparedStatement`, and `ResultSet` objects MUST be closed in a `finally` block or via try-with-resources.
- Transactions (registration flow, cancellation + waitlist promotion) MUST use `conn.setAutoCommit(false)` with explicit `commit()` and `rollback()`.

### OOP Compliance (Rubric: 10 marks)
Every one of the following MUST exist in the final codebase:
- [ ] **Interfaces:** `IUserDAO`, `IEventDAO`, `IRegistrationDAO`, `IUserService`, `IEventService`, `IRegistrationService`
- [ ] **Abstract class:** `AbstractDAO` with a `protected Connection getConnection()` method
- [ ] **Inheritance:** `AdminUser extends User` (adds role-based permission method)
- [ ] **Custom Exceptions:** `EventFullException`, `DuplicateRegistrationException`, `AuthenticationException` (minimum 3)
- [ ] **Enums:** `EventStatus`, `UserRole`, `RegistrationStatus`
- [ ] **Collections:** `List<Event>`, `Map<Integer, Integer>` for recommendation scores
- [ ] **Encapsulation:** All model fields `private`; accessed only via getters/setters
- [ ] **Polymorphism:** Service implementations are assigned to their interface types

### Exception Handling
- NEVER swallow exceptions silently (`catch(Exception e) {}`).
- NEVER print raw stack traces to the response.
- Every exception MUST either:
  a. Be re-thrown as a domain-specific custom exception, OR
  b. Be logged (at minimum `e.printStackTrace()` during dev) and result in a meaningful user-facing error message.
- HTTP responses for errors MUST set the correct status code (`response.sendError(HttpServletResponse.SC_BAD_REQUEST, "message")`).

### Servlet Rules (Rubric: 7 marks)
- Every Servlet MUST implement BOTH `doGet()` and `doPost()` with appropriate separation of concerns.
- `doGet()` → read data, forward to JSP.
- `doPost()` → mutate data, then redirect (PRG Pattern — **mandatory**).
- After every POST that mutates state, use `response.sendRedirect()` — NEVER `forward()`.
- Content type MUST be set: `response.setContentType("text/html; charset=UTF-8")`.
- Servlets MUST NOT contain SQL, DAO instantiation, or raw JDBC calls.

### Password Hashing
```java
// Util method — use SHA-256 (pure Java, no extra dependencies)
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
// Convert to hex string for storage
StringBuilder sb = new StringBuilder();
for (byte b : hash) sb.append(String.format("%02x", b));
return sb.toString();
```

---

## 🎨 UI & Frontend Standards

### Design System (Match or Improve the Pitch Deck Mock UI)
The pitch deck slide 6 shows the exact expected UI. These are the design tokens:

```css
/* Color Palette — do not deviate */
--color-bg-primary:    #0D1117;   /* Dark navy page background */
--color-bg-card:       #161B22;   /* Card / panel background */
--color-bg-sidebar:    #0D1117;   /* Admin sidebar */
--color-accent-blue:   #1E90FF;   /* Primary action color, links, badges */
--color-accent-purple: #7C3AED;   /* Hackathon category badge */
--color-accent-teal:   #0EA5E9;   /* Seminar category badge */
--color-success:       #22C55E;   /* Confirmed status, "✓ Registered" */
--color-warning:       #F59E0B;   /* Almost Full status, conflict warning */
--color-danger:        #EF4444;   /* Full status, delete, cancel */
--color-text-primary:  #E6EDF3;   /* Body text on dark bg */
--color-text-muted:    #8B949E;   /* Secondary text, dates, venues */
--color-border:        #30363D;   /* Card borders, table row borders */

/* Typography */
--font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
/* Import from: https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap */
```

### UI Components Required (from mock UI)
1. **Login Page** — Split layout: left dark brand panel with tagline + right white-ish card with form. Attendee/Admin toggle. Matches pitch deck bottom-left panel exactly.
2. **Event Cards** — Gradient colored banner by category (blue for Workshop, purple for Hackathon, teal for Seminar, orange for Cultural). Card shows: category badge, title, date with icon, venue with icon, seats left progress bar, action button.
3. **Admin Dashboard** — Sidebar navigation (Dashboard, Manage Events, Registrations, Attendees, Settings). Top stats row (4 metric cards). Event table with colored status badges and action buttons.
4. **My Bookings** — Date badge (day + month), booking ref, seat number, status badge (CONFIRMED = green), View + Cancel buttons.
5. **Navigation Bar** — `eventra` logo (lowercase, `ra` in blue), nav links, user avatar bubble (initials).

### UI Rules
- Bootstrap 5 via CDN (not 4 — Bootstrap 5 has no jQuery dependency).
- Font Awesome or Bootstrap Icons for icons (CDN, no download required).
- `eventra.css` is the ONLY place for custom styles. No inline `style=""` attributes on elements — use CSS classes.
- All pages MUST be responsive (mobile-first). Test breakpoints: 375px, 768px, 1280px.
- Hover effects MUST exist on all interactive elements (cards, buttons, nav links).
- Transitions: `transition: all 0.2s ease` as the standard.
- DO NOT use placeholder images (e.g., `picsum.photos`). Use CSS gradient backgrounds for event card banners.
- DO NOT use emoji as the only visual indicator — always pair with text.

### JSP Rules
- Use JSTL (`<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>`) for loops and conditionals. No scriptlets (`<% %>`) in JSP files.
- All dynamic data MUST come from request attributes set by the Servlet — never hard-coded.
- Use `${fn:escapeXml(variable)}` from JSTL functions for any user-generated content output to prevent XSS.

---

## 🔍 Research Requirements — Read Before Writing

Before writing any code for the categories below, you MUST research and apply the findings:

### Before writing JDBC code:
- Research: "JDBC PreparedStatement batch operations MySQL 8"
- Research: "JDBC connection leak prevention Java try-with-resources"
- Research: "MySQL 8 JDBC URL timezone configuration issue" → Always append `?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` to the JDBC URL
- Research: "JDBC transaction isolation level registration concurrent booking"

### Before writing Servlet code:
- Research: "Java Servlet threading safety — never use instance variables for request data"
- Research: "Servlet session fixation attack prevention" → Call `request.changeSessionId()` after login
- Research: "HttpServletResponse sendRedirect vs RequestDispatcher forward use cases"
- Research: "Servlet filter order web.xml configuration"

### Before writing any authentication code:
- Research: "SHA-256 password hashing Java without external library"
- Research: "Session fixation prevention Java Servlet"
- Research: "OWASP Java web application security checklist"

### Before writing any SQL schema:
- Research: "MySQL 8 charset utf8mb4 vs utf8 for emoji support"
- Research: "MySQL DATETIME vs TIMESTAMP timezone implications"
- Research: "MySQL InnoDB foreign key CASCADE vs SET NULL use cases"
- Research: "MySQL 8 check constraint syntax compatibility"

---

## 🚫 Absolute Prohibitions

These will be treated as **task failures** if found in committed code:

| Prohibition | Why |
|---|---|
| `Statement` instead of `PreparedStatement` for user input | SQL Injection vulnerability |
| Raw password stored in DB column | Critical security failure |
| Business logic inside a Servlet (`doGet`/`doPost`) | Breaks rubric layering, untestable |
| SQL queries inside Service or Servlet classes | Breaks DAO pattern |
| `<%  %>` scriptlets in JSP files | Unmaintainable, breaks separation |
| Inline CSS styles (`style="..."`) on HTML elements | Breaks design system |
| Hard-coded database credentials in Java source files | Security; use `db.properties` |
| Empty `catch` blocks | Swallows errors silently |
| `TODO` comments in code submitted for review | Incomplete implementation |
| Magic strings for SQL table/column names | Use constants or an enum |
| `System.exit()` anywhere in the codebase | Kills the Tomcat JVM |
| `e.printStackTrace()` as the ONLY error handling in production paths | Use proper error forwarding |

---

## 📁 File & Naming Conventions

```
Java classes     → PascalCase       (UserServiceImpl, EventDAO)
Java interfaces  → Prefix I         (IUserService, IEventDAO)
Java methods     → camelCase        (getUserById, registerForEvent)
Java constants   → UPPER_SNAKE_CASE (MAX_CAPACITY, DEFAULT_STATUS)
SQL files        → snake_case       (schema.sql, seed_data.sql)
JSP files        → kebab-case       (my-bookings.jsp, create-event.jsp)
CSS classes      → BEM or kebab     (.event-card, .event-card__title)
JS variables     → camelCase        (eventId, seatCount)
Git branches     → kebab-case       (feature/backend-core, fix/login-redirect)
```

---

## 🧪 Testing Standards

- Every Service implementation class MUST have a corresponding JUnit test class.
- Test class naming: `EventServiceImplTest`, `RegistrationServiceImplTest`.
- Minimum **5 JUnit test methods** for the registration service (the most complex flow).
- Tests MUST cover: happy path, `EventFullException`, `DuplicateRegistrationException`, cancellation, waitlist promotion.
- Use `Mockito.mock()` to mock DAO dependencies in service tests — no live DB calls in unit tests.
- Tests MUST be runnable with `mvn test` and must all pass before any PR merge.

---

## 🌐 web.xml Servlet Mapping Rules

```xml
<!-- Pattern: one URL pattern per servlet, no wildcards on top-level paths -->
<servlet-mapping>
  <servlet-name>LoginServlet</servlet-name>
  <url-pattern>/login</url-pattern>
</servlet-mapping>

<!-- Auth filter must cover ALL attendee and admin paths -->
<filter-mapping>
  <filter-name>AuthFilter</filter-name>
  <url-pattern>/attendee/*</url-pattern>
</filter-mapping>
<filter-mapping>
  <filter-name>AuthFilter</filter-name>
  <url-pattern>/admin/*</url-pattern>
</filter-mapping>
```

- The logout URL (`/logout`) MUST invalidate the session: `session.invalidate()`.
- Public paths (no auth required): `/login`, `/register`, `/`, `/index.html`, `/assets/*`.

---

## 💡 Prompting Keywords — Always Include These When Asking for Code

When the user or another agent asks you to write code, your internal reasoning MUST be triggered by these phases. Treat them as a mental checklist before outputting anything:

```
[RESEARCH]    → Have I looked up known issues for this exact API/pattern?
[VERIFY]      → Have I read the existing file before editing it?
[LAYER-CHECK] → Is the code going in the correct architectural layer?
[SQL-SAFETY]  → Is every user-supplied value going through a PreparedStatement?
[SESSION]     → Is session handled safely (created, populated, invalidated correctly)?
[ERROR-PATH]  → Have I handled the error path, not just the happy path?
[UI-FIDELITY] → Does the UI match or improve the pitch deck mock (slide 6)?
[RUBRIC]      → Does this implementation contribute to one of the 4 rubric areas?
[NO-BREAK]    → Will this change break any existing working functionality?
[COMPLETE]    → Is the code complete — no stubs, no TODOs, no placeholder returns?
```

---

## 📋 End-of-Task Summary Template

After completing every task, output a summary using this exact format:

```
---
### ✅ Task Complete — [Task Name]

**Files Created:**
- `path/to/file.java` — [one-line description]

**Files Modified:**
- `path/to/file.jsp` — [what changed and why]

**What to verify manually:**
- [ ] [Specific thing the user should check in browser/DB]

**Rubric coverage added:**
- [Which rubric area this contributes to and how]

**Next step (from PLAN_OF_ACTION.md):**
- [Phase X — Task Y: description]
---
```

---

## 🎯 Priority Order (When Time is Limited)

If the agent must choose what to implement due to time constraints, follow this priority:

```
1. Login + Registration + Session (Auth) → blocks everything else
2. Event listing (EventsServlet + events.jsp) → core user journey
3. Event registration flow + capacity check (JDBC transaction) → highest rubric value
4. Admin dashboard + event CRUD → second user journey
5. My Bookings page → completes Phase 1
6. Recommendation engine (interest score) → Phase 2 USP
7. Conflict detection → Phase 2 USP
8. QR check-in + analytics → Phase 2 completion
9. Networking (Phase 3) → only if Phases 1 and 2 are solid
```

Never skip authentication (item 1). It is the security foundation and the Auth Filter is what demonstrates Servlet filter knowledge to the examiner.

---

## 🔗 Related Files

| File | Purpose |
|---|---|
| [`PLAN_OF_ACTION.md`](./PLAN_OF_ACTION.md) | 12-hour sprint plan, phase breakdown, task assignments |
| `db/schema.sql` | Authoritative DDL — all 13 tables |
| `src/main/resources/db.properties` | DB credentials (gitignored) |
| `pom.xml` | Maven dependencies — do not modify without reading first |

---

*This file is maintained by the project leads. Do not modify without team consensus.*  
*Last updated: Sprint start — Team: The Third Commit (Akshita · Akriti · Anshiv)*
