package com.campuslibrary.controller;

import com.campuslibrary.dao.*;
import com.campuslibrary.model.User;
import com.campuslibrary.web.Router;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.campuslibrary.web.Router.Access.USER;

/** Dashboard with statistics, charts, current loans and announcements. */
public final class DashboardController {
    private DashboardController() {}

    public static void register(Router r) {
        r.get("/dashboard", USER, c -> {
            User u = c.user();
            int h = LocalTime.now().getHour();
            c.set("greeting", h < 12 ? "Good morning" : (h < 18 ? "Good afternoon" : "Good evening"));
            c.set("stats", StatsDao.counters(u.getId()));
            List<Map<String, Object>> byCat = StatsDao.loansByCategory();
            long maxCat = byCat.stream().mapToLong(m -> (Long) m.get("count")).max().orElse(0);
            c.set("byCategory", byCat);
            c.set("maxCategory", maxCat);
            List<Map<String, Object>> byMonth = StatsDao.loansPerMonth(6);
            c.set("byMonth", byMonth);
            c.set("maxMonth", byMonth.stream().mapToInt(m -> (Integer) m.get("count")).max().orElse(0));
            c.set("myLoans", LoanDao.currentFor(u.getId(), 6));
            c.set("readyHolds", ReservationDao.list(u.getId(), true).stream().filter(x -> x.isReady()).toList());
            c.set("popular", BookDao.mostBorrowed(u.getId(), 5));
            c.set("recent", BookDao.recent(u.getId(), 6));
            c.set("announcements", AnnouncementDao.latest(3));
            c.view("dashboard");
        });
    }
}
