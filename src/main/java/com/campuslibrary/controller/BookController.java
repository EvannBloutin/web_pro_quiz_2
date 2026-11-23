package com.campuslibrary.controller;

import com.campuslibrary.dao.*;
import com.campuslibrary.model.Book;
import com.campuslibrary.model.User;
import com.campuslibrary.util.ServiceException;
import com.campuslibrary.web.Ctx;
import com.campuslibrary.web.HttpError;
import com.campuslibrary.web.Router;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.campuslibrary.web.Router.Access.*;

/** Books: catalogue with search / filters / sorting / pagination (read), and admin create / update / delete. */
public final class BookController {
    private static final int PAGE_SIZE = 12;
    private BookController() {}

    public static void register(Router r) {
        r.get("/books", USER, BookController::index);
        r.get("/books/new", ADMIN, c -> { c.set("form", new LinkedHashMap<String, Object>(Map.of("copiesTotal", "1"))); formView(c, "new", null); });
        r.post("/books", ADMIN, BookController::create);
        r.get("/books/{id}", USER, BookController::show);
        r.get("/books/{id}/edit", ADMIN, c -> {
            Book b = load(c);
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("title", b.getTitle()); f.put("author", b.getAuthor()); f.put("isbn", b.getIsbn());
            f.put("categoryId", String.valueOf(b.getCategoryId()));
            f.put("publishedYear", b.getPublishedYear() == null ? "" : String.valueOf(b.getPublishedYear()));
            f.put("copiesTotal", String.valueOf(b.getCopiesTotal()));
            f.put("coverUrl", b.getCoverUrl() == null ? "" : b.getCoverUrl());
            f.put("description", b.getDescription() == null ? "" : b.getDescription());
            c.set("form", f);
            formView(c, "edit", b);
        });
        r.post("/books/{id}/update", ADMIN, BookController::update);
        r.post("/books/{id}/delete", ADMIN, c -> {
            Book b = load(c);
            BookDao.delete(b.getId());
            c.flash("success", "\u201c" + b.getTitle() + "\u201d was deleted from the catalogue.");
            c.redirect("/books");
        });
        r.post("/books/{id}/borrow", USER, c -> {
            try {
                LoanDao.borrow(c.user().getId(), c.id());
                c.flash("success", "Book borrowed. Please return it within " + LoanDao.LOAN_DAYS + " days.");
            } catch (ServiceException e) { c.flash("error", e.getMessage()); }
            Web.back(c, "/books/" + c.id());
        });
    }

    private static Book load(Ctx c) {
        Book b = BookDao.find(c.id(), c.user().getId());
        if (b == null) throw new HttpError(404, "This book does not exist.");
        return b;
    }

    private static void index(Ctx c) throws Exception {
        User u = c.user();
        String q = c.param("q"), sort = c.param("sort");
        long cat = c.longParam("category", 0);
        boolean avail = c.has("available");
        c.set("result", BookDao.search(u.getId(), q, cat, avail, false, sort, c.page(), PAGE_SIZE));
        c.set("categories", CategoryDao.all());
        c.set("q", q); c.set("cat", cat); c.set("avail", avail); c.set("sort", sort);
        c.view("books/index");
    }

    private static void show(Ctx c) throws Exception {
        User u = c.user();
        Book b = load(c);
        boolean hasLoan = LoanDao.activeFor(u.getId(), b.getId()) != null;
        c.set("book", b);
        c.set("reviews", ReviewDao.forBook(b.getId()));
        var mine = ReviewDao.find(u.getId(), b.getId());
        c.set("myReview", mine);
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("rating", mine == null ? "5" : String.valueOf(mine.getRating()));
        f.put("comment", mine == null ? "" : (mine.getComment() == null ? "" : mine.getComment()));
        c.set("form", f);
        c.set("activeLoan", LoanDao.activeFor(u.getId(), b.getId()));
        c.set("myReservation", ReservationDao.openFor(u.getId(), b.getId()));
        c.set("canBorrow", b.isAvailable() && !hasLoan && LoanDao.activeCount(u.getId()) < LoanDao.MAX_ACTIVE);
        c.set("related", BookDao.related(u.getId(), b.getCategoryId(), b.getId(), 4));
        c.view("books/show");
    }

