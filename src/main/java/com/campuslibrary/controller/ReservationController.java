package com.campuslibrary.controller;

import com.campuslibrary.dao.ReservationDao;
import com.campuslibrary.util.ServiceException;
import com.campuslibrary.web.Router;

import static com.campuslibrary.web.Router.Access.USER;

/** Reservations: queue for a book without a free copy, list, cancel. */
public final class ReservationController {
    private ReservationController() {}

    public static void register(Router r) {
        r.get("/reservations", USER, c -> {
            boolean all = c.user().isAdmin() && "all".equals(c.param("scope"));
            c.set("items", ReservationDao.list(all ? 0 : c.user().getId(), false));
            c.set("scopeAll", all);
            c.view("reservations/index");
        });
        r.post("/books/{id}/reserve", USER, c -> {
            try { ReservationDao.reserve(c.user().getId(), c.id()); c.flash("success", "You joined the queue. We will mark the book as ready when a copy is returned."); }
            catch (ServiceException e) { c.flash("error", e.getMessage()); }
            Web.back(c, "/books/" + c.id());
        });
        r.post("/reservations/{id}/cancel", USER, c -> {
            try { ReservationDao.cancel(c.id(), c.user()); c.flash("success", "Reservation cancelled."); }
            catch (ServiceException e) { c.flash("error", e.getMessage()); }
            Web.back(c, "/reservations");
        });
    }
}
