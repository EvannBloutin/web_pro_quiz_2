package com.campuslibrary.dao;

import com.campuslibrary.db.Db;
import com.campuslibrary.model.Loan;
import com.campuslibrary.model.User;
import com.campuslibrary.util.Page;
import com.campuslibrary.util.ServiceException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Borrow / renew / return a book, loan history and filters. All state changes run in a transaction. */
public final class LoanDao {
    public static final int LOAN_DAYS = 14;
    public static final int RENEW_DAYS = 7;
    public static final int MAX_RENEWALS = 2;
    public static final int MAX_ACTIVE = 3;
    private LoanDao() {}

    /**
     * Borrow rules: a copy must be available, the same book cannot be borrowed twice at the same time and a member
     * can hold at most 3 active loans. The book row is locked (SELECT ... FOR UPDATE) so two members cannot take
     * the last copy together.
     */
    public static long borrow(long userId, long bookId) throws ServiceException {
        return Tx.run(c -> {
            Integer avail = Db.one(c, "SELECT copies_available FROM books WHERE id = ? FOR UPDATE", rs -> rs.getInt(1), bookId);
            if (avail == null) throw new ServiceException("This book does not exist.");
            if (avail <= 0) throw new ServiceException("No copy is available right now. You can reserve this book instead.");
            if (Db.count(c, "SELECT COUNT(*) FROM loans WHERE user_id = ? AND book_id = ? AND returned_at IS NULL", userId, bookId) > 0)
                throw new ServiceException("You already have this book on loan.");
            if (Db.count(c, "SELECT COUNT(*) FROM loans WHERE user_id = ? AND returned_at IS NULL", userId) >= MAX_ACTIVE)
                throw new ServiceException("You can have at most " + MAX_ACTIVE + " active loans. Return a book first.");
            LocalDate today = LocalDate.now();
            long id = Db.insert(c, "INSERT INTO loans(user_id, book_id, borrowed_at, due_at) VALUES (?,?,?,?)",
                userId, bookId, today, today.plusDays(LOAN_DAYS));
            Db.update(c, "UPDATE books SET copies_available = copies_available - 1 WHERE id = ?", bookId);
            Db.update(c, "UPDATE reservations SET status = 'FULFILLED' WHERE user_id = ? AND book_id = ? AND status IN ('WAITING','READY')",
                userId, bookId);
            return id;
        });
    }

    /** Return a book (the borrower or a librarian). The oldest waiting reservation becomes READY. */
    public static void giveBack(long loanId, User actor) throws ServiceException {
        Tx.run(c -> {
            Loan l = Db.one(c, "SELECT id, user_id, book_id, returned_at FROM loans WHERE id = ? FOR UPDATE", rs -> {
                Loan x = new Loan();
                x.setId(rs.getLong("id")); x.setUserId(rs.getLong("user_id")); x.setBookId(rs.getLong("book_id"));
                x.setReturnedAt(rs.getObject("returned_at", LocalDate.class));
                return x;
            }, loanId);
            if (l == null) throw new ServiceException("This loan does not exist.");
            if (l.getUserId() != actor.getId() && !actor.isAdmin()) throw new ServiceException("You can only return your own loans.");
            if (l.getReturnedAt() != null) throw new ServiceException("This book has already been returned.");
            Db.update(c, "UPDATE loans SET returned_at = ? WHERE id = ?", LocalDate.now(), loanId);
            Db.update(c, "UPDATE books SET copies_available = copies_available + 1 WHERE id = ?", l.getBookId());
            ReservationDao.promoteNext(c, l.getBookId());
            return null;
        });
    }

