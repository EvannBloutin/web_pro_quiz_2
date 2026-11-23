package com.campuslibrary.dao;

import com.campuslibrary.db.Db;
import com.campuslibrary.model.Category;
import com.campuslibrary.util.ServiceException;

import java.util.List;

/** CRUD for categories. A category that still contains books cannot be deleted (ON DELETE RESTRICT). */
public final class CategoryDao {
    private CategoryDao() {}

    private static final String SELECT = "SELECT c.id, c.name, c.description, c.color, "
        + "(SELECT COUNT(*) FROM books b WHERE b.category_id = c.id) AS book_count FROM categories c";

    public static List<Category> all() { return Db.list(SELECT + " ORDER BY c.name", Mappers.CATEGORY); }
    public static Category find(long id) { return Db.one(SELECT + " WHERE c.id = ?", Mappers.CATEGORY, id); }
    public static boolean nameTaken(String name, long exceptId) {
        return Db.count("SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(?) AND id <> ?", name, exceptId) > 0;
    }
    public static long create(String name, String description, String color) {
        return Db.insert("INSERT INTO categories(name, description, color) VALUES (?,?,?)", name, description, color);
    }
    public static void update(long id, String name, String description, String color) {
        Db.update("UPDATE categories SET name = ?, description = ?, color = ? WHERE id = ?", name, description, color, id);
    }
    public static void delete(long id) throws ServiceException {
        long n = Db.count("SELECT COUNT(*) FROM books WHERE category_id = ?", id);
        if (n > 0) throw new ServiceException("This category still contains " + n + " book(s). Move or delete them first.");
        Db.update("DELETE FROM categories WHERE id = ?", id);
    }
}
