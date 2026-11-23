package com.campuslibrary.controller;

import com.campuslibrary.dao.UserDao;
import com.campuslibrary.model.User;
import com.campuslibrary.util.Pass;
import com.campuslibrary.web.Ctx;
import com.campuslibrary.web.Router;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.campuslibrary.web.Router.Access.*;

/** Registration, login and logout. */
public final class AuthController {
    private AuthController() {}

    public static void register(Router r) {
        r.get("/", PUBLIC, c -> c.redirect("/dashboard"));
        r.get("/login", GUEST, c -> { c.set("form", c.form("email")); c.view("auth/login"); });
        r.post("/login", GUEST, AuthController::login);
        r.get("/register", GUEST, c -> { c.set("form", c.form("name", "email")); c.view("auth/register"); });
        r.post("/register", GUEST, AuthController::signUp);
        r.post("/logout", USER, c -> { c.logout(); c.flash("info", "You have been logged out."); c.redirect("/login"); });
    }

    private static void login(Ctx c) throws Exception {
        User u = UserDao.findByEmail(c.param("email"));
        if (u == null || !Pass.check(c.req.getParameter("password") == null ? "" : c.req.getParameter("password"), u.getPasswordHash())) {
            c.set("form", c.form("email"));
            c.set("error", "Incorrect e-mail or password.");
            c.view("auth/login", 401);
            return;
        }
        c.login(u);
        c.flash("success", "Welcome back, " + u.getName() + ".");
        c.redirect("/dashboard");
    }

    private static void signUp(Ctx c) throws Exception {
        Map<String, String> errors = new LinkedHashMap<>();
        String name = c.param("name"), email = c.param("email");
        String pw = c.req.getParameter("password") == null ? "" : c.req.getParameter("password");
        String pw2 = c.req.getParameter("password2") == null ? "" : c.req.getParameter("password2");
        if (name.length() < 2 || name.length() > 100) errors.put("name", "Enter your name (2 to 100 characters).");
        if (!Web.validEmail(email)) errors.put("email", "Enter a valid e-mail address.");
        else if (UserDao.emailTaken(email, 0)) errors.put("email", "An account already uses this e-mail.");
        if (pw.length() < 8) errors.put("password", "Use at least 8 characters.");
        else if (!pw.equals(pw2)) errors.put("password2", "The two passwords do not match.");
        if (!errors.isEmpty()) {
            c.set("errors", errors);
            c.set("form", c.form("name", "email"));
            c.view("auth/register", 422);
            return;
        }
        long id = UserDao.create(name, email, Pass.hash(pw), false);
        c.login(UserDao.find(id));
        c.flash("success", "Your account is ready. Welcome to Campus Library!");
        c.redirect("/dashboard");
    }
}