    /** Renew: +7 days, at most twice, not when overdue, not when another member is waiting for the book. */
    public static void renew(long loanId, User actor) throws ServiceException {
        Tx.run(c -> {
            Loan l = Db.one(c, "SELECT id, user_id, book_id, due_at, returned_at, renewals FROM loans WHERE id = ? FOR UPDATE", rs -> {
                Loan x = new Loan();
                x.setId(rs.getLong("id")); x.setUserId(rs.getLong("user_id")); x.setBookId(rs.getLong("book_id"));
                x.setDueAt(rs.getObject("due_at", LocalDate.class)); x.setReturnedAt(rs.getObject("returned_at", LocalDate.class));
                x.setRenewals(rs.getInt("renewals"));
                return x;
            }, loanId);
            if (l == null) throw new ServiceException("This loan does not exist.");
            if (l.getUserId() != actor.getId() && !actor.isAdmin()) throw new ServiceException("You can only renew your own loans.");
            if (l.getReturnedAt() != null) throw new ServiceException("This book has already been returned.");
            if (l.isOverdue()) throw new ServiceException("An overdue loan cannot be renewed. Please return the book.");
            if (l.getRenewals() >= MAX_RENEWALS) throw new ServiceException("This loan has already been renewed " + MAX_RENEWALS + " times.");
            if (Db.count(c, "SELECT COUNT(*) FROM reservations WHERE book_id = ? AND user_id <> ? AND status IN ('WAITING','READY')",
                l.getBookId(), l.getUserId()) > 0)
                throw new ServiceException("Another member has reserved this book, so it cannot be renewed.");
            Db.update(c, "UPDATE loans SET due_at = ?, renewals = renewals + 1 WHERE id = ?", l.getDueAt().plusDays(RENEW_DAYS), loanId);
            return null;
        });
    }

    /** Librarian only: remove a loan record (gives the copy back to the shelf if it was still out). */
    public static void delete(long loanId) throws ServiceException {
        Tx.run(c -> {
            Loan l = Db.one(c, "SELECT book_id, returned_at FROM loans WHERE id = ? FOR UPDATE", rs -> {
                Loan x = new Loan();
                x.setBookId(rs.getLong("book_id")); x.setReturnedAt(rs.getObject("returned_at", LocalDate.class));
                return x;
            }, loanId);
            if (l == null) throw new ServiceException("This loan does not exist.");
            if (l.getReturnedAt() == null) Db.update(c, "UPDATE books SET copies_available = copies_available + 1 WHERE id = ?", l.getBookId());
            Db.update(c, "DELETE FROM loans WHERE id = ?", loanId);
            return null;
        });
    }

    public static Loan find(long id) { return Db.one(Mappers.LOAN_SELECT + " WHERE l.id = ?", Mappers.LOAN, id); }

    public static Loan activeFor(long userId, long bookId) {
        return Db.one(Mappers.LOAN_SELECT + " WHERE l.user_id = ? AND l.book_id = ? AND l.returned_at IS NULL", Mappers.LOAN, userId, bookId);
    }

    /** History with a status filter: all, active, overdue, returned. userId = 0 means every member (librarian view). */
    public static Page<Loan> list(long userId, String status, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> p = new ArrayList<>();
        if (userId > 0) { where.append(" AND l.user_id = ?"); p.add(userId); }
        switch (status) {
            case "active": where.append(" AND l.returned_at IS NULL"); break;
            case "overdue": where.append(" AND l.returned_at IS NULL AND l.due_at < CURRENT_DATE"); break;
            case "returned": where.append(" AND l.returned_at IS NOT NULL"); break;
            default:
        }
        long total = Db.count("SELECT COUNT(*) FROM loans l" + where, p.toArray());
        p.add(size); p.add((page - 1) * size);
        List<Loan> items = Db.list(Mappers.LOAN_SELECT + where
            + " ORDER BY CASE WHEN l.returned_at IS NULL THEN 0 ELSE 1 END, l.due_at, l.id DESC LIMIT ? OFFSET ?", Mappers.LOAN, p.toArray());
        return new Page<>(items, total, page, size);
    }

    public static List<Loan> currentFor(long userId, int n) {
        return Db.list(Mappers.LOAN_SELECT + " WHERE l.user_id = ? AND l.returned_at IS NULL ORDER BY l.due_at LIMIT ?", Mappers.LOAN, userId, n);
    }
    public static long activeCount(long userId) { return Db.count("SELECT COUNT(*) FROM loans WHERE user_id = ? AND returned_at IS NULL", userId); }
}
