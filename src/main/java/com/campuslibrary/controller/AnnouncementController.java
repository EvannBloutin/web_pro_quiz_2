package com.campuslibrary.controller;

import com.campuslibrary.dao.AnnouncementDao;
import com.campuslibrary.model.Announcement;
import com.campuslibrary.web.Ctx;
import com.campuslibrary.web.HttpError;
import com.campuslibrary.web.Router;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.campuslibrary.web.Router.Access.*;

/** Announcements: everyone reads them, librarians create / edit / delete / pin them. */
public final class AnnouncementController {
    private AnnouncementController() {}

    public static void register(Router r) {
        r.get("/announcements", USER, c -> { c.set("items", AnnouncementDao.all()); c.view("announcements/index"); });
        r.get("/announcements/new", ADMIN, c -> {
            Map<String, Object> f = new LinkedHashMap<>(); f.put("title", ""); f.put("body", ""); f.put("pinned", "");
            c.set("form", f); c.set("mode", "new"); c.view("announcements/form");
        });
        r.post("/announcements", ADMIN, c -> save(c, 0));
        r.get("/announcements/{id}/edit", ADMIN, c -> {
            Announcement a = load(c);
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("title", a.getTitle()); f.put("body", a.getBody()); f.put("pinned", a.isPinned() ? "on" : "");
            c.set("form", f); c.set("mode", "edit"); c.set("item", a); c.view("announcements/form");
        });
        r.post("/announcements/{id}/update", ADMIN, c -> save(c, load(c).getId()));
        r.post("/announcements/{id}/delete", ADMIN, c -> {
            Announcement a = load(c);
            AnnouncementDao.delete(a.getId());
            c.flash("success", "Announcement deleted.");
            c.redirect("/announcements");
        });
    }

    private static Announcement load(Ctx c) {
        Announcement a = AnnouncementDao.find(c.id());
        if (a == null) throw new HttpError(404, "This announcement does not exist.");
        return a;
    }

    private static void save(Ctx c, long id) throws Exception {
        Map<String, String> errors = new LinkedHashMap<>();
        String title = c.param("title"), body = c.param("body");
        boolean pinned = c.has("pinned");
        if (title.isEmpty() || title.length() > 150) errors.put("title", "Enter a title (up to 150 characters).");
        if (body.isEmpty() || body.length() > 2000) errors.put("body", "Write the announcement (up to 2000 characters).");
        if (!errors.isEmpty()) {
            c.set("errors", errors); c.set("form", c.form("title", "body", "pinned"));
            c.set("mode", id == 0 ? "new" : "edit"); if (id != 0) c.set("item", AnnouncementDao.find(id));
            c.view("announcements/form", 422);
            return;
        }
        if (id == 0) { AnnouncementDao.create(c.user().getId(), title, body, pinned); c.flash("success", "Announcement published."); }
        else { AnnouncementDao.update(id, title, body, pinned); c.flash("success", "Announcement updated."); }
        c.redirect("/announcements");
    }
}
