package com.campuslibrary.controller;

import com.campuslibrary.dao.BookDao;
import com.campuslibrary.dao.CategoryDao;
import com.campuslibrary.dao.FavoriteDao;
import com.campuslibrary.web.HttpError;
import com.campuslibrary.web.Router;

import static com.campuslibrary.web.Router.Access.USER;

/** Wish list: list favorites, add / remove one. */
public final class FavoriteController {
    private FavoriteController() {}

    public static void register(Router r) {
        r.get("/favorites", USER, c -> {
            c.set("result", BookDao.search(c.user().getId(), "", 0, false, true, c.param("sort"), c.page(), 12));
            c.view("favorites/index");
        });
        r.post("/books/{id}/favorite", USER, c -> {
            if (BookDao.find(c.id(), 0) == null) throw new HttpError(404, "This book does not exist.");
            boolean now = FavoriteDao.toggle(c.user().getId(), c.id());
            c.flash("success", now ? "Added to your favorites." : "Removed from your favorites.");
            Web.back(c, "/books/" + c.id());
        });
    }
}
