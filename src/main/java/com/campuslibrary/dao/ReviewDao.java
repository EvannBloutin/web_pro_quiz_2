package com.campuslibrary.dao;

import com.campuslibrary.db.Db;
import com.campuslibrary.model.Review;

import java.util.List;

/** CRUD for reviews (one review per user and book). */
public final class ReviewDao {
    private ReviewDao() {}

    private static final String SELECT = "SELECT r.id, r.user_id, r.book_id, r.rating, r.comment, r.created_at, u.name AS user_name "
        + "FROM reviews r JOIN users u ON u.id = r.user_id";

    public static List<Review> forBook(long bookId) {
        return Db.list(SELECT + " WHERE r.book_id = ? ORDER BY r.created_at DESC", Mappers.REVIEW, bookId);
    }
    public static Review find(long id) { return Db.one(SELECT + " WHERE r.id = ?", Mappers.REVIEW, id); }
    public static Review find(long userId, long bookId) {
        return Db.one(SELECT + " WHERE r.user_id = ? AND r.book_id = ?", Mappers.REVIEW, userId, bookId);
    }
    /** Creates the review, or updates it when the user already reviewed this book. */
    public static void save(long userId, long bookId, int rating, String comment) {
        int n = Db.update("UPDATE reviews SET rating = ?, comment = ?, created_at = CURRENT_TIMESTAMP WHERE user_id = ? AND book_id = ?",
            rating, comment, userId, bookId);
        if (n == 0) Db.insert("INSERT INTO reviews(user_id, book_id, rating, comment) VALUES (?,?,?,?)", userId, bookId, rating, comment);
    }
    public static void delete(long id) { Db.update("DELETE FROM reviews WHERE id = ?", id); }
}
