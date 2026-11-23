package com.campuslibrary.web;

import com.campuslibrary.dao.UserDao;
import com.campuslibrary.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Request context handed to every controller method: parameters, session user, flash messages, view rendering. */
public final class Ctx {
    public final HttpServletRequest req;
    public final HttpServletResponse res;
    final Map<String, String> path = new HashMap<>();
    private User user;
    private boolean userLoaded;

    Ctx(HttpServletRequest req, HttpServletResponse res) { this.req = req; this.res = res; }

    /** Trimmed request parameter ("" when missing). */
    public String param(String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }
    public boolean has(String name) { return req.getParameter(name) != null; }
    /** Id taken from the URL, e.g. /books/{id}. */
    public long id() { return Long.parseLong(path.get("id")); }
    public long longParam(String name, long def) {
        try { return Long.parseLong(param(name)); } catch (NumberFormatException e) { return def; }
    }
    public int intParam(String name, int def) {
        try { return Integer.parseInt(param(name)); } catch (NumberFormatException e) { return def; }
    }
    public int page() { return Math.max(1, intParam("page", 1)); }

    public User user() {
        if (!userLoaded) {
            userLoaded = true;
            Object uid = req.getSession().getAttribute("uid");
            user = uid == null ? null : UserDao.find((Long) uid);
        }
        return user;
    }
    public void login(User u) {
        req.changeSessionId();   // prevents session fixation
        req.getSession().setAttribute("uid", u.getId());
        user = u; userLoaded = true;
    }
    public void logout() { req.getSession().invalidate(); user = null; }

    public void set(String key, Object value) { req.setAttribute(key, value); }

    /** Copies the given request parameters in a map (used to refill a form after a validation error). */
    public Map<String, Object> form(String... names) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (String n : names) m.put(n, param(n));
        return m;
    }

    public void view(String name) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + name + ".jsp").forward(req, res);
    }
    public void view(String name, int status) throws ServletException, IOException {
        res.setStatus(status);
        view(name);
    }
    public void redirect(String target) throws IOException { res.sendRedirect(req.getContextPath() + target); }

    /** One-shot message displayed after the next page load. type: success | error | info */
    public void flash(String type, String message) {
        Map<String, String> f = new HashMap<>();
        f.put("type", type); f.put("message", message);
        req.getSession().setAttribute("flash", f);
    }
}
