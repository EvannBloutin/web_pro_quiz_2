package com.campuslibrary.controller;

import com.campuslibrary.dao.UserDao;
import com.campuslibrary.model.User;
import com.campuslibrary.util.Pass;
import com.campuslibrary.web.Ctx;
import com.campuslibrary.web.HttpError;
import com.campuslibrary.web.Router;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.campuslibrary.web.Router.Access.ADMIN;

/** Member management (librarians only): list / search, create, edit (role included), delete. */
public final class MemberController {
    private MemberController() {}

    public static void register(Router r) {
        r.get("/members", ADMIN, c -> {
            c.set("q", c.param("q"));
            c.set("result", UserDao.search(c.param("q"), c.page(), 10));
            c.view("members/index");
        });
        r.get("/members/new", ADMIN, c -> { c.set("form", new LinkedHashMap<String, Object>(Map.of("name", "", "email", "", "role", "member"))); c.set("mode", "new"); c.view("members/form"); });
        r.post("/members", ADMIN, c -> save(c, 0));
        r.get("/members/{id}/edit", ADMIN, c -> {
            User u = load(c);
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("name", u.getName()); f.put("email", u.getEmail()); f.put("role", u.isAdmin() ? "admin" : "member");
            c.set("form", f); c.set("mode", "edit"); c.set("item", u); c.view("members/form");
        });
        r.post("/members/{id}/update", ADMIN, c -> save(c, load(c).getId()));
        r.post("/members/{id}/delete", ADMIN, c -> {
            User u = load(c);
            if (u.getId() == c.user().getId()) c.flash("error", "You cannot delete your own account.");
            else if (u.getActiveLoans() > 0) c.flash("error", u.getName() + " still has " + u.getActiveLoans() + " active loan(s). Ask for the books back first.");
            else { UserDao.delete(u.getId()); c.flash("success", "Account of " + u.getName() + " was deleted."); }
            c.redirect("/members");
        });
    }

    private static User load(Ctx c) {
        User u = UserDao.find(c.id());
        if (u == null) throw new HttpError(404, "This member does not exist.");
        return u;
    }

    private static void save(Ctx c, long id) throws Exception {
        Map<String, String> errors = new LinkedHashMap<>();
        String name = c.param("name"), email = c.param("email"), role = c.param("role");
        String pw = c.req.getParameter("password") == null ? "" : c.req.getParameter("password");
        boolean admin = "admin".equals(role);
        if (name.length() < 2 || name.length() > 100) errors.put("name", "Enter a name (2 to 100 characters).");
        if (!Web.validEmail(email)) errors.put("email", "Enter a valid e-mail address.");
        else if (UserDao.emailTaken(email, id)) errors.put("email", "Another account already uses this e-mail.");
        if (id == 0 && pw.length() < 8) errors.put("password", "Use at least 8 characters.");
        if (id != 0 && !pw.isEmpty() && pw.length() < 8) errors.put("password", "Use at least 8 characters, or leave empty to keep the current one.");
        if (id == c.user().getId() && !admin) errors.put("role", "You cannot remove your own librarian role.");
        if (!errors.isEmpty()) {
            c.set("errors", errors); c.set("form", c.form("name", "email", "role"));
            c.set("mode", id == 0 ? "new" : "edit"); if (id != 0) c.set("item", UserDao.find(id));
            c.view("members/form", 422);
            return;
        }
        if (id == 0) { UserDao.create(name, email, Pass.hash(pw), admin); c.flash("success", "Account created for " + name + "."); }
        else {
            UserDao.update(id, name, email, admin);
            if (!pw.isEmpty()) UserDao.updatePassword(id, Pass.hash(pw));
            c.flash("success", "Account of " + name + " was updated.");
        }
        c.redirect("/members");
    }
}
