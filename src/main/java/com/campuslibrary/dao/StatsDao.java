package com.campuslibrary.dao;

import com.campuslibrary.db.Db;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

/** Read-only statistics for the dashboard. */
public final class StatsDao {
    private StatsDao() {}

    public static Map<String, Object> counters(long userId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("books", Db.count("SELECT COUNT(*) FROM books"));
        m.put("copies", Db.count("SELECT COALESCE(SUM(copies_total), 0) FROM books"));
        m.put("categories", Db.count("SELECT COUNT(*) FROM categories"));
        m.put("members", Db.count("SELECT COUNT(*) FROM users"));
        m.put("activeLoans", Db.count("SELECT COUNT(*) FROM loans WHERE returned_at IS NULL"));
        m.put("overdueLoans", Db.count("SELECT COUNT(*) FROM loans WHERE returned_at IS NULL AND due_at < CURRENT_DATE"));
        m.put("myActive", Db.count("SELECT COUNT(*) FROM loans WHERE user_id = ? AND returned_at IS NULL", userId));
        m.put("myOverdue", Db.count("SELECT COUNT(*) FROM loans WHERE user_id = ? AND returned_at IS NULL AND due_at < CURRENT_DATE", userId));
        m.put("myRead", Db.count("SELECT COUNT(*) FROM loans WHERE user_id = ? AND returned_at IS NOT NULL", userId));
        m.put("myFavorites", Db.count("SELECT COUNT(*) FROM favorites WHERE user_id = ?", userId));
        m.put("myReady", Db.count("SELECT COUNT(*) FROM reservations WHERE user_id = ? AND status = 'READY'", userId));
        return m;
    }

    /** Number of loans per category (for the bar chart). */
    public static List<Map<String, Object>> loansByCategory() {
        return Db.list("SELECT c.name, c.color, COUNT(l.id) AS n FROM categories c LEFT JOIN books b ON b.category_id = c.id "
            + "LEFT JOIN loans l ON l.book_id = b.id GROUP BY c.id, c.name, c.color ORDER BY n DESC, c.name", rs -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", rs.getString("name")); m.put("color", rs.getString("color")); m.put("count", rs.getLong("n"));
            return m;
        });
    }

    /** Loans per month over the last {@code months} months, oldest first. */
    public static List<Map<String, Object>> loansPerMonth(int months) {
        YearMonth now = YearMonth.now();
        LocalDate from = now.minusMonths(months - 1).atDay(1);
        List<LocalDate> dates = Db.list("SELECT borrowed_at FROM loans WHERE borrowed_at >= ?", rs -> rs.getObject(1, LocalDate.class), from);
        Map<YearMonth, Integer> counts = new LinkedHashMap<>();
        for (int i = months - 1; i >= 0; i--) counts.put(now.minusMonths(i), 0);
        for (LocalDate d : dates) counts.computeIfPresent(YearMonth.from(d), (k, v) -> v + 1);
        List<Map<String, Object>> out = new ArrayList<>();
        counts.forEach((ym, n) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label", ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            m.put("count", n);
            out.add(m);
        });
        return out;
    }
}
