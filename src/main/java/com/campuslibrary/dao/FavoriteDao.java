package com.campuslibrary.dao;

import com.campuslibrary.db.Db;

/** Wish list: create / delete (toggle); the list itself is BookDao.search(favoritesOnly). */
public final class FavoriteDao {
    private FavoriteDao() {}

    /** @return true if the book is now a favorite, false if it was removed. */
    public static boolean toggle(long userId, long bookId) {
        int n = Db.update("DELETE FROM favorites WHERE user_id = ? AND book_id = ?", userId, bookId);
        if (n > 0) return false;
        Db.insert("INSERT INTO favorites(user_id, book_id) VALUES (?,?)", userId, bookId);
        return true;
    }
    public static long count(long userId) { return Db.count("SELECT COUNT(*) FROM favorites WHERE user_id = ?", userId); }
}
