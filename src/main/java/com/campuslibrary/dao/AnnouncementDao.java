package com.campuslibrary.dao;

import com.campuslibrary.db.Db;
import com.campuslibrary.model.Announcement;

import java.util.List;

/** CRUD for library announcements (written by librarians, shown on every dashboard). */
public final class AnnouncementDao {
    private AnnouncementDao() {}

    private static final String SELECT = "SELECT a.id, a.author_id, a.title, a.body, a.pinned, a.created_at, u.name AS author_name "
        + "FROM announcements a JOIN users u ON u.id = a.author_id";

    public static List<Announcement> all() { return Db.list(SELECT + " ORDER BY a.pinned DESC, a.created_at DESC", Mappers.ANNOUNCEMENT); }
    public static List<Announcement> latest(int n) {
        return Db.list(SELECT + " ORDER BY a.pinned DESC, a.created_at DESC LIMIT ?", Mappers.ANNOUNCEMENT, n);
    }
    public static Announcement find(long id) { return Db.one(SELECT + " WHERE a.id = ?", Mappers.ANNOUNCEMENT, id); }
    public static long create(long authorId, String title, String body, boolean pinned) {
        return Db.insert("INSERT INTO announcements(author_id, title, body, pinned) VALUES (?,?,?,?)", authorId, title, body, pinned);
    }
    public static void update(long id, String title, String body, boolean pinned) {
        Db.update("UPDATE announcements SET title = ?, body = ?, pinned = ? WHERE id = ?", title, body, pinned, id);
    }
    public static void delete(long id) { Db.update("DELETE FROM announcements WHERE id = ?", id); }
}
