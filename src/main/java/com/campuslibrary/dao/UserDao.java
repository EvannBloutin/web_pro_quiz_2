package com.campuslibrary.dao;

import com.campuslibrary.db.Db;
import com.campuslibrary.model.User;
import com.campuslibrary.util.Page;

import java.util.ArrayList;
import java.util.List;

/** CRUD for users (members and librarians). */
public final class UserDao {
    private UserDao() {}

    private static final String SELECT = "SELECT u.id, u.name, u.email, u.password_hash, u.is_admin, u.created_at, "
        + "(SELECT COUNT(*) FROM loans l WHERE l.user_id = u.id AND l.returned_at IS NULL) AS active_loans FROM users u";

    private static final Db.RowMapper<User> WITH_LOANS = rs -> {
        User u = Mappers.USER.map(rs);
        u.setActiveLoans(rs.getInt("active_loans"));
        return u;
    };

    public static User find(long id) { return Db.one(SELECT + " WHERE u.id = ?", WITH_LOANS, id); }
    public static User findByEmail(String email) { return Db.one(SELECT + " WHERE LOWER(u.email) = LOWER(?)", WITH_LOANS, email); }

    public static boolean emailTaken(String email, long exceptId) {
        return Db.count("SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?) AND id <> ?", email, exceptId) > 0;
    }
    public static long create(String name, String email, String hash, boolean admin) {
        return Db.insert("INSERT INTO users(name, email, password_hash, is_admin) VALUES (?,?,?,?)", name, email, hash, admin);
    }
    public static void update(long id, String name, String email, boolean admin) {
        Db.update("UPDATE users SET name = ?, email = ?, is_admin = ? WHERE id = ?", name, email, admin, id);
    }
    public static void updateProfile(long id, String name, String email) {
        Db.update("UPDATE users SET name = ?, email = ? WHERE id = ?", name, email, id);
    }
    public static void updatePassword(long id, String hash) { Db.update("UPDATE users SET password_hash = ? WHERE id = ?", hash, id); }
    public static void delete(long id) { Db.update("DELETE FROM users WHERE id = ?", id); }
    public static long count() { return Db.count("SELECT COUNT(*) FROM users"); }

    public static Page<User> search(String q, int page, int size) {
        List<Object> p = new ArrayList<>();
        String where = "";
        if (!q.isEmpty()) {
            where = " WHERE LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ?";
            String like = "%" + q.toLowerCase() + "%";
            p.add(like); p.add(like);
        }
        long total = Db.count("SELECT COUNT(*) FROM users u" + where, p.toArray());
        p.add(size); p.add((page - 1) * size);
        List<User> items = Db.list(SELECT + where + " ORDER BY u.is_admin DESC, u.name LIMIT ? OFFSET ?", WITH_LOANS, p.toArray());
        return new Page<>(items, total, page, size);
    }
}
