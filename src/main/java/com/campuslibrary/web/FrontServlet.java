package com.campuslibrary.web;

import com.campuslibrary.App;
import com.campuslibrary.dao.LoanDao;
import com.campuslibrary.dao.ReservationDao;
import com.campuslibrary.db.DbException;
import com.campuslibrary.model.User;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

/**
 * Front controller (the "C" of MVC): every request goes through here. It checks the CSRF token and the access level,
 * calls the controller method, and forwards to a JSP view (the "V") stored in WEB-INF/views.
 */
public class FrontServlet extends HttpServlet {
    private final Router router = new Router();
    private final SecureRandom random = new SecureRandom();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        App.registerRoutes(router);
        String covers = System.getProperty("covers.online", System.getenv().getOrDefault("COVERS_ONLINE", "true"));
        getServletContext().setAttribute("coversOnline", Boolean.parseBoolean(covers));
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length() - 1);
        String method = req.getMethod();
        if (method.equals("HEAD")) method = "GET";

        Ctx ctx = new Ctx(req, res);
        try {
            HttpSession session = req.getSession();
            String csrf = (String) session.getAttribute("csrf");
            if (csrf == null) {
                byte[] b = new byte[24];
                random.nextBytes(b);
                csrf = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
                session.setAttribute("csrf", csrf);
            }
            req.setAttribute("csrf", csrf);
            req.setAttribute("ctx", req.getContextPath());

            Router.Route route = router.match(method, path, ctx);
            if (route == null) throw new HttpError(404, "We could not find this page.");

            if (method.equals("POST")) {
                String sent = ctx.param("_csrf");
                if (!MessageDigest.isEqual(sent.getBytes(StandardCharsets.UTF_8), csrf.getBytes(StandardCharsets.UTF_8)))
                    throw new HttpError(403, "Your form has expired. Reload the page and try again.");
            }

            User user = ctx.user();
            if (route.access == Router.Access.GUEST && user != null) { ctx.redirect("/dashboard"); return; }
            if ((route.access == Router.Access.USER || route.access == Router.Access.ADMIN) && user == null) {
                ctx.flash("info", "Please log in to continue.");
                ctx.redirect("/login");
                return;
            }
            if (route.access == Router.Access.ADMIN && !user.isAdmin())
                throw new HttpError(403, "This page is reserved for librarians.");

            if (user != null) {
                req.setAttribute("user", user);
                req.setAttribute("navReady", ReservationDao.readyCount(user.getId()));
                req.setAttribute("navLoans", LoanDao.activeCount(user.getId()));
            }
            Object flash = session.getAttribute("flash");
            if (flash != null) {
                req.setAttribute("flash", (Map<?, ?>) flash);
                session.removeAttribute("flash");
            }
            route.handler.handle(ctx);
        } catch (HttpError e) {
            error(ctx, e.getStatus(), e.getMessage());
        } catch (DbException e) {
            log("Database error", e);
            error(ctx, 500, "A database error occurred. Please try again.");
        } catch (Exception e) {
            log("Unexpected error on " + method + " " + path, e);
            error(ctx, 500, "Something went wrong on our side.");
        }
    }

    private void error(Ctx ctx, int status, String message) throws ServletException, IOException {
        if (ctx.res.isCommitted()) return;
        ctx.res.resetBuffer();
        ctx.set("status", status);
        ctx.set("message", message);
        ctx.view("error", status);
    }
}