    private static void formView(Ctx c, String mode, Book item) throws Exception {
        c.set("mode", mode);
        c.set("item", item);
        c.set("categories", CategoryDao.all());
        c.view("books/form");
    }

    private static Map<String, String> read(Ctx c, Book b, long exceptId) {
        Map<String, String> errors = new LinkedHashMap<>();
        b.setTitle(c.param("title")); b.setAuthor(c.param("author"));
        b.setIsbn(c.param("isbn").replaceAll("[\\s-]", "").toUpperCase());
        b.setDescription(c.param("description").isEmpty() ? null : c.param("description"));
        b.setCoverUrl(c.param("coverUrl").isEmpty() ? null : c.param("coverUrl"));
        if (b.getTitle().isEmpty() || b.getTitle().length() > 200) errors.put("title", "Enter a title (up to 200 characters).");
        if (b.getAuthor().isEmpty() || b.getAuthor().length() > 150) errors.put("author", "Enter the author(s).");
        if (!b.getIsbn().matches("\\d{9}[\\dX]|\\d{13}")) errors.put("isbn", "Enter a valid ISBN-10 or ISBN-13 (digits only, hyphens allowed).");
        else if (BookDao.isbnTaken(b.getIsbn(), exceptId)) errors.put("isbn", "Another book already uses this ISBN.");
        long cat = c.longParam("categoryId", 0);
        if (CategoryDao.find(cat) == null) errors.put("categoryId", "Choose a category.");
        b.setCategoryId(cat);
        if (c.param("publishedYear").isEmpty()) b.setPublishedYear(null);
        else {
            int y = c.intParam("publishedYear", -1);
            if (y < 1000 || y > Year.now().getValue() + 1) errors.put("publishedYear", "Enter a valid year.");
            b.setPublishedYear(y);
        }
        int copies = c.intParam("copiesTotal", -1);
        if (copies < 1 || copies > 999) errors.put("copiesTotal", "Enter a number of copies between 1 and 999.");
        b.setCopiesTotal(Math.max(copies, 0));
        if (b.getCoverUrl() != null && !b.getCoverUrl().matches("(?i)^(https?://|/assets/).{3,480}$")) errors.put("coverUrl", "Use an http(s) link to an image.");
        if (b.getDescription() != null && b.getDescription().length() > 2000) errors.put("description", "Keep the description under 2000 characters.");
        return errors;
    }

    private static final String[] FIELDS = {"title", "author", "isbn", "categoryId", "publishedYear", "copiesTotal", "coverUrl", "description"};

    private static void create(Ctx c) throws Exception {
        Book b = new Book();
        Map<String, String> errors = read(c, b, 0);
        if (!errors.isEmpty()) { c.set("errors", errors); c.set("form", c.form(FIELDS)); c.res.setStatus(422); formView(c, "new", null); return; }
        long id = BookDao.create(b);
        c.flash("success", "\u201c" + b.getTitle() + "\u201d was added to the catalogue.");
        c.redirect("/books/" + id);
    }

    private static void update(Ctx c) throws Exception {
        Book old = load(c);
        Book b = new Book();
        b.setId(old.getId());
        Map<String, String> errors = read(c, b, old.getId());
        if (errors.isEmpty()) {
            try { BookDao.update(b); }
            catch (ServiceException e) { errors.put("copiesTotal", e.getMessage()); }
        }
        if (!errors.isEmpty()) { c.set("errors", errors); c.set("form", c.form(FIELDS)); c.res.setStatus(422); formView(c, "edit", old); return; }
        c.flash("success", "\u201c" + b.getTitle() + "\u201d was updated.");
        c.redirect("/books/" + old.getId());
    }
}
