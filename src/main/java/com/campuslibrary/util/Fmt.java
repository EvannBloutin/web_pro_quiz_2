package com.campuslibrary.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Static helpers exposed to JSP as EL functions (see WEB-INF/tlds/cl.tld). */
public final class Fmt {
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);
    private Fmt() {}

    /** HTML-escape (prevents XSS). */
    public static String h(Object o) {
        if (o == null) return "";
        String s = o.toString();
        StringBuilder b = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': b.append("&amp;"); break;
                case '<': b.append("&lt;"); break;
                case '>': b.append("&gt;"); break;
                case '"': b.append("&quot;"); break;
                case '\'': b.append("&#39;"); break;
                default: b.append(c);
            }
        }
        return b.toString();
    }
    public static String date(Object o) {
        if (o instanceof LocalDate) return D.format((LocalDate) o);
        if (o instanceof LocalDateTime) return D.format((LocalDateTime) o);
        return "";
    }
    public static String dateTime(Object o) { return o instanceof LocalDateTime ? DT.format((LocalDateTime) o) : date(o); }
    public static String money(long amount) { return String.format(Locale.ROOT, "Rp %,d", amount); }
    /** Percentage helper for CSS bars. */
    public static String pct(double value, double max) {
        return max <= 0 ? "0" : String.format(Locale.ROOT, "%.1f", Math.min(100.0, value * 100.0 / max));
    }
    public static String enc(Object o) {
        return o == null ? "" : java.net.URLEncoder.encode(o.toString(), java.nio.charset.StandardCharsets.UTF_8);
    }
    public static String stars(double v) { return String.format(Locale.ROOT, "%.2f", v); }
    public static String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, Math.max(0, n - 1)).trim() + "\u2026";
    }
}
