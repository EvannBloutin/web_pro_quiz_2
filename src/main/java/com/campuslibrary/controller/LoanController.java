package com.campuslibrary.controller;

import com.campuslibrary.dao.LoanDao;
import com.campuslibrary.model.Loan;
import com.campuslibrary.util.ServiceException;
import com.campuslibrary.web.Ctx;
import com.campuslibrary.web.Router;

import static com.campuslibrary.web.Router.Access.*;

/** Loan history with status filter, return, renew and (librarian) delete. */
public final class LoanController {
    private LoanController() {}

    public static void register(Router r) {
        r.get("/loans", USER, c -> {
            boolean admin = c.user().isAdmin();
            boolean all = admin && !"mine".equals(c.param("scope"));
            String status = c.param("status");
            if (!status.matches("active|overdue|returned")) status = "all";
            c.set("result", LoanDao.list(all ? 0 : c.user().getId(), status, c.page(), 10));
            c.set("status", status); c.set("scopeAll", all);
            c.view("loans/index");
        });
        r.post("/loans/{id}/return", USER, c -> {
            try { LoanDao.giveBack(c.id(), c.user()); c.flash("success", "Book returned. Thank you!"); }
            catch (ServiceException e) { c.flash("error", e.getMessage()); }
            Web.back(c, "/loans");
        });
        r.post("/loans/{id}/renew", USER, c -> {
            try { LoanDao.renew(c.id(), c.user()); c.flash("success", "Loan renewed for " + LoanDao.RENEW_DAYS + " more days."); }
            catch (ServiceException e) { c.flash("error", e.getMessage()); }
            Web.back(c, "/loans");
        });
        r.post("/loans/{id}/delete", ADMIN, c -> {
            try { LoanDao.delete(c.id()); c.flash("success", "Loan record deleted."); }
            catch (ServiceException e) { c.flash("error", e.getMessage()); }
            Web.back(c, "/loans");
        });
    }
}
