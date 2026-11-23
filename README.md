# Campus Library

A library management website built with **Jakarta Servlets + JSP (Jakarta Server Pages 3.1)**, JSTL, JDBC and an embedded **H2** database.
Members browse, borrow, reserve and review books; librarians manage the catalogue, categories, members, loans and announcements.

Web Programming - Quiz 2 - individual project by Evann Andre Bloutin (5999261014).

## 1. Run it (2 minutes)

Requirements: **Java 17 or newer** (a JRE is enough). No Tomcat, no database and no Maven are needed:
Tomcat 10.1 is embedded and the H2 database is created automatically on the first start.

```bash
# Linux / macOS
./run.sh

# Windows (double-click or run in a terminal)
run.bat
```

Then open **http://localhost:8080**. Stop the server with `Ctrl+C`.

| Role      | E-mail                | Password   |
|-----------|-----------------------|------------|
| Librarian | `admin@library.test`  | `password` |
| Member    | `member@library.test` | `password` |
| Member    | `evann@library.test`, `sari@library.test`, `budi@library.test` | `password` |

Settings (environment variables, all optional): `PORT` (default 8080), `DATA_DIR` (default `./data`), `COVERS_ONLINE`
(`false` = never load real book covers from the Internet; the generated covers are used).
To reset the demo data, stop the server and delete the `data/` folder.

If port 8080 is busy: `PORT=9090 ./run.sh` (Linux/macOS) or `set PORT=9090 && run.bat` (Windows).

## 2. How to test

### a) Automatic test (91 checks)
With the server running on a **fresh** database (delete `data/` first), in another terminal:

```bash
./scripts/smoke-test.sh            # needs bash and curl (Linux, macOS, WSL or Git Bash)
```

It logs in, checks every page, the roles, borrowing/renewing/returning, the 3-loan limit, reservations, reviews,
favorites, the CRUD of books / categories / members / announcements, validation errors, CSRF protection and HTML escaping.
The last line must read `Result: 91 passed, 0 failed`.

### b) Manual checklist (about 10 minutes)
1. Log in as `member@library.test`. The dashboard shows an overdue book with its fine.
2. **Catalogue** -> search `al`, filter by category, tick *Available only*, change the sort, go to page 2.
3. Open *Clean Code* -> **Borrow**. Borrow two more books; the 4th is refused (limit of 3 active loans).
4. **My loans** -> *Renew* a loan (+7 days), then *Return* one. Use the All / Active / Overdue / Returned tabs.
5. Open *Pattern Recognition and Machine Learning* (no free copy) -> **Reserve**. Log in as `budi@library.test` and return that book:
   the first member in the queue (`member@library.test`) now sees **Ready to borrow** in *Reservations*.
6. Write a review (stars + comment), edit it, delete it. Add and remove a favorite (heart button).
7. **Profile** -> change your name, then your password (the current password is required).
8. Log out, log in as `admin@library.test`: use **Categories**, **Members**, **Announcements**, add / edit / delete a book
   (try an empty title or a duplicate ISBN to see the validation messages), delete a loan record.
9. Try to open `/members` as a member: you get a *Not allowed* page (403).
10. Create a new account with **Create one** on the login page.

## 3. Features (9 CRUD modules)

| # | Module | Create | Read | Update | Delete | Who |
|---|--------|:--:|:--:|:--:|:--:|-----|
| 1 | Categories | x | x | x | x | librarian |
| 2 | Books (with cover link, copies) | x | x | x | x | librarian writes, everyone reads |
| 3 | Members / accounts / roles | x | x | x | x | librarian |
| 4 | Loans (borrow, renew, return, fines) | x | x | x | x | member, librarian |
| 5 | Reviews (1 to 5 stars + comment) | x | x | x | x | member (librarian can delete) |
| 6 | Favorites (wish list) | x | x | | x | member |
| 7 | Reservations (queue) | x | x | x | x | member |
| 8 | Announcements (pinned or not) | x | x | x | x | librarian writes, everyone reads |
| 9 | Profile and password | | x | x | | member |

Also: registration / login / logout, dashboard with statistics and charts, search + filters + sorting + pagination, responsive layout.

## 4. Architecture (MVC)

```
Browser -> FrontServlet (routing, CSRF, access control)
              -> Controller (com.campuslibrary.controller.*)   validation, flash messages
                    -> DAO (com.campuslibrary.dao.*)           JDBC + SQL, transactions
                         -> H2 database (data/campus-library)
              -> View: JSP + JSTL/EL in webapp/WEB-INF/views, reusable tag files in WEB-INF/tags
```

* `src/main/java/com/campuslibrary/` - Java sources (`Main` starts the embedded Tomcat).
* `src/main/resources/schema.sql` - the 8 tables, 10 foreign keys.
* `webapp/` - JSP views, tag files, CSS, fonts (IBM Plex Serif + Manrope, bundled).
* `lib/` - the libraries (Tomcat embed 10.1, Jasper + Eclipse JDT compiler, JSTL, H2, jBCrypt).
* `scripts/` - `smoke-test.sh` (automatic test), `gen_models.py` (generates the model classes).

Security: bcrypt password hashes, CSRF token on every POST, session id renewed at login, HTML escaping of all user content,
server-side validation, prepared statements only (no SQL injection), role checks on every admin route.

Business rules: a loan lasts 14 days; max 3 active loans per member; the same book cannot be borrowed twice at once;
2 renewals of 7 days (not when overdue or when someone else is waiting); a late book costs Rp 1,000 per day;
a category that still holds books cannot be deleted; borrowing locks the book row (`SELECT ... FOR UPDATE`) inside a transaction.

## 5. Rebuild from source

```bash
./build.sh        # needs a JDK 17+ (javac); recreates campus-library.jar
```
JSP files are compiled on the fly: edit a `.jsp` or the CSS and just reload the page. After changing Java code, run `./build.sh` and restart.

## 6. Deploy (Docker)

A `Dockerfile` is provided (Java 17 + the same start command). Example:

```bash
docker build -t campus-library .
docker run -p 8080:8080 -v campus-data:/app/data campus-library
```
On a host such as Render, Railway or Fly.io, create a service from the Dockerfile, expose port 8080 (or read the `PORT`
variable, which the app already supports) and mount a persistent disk on `/app/data` to keep the database.

## Credits
Fonts: IBM Plex Serif and Manrope (SIL Open Font License). Real book covers (optional) come from Open Library by ISBN.
