package com.campuslibrary.dao;

import com.campuslibrary.db.Db;
import com.campuslibrary.model.Reservation;
import com.campuslibrary.model.User;
import com.campuslibrary.util.ServiceException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Reservations (holds): a member joins the queue of a book that has no free copy. */
public final class ReservationDao {
    private ReservationDao() {}

    public static long reserve(long userId, long bookId) throws ServiceException {
        return Tx.run(c -> {
            Integer avail = Db.one(c, "SELECT copies_available FROM books WHERE id = ? FOR UPDATE", rs -> rs.getInt(1), bookId);
            if (avail == null) throw new ServiceException("This book does not exist.");
            if (avail > 0) throw new ServiceException("A copy is available right now: you can borrow it instead of reserving it.");
            if (Db.count(c, "SELECT COUNT(*) FROM loans WHERE user_id = ? AND book_id = ? AND returned_at IS NULL", userId, bookId) > 0)
                throw new ServiceException("You already have this book on loan.");
            if (Db.count(c, "SELECT COUNT(*) FROM reservations WHERE user_id = ? AND book_id = ? AND status IN ('WAITING','READY')", userId, bookId) > 0)
                throw new ServiceException("You are already in the queue for this book.");
            return Db.insert(c, "INSERT INTO reservations(user_id, book_id, status) VALUES (?,?, 'WAITING')", userId, bookId);
        });
    }

    /** Cancel an open reservation (owner or librarian). If a READY hold is cancelled, the next member in line is promoted. */
    public static void cancel(long id, User actor) throws ServiceException {
        Tx.run(c -> {
            Reservation r = Db.one(c, "SELECT id, user_id, book_id, status FROM reservations WHERE id = ? FOR UPDATE", rs -> {
                Reservation x = new Reservation();
                x.setId(rs.getLong("id")); x.setUserId(rs.getLong("user_id")); x.setBookId(rs.getLong("book_id")); x.setStatus(rs.getString("status"));
                return x;
            }, id);
            if (r == null) throw new ServiceException("This reservation does not exist.");
            if (r.getUserId() != actor.getId() && !actor.isAdmin()) throw new ServiceException("You can only cancel your own reservations.");
            if (!r.isOpen()) throw new ServiceException("This reservation is already closed.");
            Db.update(c, "UPDATE reservations SET status = 'CANCELLED' WHERE id = ?", id);
            if (r.isReady()) promoteNext(c, r.getBookId());
            return null;
        });
    }

    /** Called when a copy comes back: the oldest WAITING reservation of that book becomes READY. */
    static void promoteNext(Connection c, long bookId) throws SQLException {
        Long next = Db.one(c, "SELECT id FROM reservations WHERE book_id = ? AND status = 'WAITING' ORDER BY created_at, id LIMIT 1",
            rs -> rs.getLong(1), bookId);
        if (next != null) Db.update(c, "UPDATE reservations SET status = 'READY' WHERE id = ?", next);
    }

    public static Reservation find(long id) { return Db.one(Mappers.RES_SELECT + " WHERE r.id = ?", Mappers.RES, id); }

    public static Reservation openFor(long userId, long bookId) {
        return Db.one(Mappers.RES_SELECT + " WHERE r.user_id = ? AND r.book_id = ? AND r.status IN ('WAITING','READY')", Mappers.RES, userId, bookId);
    }

    /** userId = 0 lists every member's reservations (librarian view). */
    public static List<Reservation> list(long userId, boolean openOnly) {
        String where = " WHERE 1 = 1" + (userId > 0 ? " AND r.user_id = ?" : "") + (openOnly ? " AND r.status IN ('WAITING','READY')" : "");
        Object[] p = userId > 0 ? new Object[]{userId} : new Object[]{};
        return Db.list(Mappers.RES_SELECT + where + " ORDER BY CASE WHEN r.status = 'READY' THEN 0 WHEN r.status = 'WAITING' THEN 1 ELSE 2 END, r.created_at DESC LIMIT 100",
            Mappers.RES, p);
    }
    public static long readyCount(long userId) {
        return Db.count("SELECT COUNT(*) FROM reservations WHERE user_id = ? AND status = 'READY'", userId);
    }
}
