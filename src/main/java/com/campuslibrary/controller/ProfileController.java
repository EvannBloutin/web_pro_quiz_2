package com.campuslibrary.controller;

import com.campuslibrary.dao.UserDao;
import com.campuslibrary.model.User;
import com.campuslibrary.util.Pass;
import com.campuslibrary.web.Ctx;
import com.campuslibrary.web.Router;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.campuslibrary.web.Router.Access.USER;

/** Profile: edit name / e-mail, change password. */
public final class ProfileController {
    private ProfileController() {}

    public static void register(Router r) {
        r.get("/profile", USER, c -> show(c, null, null));
        r.post("/profile", USER, c -> {
            Map<String, String> errors = new LinkedHashMap<>();
            String name = c.param("name"), email = c.param("email");
            if (name.length() < 2 || name.length() > 100) errors.put("name", "Enter your name (2 to 100 characters).");
            if (!Web.validEmail(email)) errors.put("email", "Enter a valid e-mail address.");
            else if (UserDao.emailTaken(email, c.user().getId())) errors.put("email", "Another account already uses this e-mail.");
            if (!errors.isEmpty()) { show(c, errors, c.form("name", "email")); return; }
            UserDao.updateProfile(c.user().getId(), name, email);
            c.flash("success", "Your profile was updated.");
            c.redirect("/profile");
        });
        r.post("/profile/password", USER, c -> {
            Map<String, String> errors = new LinkedHashMap<>();
            String cur = c.req.getParameter("current") == null ? "" : c.req.getParameter("current");
            String pw = c.req.getParameter("password") == null ? "" : c.req.getParameter("password");
            String pw2 = c.req.getParameter("password2") == null ? "" : c.req.getParameter("password2");
            User u = c.user();
            if (!Pass.check(cur, u.getPasswordHash())) errors.put("current", "Your current password is incorrect.");
            if (pw.length() < 8) errors.put("password", "Use at least 8 characters.");
            else if (!pw.equals(pw2)) errors.put("password2", "The two passwords do not match.");
            if (!errors.isEmpty()) { show(c, errors, null); return; }
            UserDao.updatePassword(u.getId(), Pass.hash(pw));
            c.flash("success", "Your password was changed.");
            c.redirect("/profile");
        });
    }

    private static void show(Ctx c, Map<String, String> errors, Map<String, Object> form) throws Exception {
        User u = c.user();
        if (form == null) { form = new LinkedHashMap<>(); form.put("name", u.getName()); form.put("email", u.getEmail()); }
        c.set("form", form);
        if (errors != null) c.set("errors", errors);
        c.view("profile/edit", errors == null ? 200 : 422);
    }
}
