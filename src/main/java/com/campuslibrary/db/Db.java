package com.campuslibrary.db;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Tiny JDBC helper: connection pool, queries, inserts and transactions. */
public final class Db {
    private static BasicDataSource ds;
    private Db() {}

    @FunctionalInterface public interface RowMapper<T> { T map(ResultSet rs) throws SQLException; }
    @FunctionalInterface public interface Work<T> { T run(Connection c) throws Exception; }

    public static void init(String url) {
        ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl(url);
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setMaxTotal(12);
    }

    /** Creates the tables (schema.sql) when they do not exist yet. */
    public static void createSchema() {
        try (InputStream in = Db.class.getResourceAsStream("/schema.sql");
             Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String part : sql.split(";\\s*\\n")) {
                if (!part.isBlank()) st.execute(part);
            }
        } catch (SQLException | IOException e) { throw new RuntimeException(e); }
    }

    public static Connection conn() throws SQLException { return ds.getConnection(); }

    private static void bind(PreparedStatement ps, Object[] p) throws SQLException {
        for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
    }

    // ---- with an explicit connection (used inside transactions) ----
    public static <T> List<T> list(Connection c, String sql, RowMapper<T> m, Object... p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) out.add(m.map(rs));
                return out;
            }
        }
    }
    public static <T> T one(Connection c, String sql, RowMapper<T> m, Object... p) throws SQLException {
        List<T> l = list(c, sql, m, p);
        return l.isEmpty() ? null : l.get(0);
    }
    public static long count(Connection c, String sql, Object... p) throws SQLException {
        Long v = one(c, sql, rs -> rs.getLong(1), p);
        return v == null ? 0 : v;
    }
    public static int update(Connection c, String sql, Object... p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) { bind(ps, p); return ps.executeUpdate(); }
    }
    public static long insert(Connection c, String sql, Object... p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getLong(1) : 0; }
        }
    }

    // ---- auto-commit shortcuts ----
    public static <T> List<T> list(String sql, RowMapper<T> m, Object... p) {
        try (Connection c = conn()) { return list(c, sql, m, p); } catch (SQLException e) { throw new DbException(e); }
    }
    public static <T> T one(String sql, RowMapper<T> m, Object... p) {
        try (Connection c = conn()) { return one(c, sql, m, p); } catch (SQLException e) { throw new DbException(e); }
    }
    public static long count(String sql, Object... p) {
        try (Connection c = conn()) { return count(c, sql, p); } catch (SQLException e) { throw new DbException(e); }
    }
    public static int update(String sql, Object... p) {
        try (Connection c = conn()) { return update(c, sql, p); } catch (SQLException e) { throw new DbException(e); }
    }
    public static long insert(String sql, Object... p) {
        try (Connection c = conn()) { return insert(c, sql, p); } catch (SQLException e) { throw new DbException(e); }
    }

    /** Runs the work in one transaction (commit on success, rollback on any exception). */
    public static <T> T tx(Work<T> w) throws Exception {
        try (Connection c = conn()) {
            c.setAutoCommit(false);
            try {
                T r = w.run(c);
                c.commit();
                return r;
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
