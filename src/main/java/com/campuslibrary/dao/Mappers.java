package com.campuslibrary.dao;

import com.campuslibrary.db.Db.RowMapper;
import com.campuslibrary.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** ResultSet -> model mappers (one per query shape). */
final class Mappers {
    private Mappers() {}

    static final String BOOK_SELECT =
        "SELECT b.id, b.category_id, b.title, b.author, b.isbn, b.description, b.published_year, b.cover_url, "
      + "b.copies_total, b.copies_available, b.created_at, c.name AS category_name, c.color AS category_color, "
      + "COALESCE((SELECT AVG(CAST(r.rating AS DOUBLE)) FROM reviews r WHERE r.book_id = b.id), 0) AS avg_rating, "
      + "(SELECT COUNT(*) FROM reviews r WHERE r.book_id = b.id) AS review_count, "
      + "(SELECT COUNT(*) FROM loans l WHERE l.book_id = b.id) AS loan_count, "
      + "(SELECT COUNT(*) FROM favorites f WHERE f.book_id = b.id AND f.user_id = ?) AS fav "
      + "FROM books b JOIN categories c ON c.id = b.category_id";

    static final RowMapper<Book> BOOK = rs -> {
        Book b = new Book();
        b.setId(rs.getLong("id")); b.setCategoryId(rs.getLong("category_id"));
        b.setTitle(rs.getString("title")); b.setAuthor(rs.getString("author")); b.setIsbn(rs.getString("isbn"));
        b.setDescription(rs.getString("description"));
        int y = rs.getInt("published_year"); b.setPublishedYear(rs.wasNull() ? null : y);
        b.setCoverUrl(rs.getString("cover_url"));
        b.setCopiesTotal(rs.getInt("copies_total")); b.setCopiesAvailable(rs.getInt("copies_available"));
        b.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        b.setCategoryName(rs.getString("category_name")); b.setCategoryColor(rs.getString("category_color"));
        b.setAvgRating(rs.getDouble("avg_rating")); b.setReviewCount(rs.getInt("review_count"));
        b.setLoanCount(rs.getInt("loan_count")); b.setFavorite(rs.getInt("fav") > 0);
        return b;
    };

    static final String LOAN_SELECT =
        "SELECT l.id, l.user_id, l.book_id, l.borrowed_at, l.due_at, l.returned_at, l.renewals, "
      + "b.title AS book_title, b.author AS book_author, b.isbn AS book_isbn, b.cover_url AS book_cover_url, "
      + "b.category_id AS book_category_id, c.color AS category_color, u.name AS user_name "
      + "FROM loans l JOIN books b ON b.id = l.book_id JOIN categories c ON c.id = b.category_id "
      + "JOIN users u ON u.id = l.user_id";

    static final RowMapper<Loan> LOAN = rs -> {
        Loan l = new Loan();
        l.setId(rs.getLong("id")); l.setUserId(rs.getLong("user_id")); l.setBookId(rs.getLong("book_id"));
        l.setBorrowedAt(rs.getObject("borrowed_at", LocalDate.class)); l.setDueAt(rs.getObject("due_at", LocalDate.class));
        l.setReturnedAt(rs.getObject("returned_at", LocalDate.class)); l.setRenewals(rs.getInt("renewals"));
        l.setBookTitle(rs.getString("book_title")); l.setBookAuthor(rs.getString("book_author"));
        l.setBookIsbn(rs.getString("book_isbn")); l.setBookCoverUrl(rs.getString("book_cover_url"));
        l.setBookCategoryId(rs.getLong("book_category_id")); l.setCategoryColor(rs.getString("category_color"));
        l.setUserName(rs.getString("user_name"));
        return l;
    };

    static final String RES_SELECT =
        "SELECT r.id, r.user_id, r.book_id, r.status, r.created_at, b.title AS book_title, b.author AS book_author, "
      + "b.isbn AS book_isbn, b.cover_url AS book_cover_url, b.category_id AS book_category_id, "
      + "c.color AS category_color, u.name AS user_name, b.copies_available, "
      + "(SELECT COUNT(*) FROM reservations r2 WHERE r2.book_id = r.book_id AND r2.status IN ('WAITING','READY') "
      + "AND (r2.created_at < r.created_at OR (r2.created_at = r.created_at AND r2.id <= r.id))) AS position "
      + "FROM reservations r JOIN books b ON b.id = r.book_id JOIN categories c ON c.id = b.category_id "
      + "JOIN users u ON u.id = r.user_id";

    static final RowMapper<Reservation> RES = rs -> {
        Reservation r = new Reservation();
        r.setId(rs.getLong("id")); r.setUserId(rs.getLong("user_id")); r.setBookId(rs.getLong("book_id"));
        r.setStatus(rs.getString("status")); r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        r.setBookTitle(rs.getString("book_title")); r.setBookAuthor(rs.getString("book_author"));
        r.setBookIsbn(rs.getString("book_isbn")); r.setBookCoverUrl(rs.getString("book_cover_url"));
        r.setBookCategoryId(rs.getLong("book_category_id")); r.setCategoryColor(rs.getString("category_color"));
        r.setUserName(rs.getString("user_name")); r.setCopiesAvailable(rs.getInt("copies_available"));
        r.setPosition(rs.getInt("position"));
        return r;
    };

    static final RowMapper<User> USER = rs -> {
        User u = new User();
        u.setId(rs.getLong("id")); u.setName(rs.getString("name")); u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash")); u.setAdmin(rs.getBoolean("is_admin"));
        u.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return u;
    };

    static final RowMapper<Category> CATEGORY = rs -> {
        Category c = new Category();
        c.setId(rs.getLong("id")); c.setName(rs.getString("name")); c.setDescription(rs.getString("description"));
        c.setColor(rs.getString("color")); c.setBookCount(rs.getInt("book_count"));
        return c;
    };

    static final RowMapper<Announcement> ANNOUNCEMENT = rs -> {
        Announcement a = new Announcement();
        a.setId(rs.getLong("id")); a.setAuthorId(rs.getLong("author_id")); a.setTitle(rs.getString("title"));
        a.setBody(rs.getString("body")); a.setPinned(rs.getBoolean("pinned"));
        a.setCreatedAt(rs.getObject("created_at", LocalDateTime.class)); a.setAuthorName(rs.getString("author_name"));
        return a;
    };

    static final RowMapper<Review> REVIEW = rs -> {
        Review r = new Review();
        r.setId(rs.getLong("id")); r.setUserId(rs.getLong("user_id")); r.setBookId(rs.getLong("book_id"));
        r.setRating(rs.getInt("rating")); r.setComment(rs.getString("comment"));
        r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class)); r.setUserName(rs.getString("user_name"));
        return r;
    };
}
