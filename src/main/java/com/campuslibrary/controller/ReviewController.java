package com.campuslibrary.controller;

import com.campuslibrary.dao.BookDao;
import com.campuslibrary.dao.ReviewDao;
import com.campuslibrary.model.Review;
import com.campuslibrary.web.HttpError;
import com.campuslibrary.web.Router;

import static com.campuslibrary.web.Router.Access.USER;

/** Reviews: create / update (one per member and book) and delete (owner or librarian). */
public final class ReviewController {
    private ReviewController() {}

    public static void register(Router r) {
        r.post("/books/{id}/review", USER, c -> {
            if (BookDao.find(c.id(), 0) == null) throw new HttpError(404, "This book does not exist.");
            int rating = c.intParam("rating", 0);
            String comment = c.param("comment");
            if (rating < 1 || rating > 5) { c.flash("error", "Choose a rating from 1 to 5 stars."); c.redirect("/books/" + c.id()); return; }
            if (comment.length() > 1000) { c.flash("error", "Keep your comment under 1000 characters."); c.redirect("/books/" + c.id()); return; }
            boolean existed = ReviewDao.find(c.user().getId(), c.id()) != null;
            ReviewDao.save(c.user().getId(), c.id(), rating, comment.isEmpty() ? null : comment);
            c.flash("success", existed ? "Your review was updated." : "Thanks for your review!");
            c.redirect("/books/" + c.id() + "#reviews");
        });
        r.post("/reviews/{id}/delete", USER, c -> {
            Review rv = ReviewDao.find(c.id());
            if (rv == null) throw new HttpError(404, "This review does not exist.");
            if (rv.getUserId() != c.user().getId() && !c.user().isAdmin()) throw new HttpError(403, "You can only delete your own reviews.");
            ReviewDao.delete(rv.getId());
            c.flash("success", "Review deleted.");
            c.redirect("/books/" + rv.getBookId() + "#reviews");
        });
    }
}
