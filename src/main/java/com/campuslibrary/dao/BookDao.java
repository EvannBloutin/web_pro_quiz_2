package com.campuslibrary.dao;

import com.campuslibrary.db.Db;
import com.campuslibrary.model.Book;
import com.campuslibrary.util.Page;
import com.campuslibrary.util.ServiceException;

import java.util.ArrayList;
import java.util.List;

/** CRUD, search, filters, sorting and pagination for books. */
public final class BookDao {
    private BookDao() {}

    public static Book find(long id, long viewerId) {
        return Db.one(Mappers.BOOK_SELECT + " WHERE b.id = ?", Mappers.BOOK, viewerId, id);
    }

    public static boolean isbnTaken(String isbn, long exceptId) {
        return Db.count("SELECT COUNT(*) FROM books WHERE isbn = ? AND id <> ?", isbn, exceptId) > 0;
    }

    public static Page<Book> search(long viewerId, String q, long categoryId, boolean availableOnly,
                                    boolean favoritesOnly, String sort, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> wp = new ArrayList<>();
        if (!q.isEmpty()) {
            where.append(" AND (LOWER(b.title) LIKE ? OR LOWER(b.author) LIKE ? OR b.isbn LIKE ?)");
            String like = "%" + q.toLowerCase() + "%";
            wp.add(like); wp.add(like); wp.add("%" + q + "%");
        }
        if (categoryId > 0) { where.append(" AND b.category_id = ?"); wp.add(categoryId); }
        if (availableOnly) where.append(" AND b.copies_available > 0");
        if (favoritesOnly) { where.append(" AND EXISTS (SELECT 1 FROM favorites f2 WHERE f2.book_id = b.id AND f2.user_id = ?)"); wp.add(viewerId); }

        String order;
        switch (sort) {
            case "title": order = "b.title"; break;
            case "author": order = "b.author, b.title"; break;
            case "year": order = "b.published_year DESC, b.title"; break;
            case "rating": order = "avg_rating DESC, review_count DESC, b.title"; break;
            case "popular": order = "loan_count DESC, b.title"; break;
            default: order = "b.created_at DESC, b.id DESC";
        }
        long total = Db.count("SELECT COUNT(*) FROM books b" + where, wp.toArray());
        List<Object> p = new ArrayList<>();
        p.add(viewerId); p.addAll(wp); p.add(size); p.add((page - 1) * size);
        List<Book> items = Db.list(Mappers.BOOK_SELECT + where + " ORDER BY " + order + " LIMIT ? OFFSET ?", Mappers.BOOK, p.toArray());
        return new Page<>(items, total, page, size);
    }

    public static List<Book> mostBorrowed(long viewerId, int n) {
        return Db.list(Mappers.BOOK_SELECT + " ORDER BY loan_count DESC, b.title LIMIT ?", Mappers.BOOK, viewerId, n);
    }
    public static List<Book> recent(long viewerId, int n) {
        return Db.list(Mappers.BOOK_SELECT + " ORDER BY b.created_at DESC, b.id DESC LIMIT ?", Mappers.BOOK, viewerId, n);
    }
    public static List<Book> related(long viewerId, long categoryId, long exceptId, int n) {
        return Db.list(Mappers.BOOK_SELECT + " WHERE b.category_id = ? AND b.id <> ? ORDER BY loan_count DESC, b.title LIMIT ?",
            Mappers.BOOK, viewerId, categoryId, exceptId, n);
    }

    public static long create(Book b) {
        return Db.insert("INSERT INTO books(category_id, title, author, isbn, description, published_year, cover_url, copies_total, copies_available) "
            + "VALUES (?,?,?,?,?,?,?,?,?)", b.getCategoryId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getDescription(),
            b.getPublishedYear(), b.getCoverUrl(), b.getCopiesTotal(), b.getCopiesTotal());
    }

    /** Updates a book; when the total number of copies changes, the available copies stay consistent. */
    public static void update(Book b) throws ServiceException {
        Book old = find(b.getId(), 0);
        int borrowed = old.getCopiesTotal() - old.getCopiesAvailable();
        if (b.getCopiesTotal() < borrowed)
            throw new ServiceException("Total copies cannot be lower than the " + borrowed + " copy(ies) currently on loan.");
        Db.update("UPDATE books SET category_id = ?, title = ?, author = ?, isbn = ?, description = ?, published_year = ?, "
            + "cover_url = ?, copies_total = ?, copies_available = ? WHERE id = ?", b.getCategoryId(), b.getTitle(), b.getAuthor(),
            b.getIsbn(), b.getDescription(), b.getPublishedYear(), b.getCoverUrl(), b.getCopiesTotal(),
            b.getCopiesTotal() - borrowed, b.getId());
    }

    public static void delete(long id) { Db.update("DELETE FROM books WHERE id = ?", id); }
    public static long count() { return Db.count("SELECT COUNT(*) FROM books"); }
}
